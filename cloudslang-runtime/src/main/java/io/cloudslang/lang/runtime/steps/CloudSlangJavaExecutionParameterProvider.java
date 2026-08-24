/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.steps;

import com.hp.oo.sdk.content.annotations.Param;
import com.hp.oo.sdk.content.plugin.GlobalSessionObject;
import com.hp.oo.sdk.content.plugin.SerializableSessionObject;
import com.hp.oo.sdk.content.plugin.SessionObject;
import com.hp.oo.sdk.content.plugin.StepSerializableSessionObject;
import io.cloudslang.runtime.api.java.JavaExecutionParametersProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.cloudslang.score.api.execution.ExecutionParametersConsts.GLOBAL_SESSION_OBJECT;
import static io.cloudslang.score.api.execution.ExecutionParametersConsts.SESSION_OBJECT;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 17/05/2016.
 */
public class CloudSlangJavaExecutionParameterProvider implements JavaExecutionParametersProvider {
    private static final Logger logger = LogManager.getLogger(CloudSlangJavaExecutionParameterProvider.class);

    private static final String PARAM_CLASS_NAME = Param.class.getCanonicalName();
    private static final String GLOBAL_SESSION_OBJECT_CLASS_NAME = GlobalSessionObject.class.getCanonicalName();
    private static final String SESSION_OBJECT_CLASS_NAME = SessionObject.class.getCanonicalName();
    private static final String SERIALIZABLE_SESSION_OBJECT = SerializableSessionObject.class.getCanonicalName();
    private static final String STEP_SERIALIZABLE_SESSION_OBJECT =
            StepSerializableSessionObject.class.getCanonicalName();
    private static final String LIST_ITERATOR_ACTION = "io.cloudslang.content.actions.ListIteratorAction";

    private final Map<String, SerializableSessionObject> serializableSessionData;
    private final Map<String, Serializable> currentContext;
    private final Map<String, Object> globalSessionObjectData;
    private final Map<String, Object> sessionObjectData;
    private final String nodeNameWithDepth;
    private final int depth;

    public CloudSlangJavaExecutionParameterProvider(Map<String, SerializableSessionObject> serializableSessionData,
                                                    Map<String, Serializable> currentContext,
                                                    Map<String, Map<String, Object>> nonSerializableExecutionData,
                                                    String nodeNameWithDepth, int depth) {
        this.serializableSessionData = serializableSessionData;
        this.currentContext = currentContext;
        this.globalSessionObjectData = nonSerializableExecutionData
                .getOrDefault(GLOBAL_SESSION_OBJECT, new HashMap<>());
        this.sessionObjectData = nonSerializableExecutionData.getOrDefault(SESSION_OBJECT, new HashMap<>());
        this.nodeNameWithDepth = nodeNameWithDepth;
        this.depth = depth;
    }

