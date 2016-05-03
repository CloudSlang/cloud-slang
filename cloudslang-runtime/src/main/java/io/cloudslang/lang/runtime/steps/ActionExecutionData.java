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
import io.cloudslang.lang.runtime.bindings.scripts.ScriptExecutor;
import io.cloudslang.lang.runtime.env.ReturnValues;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.runtime.api.java.JavaRuntimeService;
import io.cloudslang.score.api.execution.ExecutionParametersConsts;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

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

    @Autowired
    private JavaRuntimeService javaExecutionService;

    public void doAction(@Param(ScoreLangConstants.RUN_ENV) RunEnvironment runEnv,
                         @Param(ExecutionParametersConsts.NON_SERIALIZABLE_EXECUTION_DATA) Map<String, Object> nonSerializableExecutionData,
                         @Param(ScoreLangConstants.ACTION_TYPE) ActionType actionType,
                         @Param(ScoreLangConstants.ACTION_DEPENDENCIES) List<String> dependencies,
                         @Param(ScoreLangConstants.ACTION_CLASS_KEY) String className,
                         @Param(ScoreLangConstants.ACTION_METHOD_KEY) String methodName,
                         @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
                         @Param(ScoreLangConstants.PYTHON_SCRIPT_KEY) String python_script,
                         @Param(ScoreLangConstants.NEXT_STEP_ID_KEY) Long nextStepId) {

        Map<String, Serializable> returnValue = new HashMap<>();
        Map<String, Serializable> callArguments = runEnv.removeCallArguments();
        Map<String, Serializable> callArgumentsDeepCopy = new HashMap<>();

        for (Map.Entry<String, Serializable> entry : callArguments.entrySet()) {
            callArgumentsDeepCopy.put(entry.getKey(), SerializationUtils.clone(entry.getValue()));
        }

        Map<String, SerializableSessionObject> serializableSessionData = runEnv.getSerializableDataMap();
        fireEvent(executionRuntimeServices, ScoreLangConstants.EVENT_ACTION_START, "Preparing to run action " + actionType,
                runEnv.getExecutionPath().getParentPath(), LanguageEventData.StepType.ACTION, null,
                Pair.of(LanguageEventData.CALL_ARGUMENTS, (Serializable) callArgumentsDeepCopy));

        if(logger.isDebugEnabled()) {
            logger.debug("Going to executing [" + actionType.name() + "] action with dependencies [" + dependencies + "]");
        }

        try {
            switch (actionType) {
                case JAVA:
                    returnValue = runJavaAction(serializableSessionData, callArguments, nonSerializableExecutionData,
                            className, methodName, dependencies);
                    break;
                case PYTHON:
                    returnValue = prepareAndRunPythonAction(callArguments, python_script);
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

        //todo: hook

        ReturnValues returnValues = new ReturnValues(returnValue, null);
        runEnv.putReturnValues(returnValues);
        fireEvent(executionRuntimeServices, ScoreLangConstants.EVENT_ACTION_END, "Action performed",
                runEnv.getExecutionPath().getParentPath(), LanguageEventData.StepType.ACTION, null,
                Pair.of(LanguageEventData.RETURN_VALUES, (Serializable) returnValue));

        runEnv.putNextStepPosition(nextStepId);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Serializable> runJavaAction(Map<String, SerializableSessionObject> serializableSessionData,
                                                    Map<String, Serializable> currentContext,
                                                    Map<String, Object> nonSerializableExecutionData,
                                                    String className,
                                                    String methodName,
                                                    List<String> dependencies) {
        List<Object> actualParameters = extractMethodData(serializableSessionData, currentContext, nonSerializableExecutionData, className, methodName);
        String dependency = (dependencies == null || dependencies.isEmpty()) ? "" : dependencies.get(0);
        Map<String, Serializable> returnMap = (Map<String, Serializable>) javaExecutionService.execute(dependency,
                className, methodName, actualParameters.toArray(new Object[actualParameters.size()]));
        if (returnMap == null) {
            throw new RuntimeException("Action method did not return Map<String,String>");
        }
        return returnMap;
    }

    private List<Object> extractMethodData(Map<String, SerializableSessionObject> serializableSessionData,
                                       Map<String, Serializable> currentContext,
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

    protected List<Object> resolveActionArguments(Map<String, SerializableSessionObject> serializableSessionData,
                                              Method actionMethod,
                                              Map<String, Serializable> currentContext,
                                              Map<String, Object> nonSerializableExecutionData) {
        List<Object> args = new ArrayList<>();

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
                        Serializable value = currentContext.get(parameterName);
                        Class parameterClass = parameterTypes[index - 1];
                        if (parameterClass.isInstance(value) || value == null) {
                            args.add(value);
                        } else {
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
        return args;
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

    private Map<String, Serializable> prepareAndRunPythonAction(
            Map<String, Serializable> callArguments,
            String pythonScript) {

        if (StringUtils.isNotBlank(pythonScript)) {
            return scriptExecutor.executeScript(pythonScript, callArguments);
        }

        throw new RuntimeException("Python script not found in action data");
    }

}
