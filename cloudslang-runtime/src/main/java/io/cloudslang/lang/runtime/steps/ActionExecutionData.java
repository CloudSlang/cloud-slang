/**
 * ****************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 * <p/>
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * *****************************************************************************
 */
package io.cloudslang.lang.runtime.steps;

import com.hp.oo.sdk.content.annotations.Param;
import com.hp.oo.sdk.content.plugin.GlobalSessionObject;
import com.hp.oo.sdk.content.plugin.SerializableSessionObject;
import io.cloudslang.lang.entities.ActionType;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptExecutor;
import io.cloudslang.lang.runtime.env.ReturnValues;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.score.api.execution.ExecutionParametersConsts;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.cloudslang.score.api.execution.ExecutionParametersConsts.EXECUTION_RUNTIME_SERVICES;

/**
 * User: stoneo
 * Date: 02/11/2014
 * Time: 10:25
 */
@Component
public class ActionExecutionData extends AbstractExecutionData {

    private static final Logger logger = Logger.getLogger(ActionExecutionData.class);

    @Autowired
    private ScriptExecutor scriptExecutor;

    public void doAction(@Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
                         @Param(ScoreLangConstants.RUN_ENV) RunEnvironment runEnv,
                         @Param(ExecutionParametersConsts.NON_SERIALIZABLE_EXECUTION_DATA) Map<String, Object> nonSerializableExecutionData,
                         @Param(ScoreLangConstants.NEXT_STEP_ID_KEY) Long nextStepId,
                         @Param(ScoreLangConstants.ACTION_TYPE) ActionType actionType,
                         @Param(ScoreLangConstants.JAVA_ACTION_CLASS_KEY) String className,
                         @Param(ScoreLangConstants.JAVA_ACTION_METHOD_KEY) String methodName,
                         @Param(ScoreLangConstants.JAVA_ACTION_GAV_KEY) String gav,
                         @Param(ScoreLangConstants.PYTHON_ACTION_SCRIPT_KEY) String script,
                         @Param(ScoreLangConstants.PYTHON_ACTION_DEPENDENCIES_KEY) Serializable dependencies) {

        Map<String, Value> returnValue = new HashMap<>();
        Map<String, Value> callArguments = runEnv.removeCallArguments();
        Map<String, Value> callArgumentsDeepCopy = new HashMap<>();

        for (Map.Entry<String, Value> entry : callArguments.entrySet()) {
            callArgumentsDeepCopy.put(entry.getKey(), ValueFactory.create(entry.getValue()));
        }

        Map<String, SerializableSessionObject> serializableSessionData = runEnv.getSerializableDataMap();
        fireEvent(executionRuntimeServices, ScoreLangConstants.EVENT_ACTION_START, "Preparing to run action " + actionType,
                runEnv.getExecutionPath().getParentPath(), LanguageEventData.StepType.ACTION, null,
                Pair.of(LanguageEventData.CALL_ARGUMENTS, (Serializable)callArgumentsDeepCopy));
        try {
            switch (actionType) {
                case JAVA:
                    returnValue = runJavaAction(serializableSessionData, callArguments, nonSerializableExecutionData, className, methodName);
                    break;
                case PYTHON:
                    returnValue = prepareAndRunPythonAction(callArguments, script);
                    break;
                default:
                    break;
            }
        } catch (RuntimeException ex) {
            fireEvent(executionRuntimeServices, ScoreLangConstants.EVENT_ACTION_ERROR, ex.getMessage(),
                    runEnv.getExecutionPath().getParentPath(), LanguageEventData.StepType.ACTION, null,
                    Pair.of(LanguageEventData.EXCEPTION, ex.getMessage()));
            logger.error(ex);
            throw (ex);
        }

        ReturnValues returnValues = new ReturnValues(returnValue, null);
        runEnv.putReturnValues(returnValues);
        fireEvent(executionRuntimeServices, ScoreLangConstants.EVENT_ACTION_END, "Action performed",
                runEnv.getExecutionPath().getParentPath(), LanguageEventData.StepType.ACTION, null);

        runEnv.putNextStepPosition(nextStepId);
    }

    private Map<String, Value> runJavaAction(Map<String, SerializableSessionObject> serializableSessionData,
                                             Map<String, Value> currentContext,
                                             Map<String, Object> nonSerializableExecutionData,
                                             String className,
                                             String methodName) {

        Value[] actualParameters = extractMethodData(serializableSessionData, currentContext, nonSerializableExecutionData, className, methodName);

        return invokeActionMethod(className, methodName, actualParameters);
    }