    @Override
    public Object[] getExecutionParameters(Method executionMethod) {
        Class<?>[] parameterTypes = executionMethod.getParameterTypes();
        List<Object> args = new ArrayList<>(parameterTypes.length);
        int index = -1;
        Annotation[][] parameterAnnotations = executionMethod.getParameterAnnotations();
        for (Annotation[] annotations : parameterAnnotations) {
            index++;
            for (Annotation annotation : annotations) {
                String parameterName = getValueIfParamAnnotation(annotation);
                if (parameterName != null) {
                    final String methodName = executionMethod.getDeclaringClass().getName();
                    String paramClassName = parameterTypes[index].getCanonicalName();
                    ClassLoader parameterClassLoader = parameterTypes[index].getClassLoader();
                    if (paramClassName.equals(GLOBAL_SESSION_OBJECT_CLASS_NAME)) {
                        handleSessionContextArgument(globalSessionObjectData, GLOBAL_SESSION_OBJECT_CLASS_NAME,
                                args, parameterName, methodName,
                                parameterClassLoader);
                    } else if (paramClassName.equals(SESSION_OBJECT_CLASS_NAME)) {
                        handleSessionContextArgument(sessionObjectData, SESSION_OBJECT_CLASS_NAME,
                                args, parameterName + "_" + (depth - 1), methodName,
                                parameterClassLoader);
                    } else if (paramClassName.equals(SERIALIZABLE_SESSION_OBJECT)) {
                        handleSessionContextArgument(serializableSessionData, SERIALIZABLE_SESSION_OBJECT,
                                args, parameterName, methodName,
                                parameterClassLoader);
                    } else if (paramClassName.equals(STEP_SERIALIZABLE_SESSION_OBJECT)) {
                        handleStepSessionContextArgument(serializableSessionData, args, parameterName,
                                parameterClassLoader, parameterTypes[index]);
                    } else {
                        Serializable value = currentContext.get(parameterName);
                        Class<?> parameterClass = parameterTypes[index];
                        if ((value == null) || parameterClass.isInstance(value)) {
                            args.add(value);
                        } else {
                            throw new RuntimeException(new StringBuilder("Parameter type mismatch for action ")
                                    .append(executionMethod.getName())
                                    .append(" of class ")
                                    .append(executionMethod.getDeclaringClass().getName())
                                    .append(". Parameter ")
                                    .append(parameterName)
                                    .append(" expects type ")
                                    .append(parameterClass.getName())
                                    .append(". Actual type is ")
                                    .append(value.getClass().getName())
                                    .toString());
                        }
                    }
                }
            }
        }
        if (args.size() != parameterAnnotations.length) {
            throw new RuntimeException("All action arguments should be annotated with @Param");
        }
        return args.toArray(new Object[args.size()]);
    }

    private String getValueIfParamAnnotation(Annotation annotation) {
        Class<? extends Annotation> annotationType = annotation.annotationType();
        if (PARAM_CLASS_NAME.equalsIgnoreCase(annotation.annotationType().getCanonicalName())) {
            try {
                return (String) annotationType.getMethod("value", new Class[0]).invoke(annotation, new Object[0]);
            } catch (Exception e) {
                throw new RuntimeException("Failed to get value from " + Param.class.getCanonicalName() +
                        " annotation", e);
            }
        }
        return null;
    }

    private void handleStepSessionContextArgument(Map sessionData, List<Object> args,
                                                  String parameterName, ClassLoader classLoader,
                                                  Class<?> expectedClass) {
        final String stepSessionKey = parameterName + "_" + nodeNameWithDepth;
        Object sessionContextObject = sessionData.get(stepSessionKey);
        if (sessionContextObject == null) {
            try {
                sessionContextObject = Class.forName(STEP_SERIALIZABLE_SESSION_OBJECT, true, classLoader)
                        .getConstructor(String.class)
                        .newInstance(stepSessionKey);
            } catch (Exception e) {
                throw new RuntimeException(
                        "Failed to create instance of [" + STEP_SERIALIZABLE_SESSION_OBJECT + "] class", e);
            }
            //noinspection unchecked
            sessionData.put(stepSessionKey, sessionContextObject);
        }

        if (sessionContextObject != null && expectedClass != null &&
                !expectedClass.isInstance(sessionContextObject)) {
            sessionContextObject = migrateSessionContextObject(sessionData, stepSessionKey,
                    sessionContextObject, expectedClass);
        }
        args.add(sessionContextObject);
    }

    private Object migrateSessionContextObject(Map sessionData, String sessionKey,
                                               Object sessionContextObject, Class<?> expectedClass) {
        try {
            Object migratedObject = instantiateMigratedObject(expectedClass, sessionContextObject, sessionKey);
            copyCompatibleFields(sessionContextObject, migratedObject);
            //noinspection unchecked
            sessionData.put(sessionKey, migratedObject);
            return migratedObject;
        } catch (Exception e) {
            logger.warn("Failed to migrate session context object. key: {}, expectedClass: {}, " +
                            "actualClass: {}, node: {}. Keeping original object for this invocation.",
                    sessionKey,
                    expectedClass.getName(),
                    sessionContextObject.getClass().getName(),
                    nodeNameWithDepth,
                    e);
            return sessionContextObject;
        }
    }

