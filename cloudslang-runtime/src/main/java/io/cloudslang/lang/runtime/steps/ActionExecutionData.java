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
import com.hp.oo.sdk.content.plugin.SerializableSessionObject;
import io.cloudslang.lang.entities.ActionType;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptExecutor;
import io.cloudslang.lang.runtime.env.ReturnValues;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.runtime.api.java.JavaRuntimeService;
import io.cloudslang.score.api.execution.ExecutionParametersConsts;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.python.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.cloudslang.score.api.execution.ExecutionParametersConsts.EXECUTION_RUNTIME_SERVICES;

/**
 * User: stoneo
 * Date: 02/11/2014
 * Time: 10:25
 */
@Component
public class ActionExecutionData extends AbstractExecutionData {

    private static final Logger logger = Logger.getLogger(ActionExecutionData.class);
    public static final String PACKAGING_TYPE_JAR = "jar";
    public static final String PACKAGING_TYPE_ZIP = "zip";
    public static final int GAV_PARTS = 3;

    @Autowired
    private ScriptExecutor scriptExecutor;

    @Autowired
    private JavaRuntimeService javaExecutionService;

    public void doAction(@Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
                         @Param(ScoreLangConstants.RUN_ENV) RunEnvironment runEnv,
                         @Param(ExecutionParametersConsts.NON_SERIALIZABLE_EXECUTION_DATA)
                                 Map<String, Object> nonSerializableExecutionData,
                         @Param(ScoreLangConstants.NEXT_STEP_ID_KEY) Long nextStepId,
                         @Param(ScoreLangConstants.ACTION_TYPE) ActionType actionType,
                         @Param(ScoreLangConstants.JAVA_ACTION_CLASS_KEY) String className,
                         @Param(ScoreLangConstants.JAVA_ACTION_METHOD_KEY) String methodName,
                         @Param(ScoreLangConstants.JAVA_ACTION_GAV_KEY) String gav,
                         @Param(ScoreLangConstants.PYTHON_ACTION_SCRIPT_KEY) String script,
                         @Param(ScoreLangConstants.PYTHON_ACTION_DEPENDENCIES_KEY) Collection<String> dependencies) {

        Map<String, Value> returnValue = new HashMap<>();
        Map<String, Value> callArguments = runEnv.removeCallArguments();
        Map<String, Value> callArgumentsDeepCopy = new HashMap<>();

        for (Map.Entry<String, Value> entry : callArguments.entrySet()) {
            callArgumentsDeepCopy.put(entry.getKey(), ValueFactory.create(entry.getValue()));
        }

        Map<String, SerializableSessionObject> serializableSessionData = runEnv.getSerializableDataMap();
        fireEvent(executionRuntimeServices, ScoreLangConstants.EVENT_ACTION_START, "Preparing to run action " +
                        actionType,
                runEnv.getExecutionPath().getParentPath(), LanguageEventData.StepType.ACTION, null,
                Pair.of(LanguageEventData.CALL_ARGUMENTS, (Serializable) callArgumentsDeepCopy));
        try {
            switch (actionType) {
                case JAVA:
                    returnValue = runJavaAction(serializableSessionData, callArguments, nonSerializableExecutionData,
                            gav, className, methodName);
                    break;
                case PYTHON:
                    returnValue = prepareAndRunPythonAction(dependencies, script, callArguments);
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

    @SuppressWarnings("unchecked")
    private Map<String, Value> runJavaAction(Map<String, SerializableSessionObject> serializableSessionData,
                                             Map<String, Value> currentContext,
                                             Map<String, Object> nonSerializableExecutionData,
                                             String gav, String className, String methodName) {
        Map<String, Serializable> returnMap = (Map<String, Serializable>) javaExecutionService
                .execute(normalizeJavaGav(gav), className, methodName,
                new CloudSlangJavaExecutionParameterProvider(serializableSessionData,
                        createActionContext(currentContext), nonSerializableExecutionData));
        if (returnMap == null) {
            throw new RuntimeException("Action method did not return Map<String,String>");
        }
        return createActionResult(returnMap, currentContext);
    }

    protected Map<String, Serializable> createActionContext(Map<String, Value> context) {
        Map<String, Serializable> result = new HashMap<>();
        for (Map.Entry<String, Value> entry : context.entrySet()) {
            result.put(entry.getKey(), entry.getValue() == null ? null : entry.getValue().get());
        }
        return result;
    }

    protected Map<String, Value> createActionResult(Map<String, Serializable> executionResult,
                                                    Map<String, Value> context) {
        Map<String, Value> result = new HashMap<>();
        for (Map.Entry<String, Serializable> entry : executionResult.entrySet()) {
            Value callArgumenet = context.get(entry.getKey());
            Value value = ValueFactory.create(entry.getValue(), callArgumenet != null && callArgumenet.isSensitive());
            result.put(entry.getKey(), value);
        }
        return result;
    }

    /**
     * Checks whether need to append packaging type to the gav
     *
     * @param gav
     * @param packagingType
     * @return
     */
    private String normalizeGav(String gav, String packagingType) {
        //this is temporary solution until we add mandatory for java
        //after this we will not check the empty assuming it is always not empty and we have 3 parts
        return (StringUtils.isEmpty(gav) || (gav.split(":").length > GAV_PARTS)) ? gav : gav + ":" + packagingType;
    }

    private String normalizeJavaGav(String gav) {
        return normalizeGav(gav, PACKAGING_TYPE_JAR);
    }

    private Set<String> normalizePythonDependencies(Collection<String> dependencies) {
        Set<String> pythonDependencies = dependencies == null || dependencies.isEmpty() ?
                Sets.<String>newHashSet() : new HashSet<>(dependencies);
        Set<String> normalizedDependencies = new HashSet<>(pythonDependencies.size());
        for (String dependency : pythonDependencies) {
            normalizedDependencies.add(normalizeGav(dependency, PACKAGING_TYPE_ZIP));
        }
        return normalizedDependencies;
    }

    private Map<String, Value> prepareAndRunPythonAction(Collection<String> dependencies, String pythonScript,
                                                         Map<String, Value> callArguments) {
        if (StringUtils.isNotBlank(pythonScript)) {
            return scriptExecutor.executeScript(
                    normalizePythonDependencies(dependencies), pythonScript, callArguments);
        }

        throw new RuntimeException("Python script not found in action data");
    }

}