    private Value[] extractMethodData(Map<String, SerializableSessionObject> serializableSessionData,
                                      Map<String, Value> currentContext,
                                      Map<String, Object> nonSerializableExecutionData,
                                      String className,
                                      String methodName) {

        //get the Method object
        Method actionMethod = getMethodByName(className, methodName);
        if (actionMethod == null) {
            throw new RuntimeException("Method " + methodName + " is not part of class " + className);
        }

        //extract the parameters from execution context
        return resolveActionArguments(serializableSessionData, actionMethod, currentContext, nonSerializableExecutionData);
    }


    private Map<String, Value> invokeActionMethod(String className, String methodName, Value... parameters) {
        Method actionMethod = getMethodByName(className, methodName);
        Class actionClass = getActionClass(className);
        Map<String, Value> result;
        try {
            Object[] params = new Object[parameters.length];
            for (int index = 0; index < parameters.length; index++) {
                params[index] = parameters[index] == null ? null : parameters[index].get();
            }
            //noinspection unchecked
            Map<String, Serializable> returnObject = (Map<String, Serializable>)actionMethod.invoke(actionClass.newInstance(), params);

            result = new HashMap<>(returnObject.size());
            for (Map.Entry<String, Serializable> entry : returnObject.entrySet()) {
                result.put(entry.getKey(), ValueFactory.create(entry.getValue(), false));
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Invocation of method " + methodName + " of class " + className + " threw an exception", e);
        }
    }

    private Class getActionClass(String className) {
        Class actionClass;
        try {
            actionClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class name " + className + " was not found", e);
        }
        return actionClass;
    }

    private Method getMethodByName(String className, String methodName) {
        Class actionClass = getActionClass(className);
        Method[] methods = actionClass.getDeclaredMethods();
        Method actionMethod = null;
        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                actionMethod = m;
            }
        }
        return actionMethod;
    }

    protected Value[] resolveActionArguments(Map<String, SerializableSessionObject> serializableSessionData,
                                             Method actionMethod,
                                             Map<String, Value> currentContext,
                                             Map<String, Object> nonSerializableExecutionData) {
        List<Value> args = new ArrayList<>();

        int index = 0;
        Class[] parameterTypes = actionMethod.getParameterTypes();
        for (Annotation[] annotations : actionMethod.getParameterAnnotations()) {
            index++;
            for (Annotation annotation : annotations) {
                if (annotation instanceof Param) {
                    if (parameterTypes[index - 1].equals(GlobalSessionObject.class)) {
                        handleNonSerializableSessionContextArgument(nonSerializableExecutionData, args, (Param) annotation);
                    } else if (parameterTypes[index - 1].equals(SerializableSessionObject.class)) {
                        handleSerializableSessionContextArgument(serializableSessionData, args, (Param) annotation);
                    } else {
                        String parameterName = ((Param) annotation).value();
                        Value value = currentContext.get(parameterName);
                        Class parameterClass = parameterTypes[index - 1];
                        if (value == null || parameterClass.isInstance(value.get())) {
                            args.add(value);
                        } else {
                            //noinspection StringBufferReplaceableByString
                            StringBuilder exceptionMessageBuilder = new StringBuilder();
                            exceptionMessageBuilder.append("Parameter type mismatch for action ");
                            exceptionMessageBuilder.append(actionMethod.getName());
                            exceptionMessageBuilder.append(" of class ");
                            exceptionMessageBuilder.append(actionMethod.getDeclaringClass().getName());
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
        return args.toArray(new Value[args.size()]);
    }

    private void handleNonSerializableSessionContextArgument(Map<String, Object> nonSerializableExecutionData, List<Value> args, Param annotation) {
        String key = annotation.value();
        Object nonSerializableSessionContextObject = nonSerializableExecutionData.get(key);
        if (nonSerializableSessionContextObject == null) {
            nonSerializableSessionContextObject = new GlobalSessionObject<>();
            nonSerializableExecutionData.put(key, nonSerializableSessionContextObject);
        }
        args.add(ValueFactory.create((Serializable)nonSerializableSessionContextObject));
    }

    private void handleSerializableSessionContextArgument(Map<String, SerializableSessionObject> serializableSessionData, List<Value> args, Param annotation) {
        String key = annotation.value();
        SerializableSessionObject serializableSessionContextObject = serializableSessionData.get(key);
        if (serializableSessionContextObject == null) {
            serializableSessionContextObject = new SerializableSessionObject();
            //noinspection unchecked
            serializableSessionData.put(key, serializableSessionContextObject);
        }
        args.add(ValueFactory.create(serializableSessionContextObject));
    }

    private Map<String, Value> prepareAndRunPythonAction(
            Map<String, Value> callArguments,
            String pythonScript) {

        if (StringUtils.isNotBlank(pythonScript)) {
            return scriptExecutor.executeScript(pythonScript, callArguments);
        }

        throw new RuntimeException("Python script not found in action data");
    }

}