    private Object instantiateMigratedObject(Class<?> expectedClass, Object sessionContextObject,
                                             String fallbackSessionName)
            throws ReflectiveOperationException {
        try {
            return expectedClass.getConstructor(String.class)
                    .newInstance(resolveStepSessionName(sessionContextObject, fallbackSessionName));
        } catch (NoSuchMethodException ignored) {
            return expectedClass.newInstance();
        }
    }

    private String resolveStepSessionName(Object sessionContextObject, String fallbackName) {
        Object nameValue = invokeNoArgMethodIfExists(sessionContextObject, "getName");
        if (nameValue != null) {
            return String.valueOf(nameValue);
        }

        Class<?> currentClass = sessionContextObject.getClass();
        while (currentClass != null) {
            try {
                Field nameField = currentClass.getDeclaredField("name");
                nameField.setAccessible(true);
                Object fieldValue = nameField.get(sessionContextObject);
                return fieldValue == null ? fallbackName : String.valueOf(fieldValue);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
                // Keep searching up the hierarchy and use fallback when unavailable.
            }
            currentClass = currentClass.getSuperclass();
        }

        return fallbackName;
    }

    private Object invokeNoArgMethodIfExists(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void copyCompatibleFields(Object source, Object target) throws IllegalAccessException {
        Class<?> sourceClass = source.getClass();
        while (sourceClass != null) {
            Field[] sourceFields = sourceClass.getDeclaredFields();
            for (Field sourceField : sourceFields) {
                int modifiers = sourceField.getModifiers();
                if (Modifier.isStatic(modifiers)) {
                    continue;
                }
                sourceField.setAccessible(true);
                Object sourceValue = sourceField.get(source);
                setFieldOnTargetHierarchy(target, sourceField.getName(), sourceValue);
            }
            sourceClass = sourceClass.getSuperclass();
        }
    }

    private void setFieldOnTargetHierarchy(Object target, String fieldName, Object fieldValue)
            throws IllegalAccessException {
        Class<?> targetClass = target.getClass();
        while (targetClass != null) {
            try {
                Field targetField = targetClass.getDeclaredField(fieldName);
                int modifiers = targetField.getModifiers();
                if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
                    return;
                }
                targetField.setAccessible(true);
                targetField.set(target, fieldValue);
                return;
            } catch (NoSuchFieldException | IllegalArgumentException ignore) {
                // Continue searching in parent types or skip incompatible values.
            }
            targetClass = targetClass.getSuperclass();
        }
    }

    private void handleSessionContextArgument(Map sessionData, String objectClassName, List<Object> args,
                                              String parameterName, String methodName, ClassLoader classLoader) {
        // cloudSlang list iterator fix
        final String parameter = StringUtils.equals(methodName, LIST_ITERATOR_ACTION) ?
                this.nodeNameWithDepth : parameterName;

        Object sessionContextObject = sessionData.get(parameter);

        if (sessionContextObject == null) {
            try {
                sessionContextObject = Class.forName(objectClassName, true, classLoader).newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create instance of [" + objectClassName + "] class", e);
            }
            //noinspection unchecked
            sessionData.put(parameter, sessionContextObject);
        } else {
            try {
                Class<?> expectedClass = Class.forName(objectClassName, true, classLoader);
                if (!expectedClass.isInstance(sessionContextObject)) {
                    sessionContextObject = migrateSessionContextObject(sessionData, parameter,
                            sessionContextObject, expectedClass);
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Failed to load class [" + objectClassName + "]", e);
            }
        }

        args.add(sessionContextObject);
    }
}
