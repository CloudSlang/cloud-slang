package io.cloudslang.lang.runtime.steps;

import com.hp.oo.sdk.content.annotations.Param;
import com.hp.oo.sdk.content.plugin.GlobalSessionObject;
import com.hp.oo.sdk.content.plugin.SerializableSessionObject;
import io.cloudslang.runtime.api.java.JavaExecutionParametersProvider;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Genadi Rabinovich, genadi@hpe.com on 17/05/2016.
 */
public class CloudSlangJavaExecutionParameterProvider implements JavaExecutionParametersProvider {
    private static Method PARAM_VALUE_METHOD = null;

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
                    if (parameterTypes[index - 1].equals(GlobalSessionObject.class)) {
                        handleNonSerializableSessionContextArgument(nonSerializableExecutionData, args, parameterName);
                    } else if (parameterTypes[index - 1].equals(SerializableSessionObject.class)) {
                        handleSerializableSessionContextArgument(serializableSessionData, args, parameterName);
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
        if ("com.hp.oo.sdk.content.annotations.Param".equalsIgnoreCase(annotation.annotationType().getCanonicalName())) {
            if(PARAM_VALUE_METHOD == null) {
                try {
                    PARAM_VALUE_METHOD = annotationType.getMethod("value", new Class[0]);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("com.hp.oo.sdk.content.annotations.Param" + " annotation does not have value method!!!!", e);
                }
            }
            try {
                return (String) PARAM_VALUE_METHOD.invoke(annotation, new Object[0]);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Failed to get value from " + "com.hp.oo.sdk.content.annotations.Param" + " annotation", e);
            }
        }
        return null;
    }

    private void handleNonSerializableSessionContextArgument(Map<String, Object> nonSerializableExecutionData, List<Object> args, String parameterName) {
        Object nonSerializableSessionContextObject = nonSerializableExecutionData.get(parameterName);
        if (nonSerializableSessionContextObject == null) {
            nonSerializableSessionContextObject = new GlobalSessionObject<>();
            nonSerializableExecutionData.put(parameterName, nonSerializableSessionContextObject);
        }
        args.add(nonSerializableSessionContextObject);
    }

    private void handleSerializableSessionContextArgument(Map<String, SerializableSessionObject> serializableSessionData, List<Object> args, String parameterName) {
        SerializableSessionObject serializableSessionContextObject = serializableSessionData.get(parameterName);
        if (serializableSessionContextObject == null) {
            serializableSessionContextObject = new SerializableSessionObject();
            //noinspection unchecked
            serializableSessionData.put(parameterName, serializableSessionContextObject);
        }
        args.add(serializableSessionContextObject);
    }
}
