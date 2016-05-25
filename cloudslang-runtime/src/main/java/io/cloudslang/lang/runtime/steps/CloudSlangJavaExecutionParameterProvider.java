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
                if (annotation instanceof Param) {
                    if (parameterTypes[index - 1].equals(GlobalSessionObject.class)) {
                        handleNonSerializableSessionContextArgument(nonSerializableExecutionData, args, (Param) annotation);
                    } else if (parameterTypes[index - 1].equals(SerializableSessionObject.class)) {
                        handleSerializableSessionContextArgument(serializableSessionData, args, (Param) annotation);
                    } else {
                        String parameterName = ((Param) annotation).value();
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

    private void handleNonSerializableSessionContextArgument(Map<String, Object> nonSerializableExecutionData, List<Object> args, Param annotation) {
        String key = annotation.value();
        Object nonSerializableSessionContextObject = nonSerializableExecutionData.get(key);
        if (nonSerializableSessionContextObject == null) {
            nonSerializableSessionContextObject = new GlobalSessionObject<>();
            nonSerializableExecutionData.put(key, nonSerializableSessionContextObject);
        }
        args.add(nonSerializableSessionContextObject);
    }

    private void handleSerializableSessionContextArgument(Map<String, SerializableSessionObject> serializableSessionData, List<Object> args, Param annotation) {
        String key = annotation.value();
        SerializableSessionObject serializableSessionContextObject = serializableSessionData.get(key);
        if (serializableSessionContextObject == null) {
            serializableSessionContextObject = new SerializableSessionObject();
            //noinspection unchecked
            serializableSessionData.put(key, serializableSessionContextObject);
        }
        args.add(serializableSessionContextObject);
    }

}
