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

import java.io.Serializable;
import java.lang.annotation.Annotation;
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
    private static final String PARAM_CLASS_NAME = Param.class.getCanonicalName();
    private static final String GLOBAL_SESSION_OBJECT_CLASS_NAME = GlobalSessionObject.class.getCanonicalName();
    private static final String SESSION_OBJECT_CLASS_NAME = SessionObject.class.getCanonicalName();
    private static final String SERIALIZABLE_SESSION_OBJECT = SerializableSessionObject.class.getCanonicalName();
    private static final String STEP_SERIALIZABLE_SESSION_OBJECT =
            StepSerializableSessionObject.class.getCanonicalName();
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
        List<Object> args = new ArrayList<>();

        int index = 0;
        Class[] parameterTypes = executionMethod.getParameterTypes();
        for (Annotation[] annotations : executionMethod.getParameterAnnotations()) {
            index++;
            for (Annotation annotation : annotations) {
                String parameterName = getValueIfParamAnnotation(annotation);
                if (parameterName != null) {
                    String paramClassName = parameterTypes[index - 1].getCanonicalName();
                    if (paramClassName.equals(GLOBAL_SESSION_OBJECT_CLASS_NAME)) {
                        handleSessionContextArgument(globalSessionObjectData, GLOBAL_SESSION_OBJECT_CLASS_NAME,
                                args, parameterName,
                                annotation.getClass().getClassLoader());
                    } else if (paramClassName.equals(SESSION_OBJECT_CLASS_NAME)) {
                        handleSessionContextArgument(sessionObjectData, SESSION_OBJECT_CLASS_NAME,
                                args, parameterName + "_" + (depth - 1),
                                annotation.getClass().getClassLoader());
                    } else if (paramClassName.equals(SERIALIZABLE_SESSION_OBJECT)) {
                        handleSessionContextArgument(serializableSessionData, SERIALIZABLE_SESSION_OBJECT,
                                args, parameterName,
                                annotation.getClass().getClassLoader());
                    } else if (paramClassName.equals(STEP_SERIALIZABLE_SESSION_OBJECT)) {
                        handleStepSessionContextArgument(serializableSessionData, args, parameterName,
                                annotation.getClass().getClassLoader());
                    } else {
                        Serializable value = currentContext.get(parameterName);
                        Class parameterClass = parameterTypes[index - 1];
                        if (parameterClass.isInstance(value) || value == null) {
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
            if (args.size() != index) {
                throw new RuntimeException("All action arguments should be annotated with @Param");
            }
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
                                                  String parameterName, ClassLoader classLoader) {
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
        args.add(sessionContextObject);
    }

    private void handleSessionContextArgument(Map sessionData, String objectClassName, List<Object> args,
                                              String parameterName, ClassLoader classLoader) {
        // cloudslang list iterator fix
        final String parameter = this.nodeNameWithDepth.startsWith("list_iterator") ?
                                             this.nodeNameWithDepth : parameterName ;

        Object sessionContextObject = sessionData.get(parameter);
        if (sessionContextObject == null) {
            try {
                sessionContextObject = Class.forName(objectClassName, true, classLoader).newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create instance of [" + objectClassName + "] class", e);
            }
            //noinspection unchecked
            sessionData.put(parameter, sessionContextObject);
        }
        args.add(sessionContextObject);
    }
}
