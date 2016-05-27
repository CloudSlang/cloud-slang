package io.cloudslang.lang.runtime.steps;

import com.hp.oo.sdk.content.annotations.Param;
import com.hp.oo.sdk.content.plugin.GlobalSessionObject;
import com.hp.oo.sdk.content.plugin.SerializableSessionObject;
import io.cloudslang.runtime.api.java.JavaExecutionParametersProvider;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 17/05/2016.
 */
public class CloudSlangJavaExecutionParameterProvider implements JavaExecutionParametersProvider {
    private static final String PARAM_CLASS_NAME = Param.class.getCanonicalName();
    private static final String GLOBAL_SESSION_OBJECT_CLASS_NAME = GlobalSessionObject.class.getCanonicalName();
    private static final String SERIALIZABLE_SESSION_OBJECT = SerializableSessionObject.class.getCanonicalName();
    private final Map<String, SerializableSessionObject> serializableSessionData;
    private final Map<String, Serializable> currentContext;
    private final Map<String, Object> nonSerializableExecutionData;

    public CloudSlangJavaExecutionParameterProvider(Map<String, SerializableSessionObject> serializableSessionData,
                                                    Map<String, Serializable> currentContext,
                                                    Map<String, Object> nonSerializableExecutionData) {
        this.serializableSessionData = serializableSessionData;
        this.currentContext = currentContext;
        this.nonSerializableExecutionData = nonSerializableExecutionData;
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
                        handleNonSerializableSessionContextArgument(nonSerializableExecutionData, args, parameterName,
                                annotation.getClass().getClassLoader());
                    } else if (paramClassName.equals(SERIALIZABLE_SESSION_OBJECT)) {
                        handleSerializableSessionContextArgument(serializableSessionData, args, parameterName,
                                annotation.getClass().getClassLoader());
                    } else {
                        Serializable value = currentContext.get(parameterName);
                        Class parameterClass = parameterTypes[index - 1];
                        if (parameterClass.isInstance(value) || value == null) {
                            args.add(value);
                        } else {
                            StringBuilder exceptionMessageBuilder = new StringBuilder();
                            exceptionMessageBuilder.append("Parameter type mismatch for action ");
                            exceptionMessageBuilder.append(executionMethod.getName());
                            exceptionMessageBuilder.append(" of class ");
                            exceptionMessageBuilder.append(executionMethod.getDeclaringClass().getName());
                            exceptionMessageBuilder.append(". Parameter ");
                            exceptionMessageBuilder.append(parameterName);
                            exceptionMessageBuilder.append(" expects type ");
                            exceptionMessageBuilder.append(parameterClass.getName());
                            throw new RuntimeException(exceptionMessageBuilder.toString());
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
                throw new RuntimeException("Failed to get value from " + Param.class.getCanonicalName() + " annotation", e);
            }
        }
        return null;
    }

    private void handleNonSerializableSessionContextArgument(Map<String, Object> nonSerializableExecutionData, List<Object> args, String parameterName,
                                                             ClassLoader classLoader) {
        Object nonSerializableSessionContextObject = nonSerializableExecutionData.get(parameterName);
        if (nonSerializableSessionContextObject == null) {
            try {
                nonSerializableSessionContextObject = Class.forName(GLOBAL_SESSION_OBJECT_CLASS_NAME, true, classLoader).newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create instance of [" + GLOBAL_SESSION_OBJECT_CLASS_NAME + "] class", e);
            }
            nonSerializableExecutionData.put(parameterName, nonSerializableSessionContextObject);
        }
        args.add(nonSerializableSessionContextObject);
    }

    private void handleSerializableSessionContextArgument(Map<String, SerializableSessionObject> serializableSessionData, List<Object> args, String parameterName,
                                                          ClassLoader classLoader) {
        SerializableSessionObject serializableSessionContextObject = serializableSessionData.get(parameterName);
        if (serializableSessionContextObject == null) {
            try {
                serializableSessionContextObject = (SerializableSessionObject) Class.forName(SERIALIZABLE_SESSION_OBJECT, true, classLoader).newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create instance of [" + SERIALIZABLE_SESSION_OBJECT + "] class", e);
            }
            //noinspection unchecked
            serializableSessionData.put(parameterName, serializableSessionContextObject);
        }
        args.add(serializableSessionContextObject);
    }
}
