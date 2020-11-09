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
import io.cloudslang.lang.compiler.modeller.model.SeqStep;
import io.cloudslang.lang.entities.ActionType;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptExecutor;
import io.cloudslang.lang.runtime.env.ReturnValues;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.runtime.api.java.JavaRuntimeService;
import io.cloudslang.runtime.api.sequential.SequentialExecutionService;
import io.cloudslang.score.api.execution.ExecutionParametersConsts;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.python.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.cloudslang.score.api.execution.ExecutionParametersConsts.EXECUTION_RUNTIME_SERVICES;
import static io.cloudslang.score.api.execution.ExecutionParametersConsts.SEQUENTIAL;

/**
 * User: stoneo
 * Date: 02/11/2014
 * Time: 10:25
 */
@Component
public class ActionExecutionData extends AbstractExecutionData {

    private static final Logger logger = LogManager.getLogger(ActionExecutionData.class);
    public static final String PACKAGING_TYPE_JAR = "jar";
    public static final String PACKAGING_TYPE_ZIP = "zip";
    public static final int GAV_PARTS = 3;

    private static final String RETURN_CODE = "returnCode";
    private static final String EXCEPTION = "exception";
    private static final String MARKER = "Exception: ";

    private static final boolean REMOVE_MESSAGE_FROM_LOGGED_EX =
            Boolean.parseBoolean(
                    System.getProperty("worker.execution.sanitizeOperationStacktrace","false"));

    @Autowired
    private ScriptExecutor scriptExecutor;

    @Autowired
    private JavaRuntimeService javaExecutionService;

    @Autowired
    private SequentialExecutionService seqExecutionService;

    @Autowired(required = false)
    private SlangStepDataConsumer stepDataConsumer;

    public void doAction(@Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices,
                         @Param(ScoreLangConstants.RUN_ENV) RunEnvironment runEnv,
                         @Param(ExecutionParametersConsts.NON_SERIALIZABLE_EXECUTION_DATA)
                                 Map<String, Map<String, Object>> nonSerializableExecutionData,
                         @Param(ScoreLangConstants.NEXT_STEP_ID_KEY) Long nextStepId,
                         @Param(ScoreLangConstants.ACTION_TYPE) ActionType actionType,
                         @Param(ScoreLangConstants.JAVA_ACTION_CLASS_KEY) String className,
                         @Param(ScoreLangConstants.JAVA_ACTION_METHOD_KEY) String methodName,
                         @Param(ScoreLangConstants.JAVA_ACTION_GAV_KEY) String gav,
                         @Param(ScoreLangConstants.PYTHON_ACTION_SCRIPT_KEY) String script,
                         @Param(ScoreLangConstants.PYTHON_ACTION_USE_JYTHON_KEY) Boolean useJython,
                         @Param(ScoreLangConstants.PYTHON_ACTION_DEPENDENCIES_KEY) Collection<String> dependencies,
                         @Param(ScoreLangConstants.SEQ_STEPS_KEY) List<SeqStep> steps,
                         @Param(ScoreLangConstants.SEQ_EXTERNAL_KEY) Boolean external,
                         @Param(ExecutionParametersConsts.EXECUTION) Serializable execution) {

        Map<String, Value> returnValue = new HashMap<>();
        Map<String, Value> callArguments = runEnv.removeCallArguments();
        Map<String, Value> callArgumentsDeepCopy = new HashMap<>();

        for (Map.Entry<String, Value> entry : callArguments.entrySet()) {
            callArgumentsDeepCopy.put(entry.getKey(), ValueFactory.create(entry.getValue()));
        }

        Map<String, SerializableSessionObject> serializableSessionData = runEnv.getSerializableDataMap();
        fireEvent(
                executionRuntimeServices,
                ScoreLangConstants.EVENT_ACTION_START,
                "Preparing to run action " + actionType,
                runEnv.getExecutionPath().getParentPath(),
                LanguageEventData.StepType.ACTION,
                null,
                callArgumentsDeepCopy,
                Pair.of(LanguageEventData.CALL_ARGUMENTS, (Serializable) callArgumentsDeepCopy));
        try {
            switch (actionType) {
                case JAVA:
                    returnValue = runJavaAction(serializableSessionData, callArguments, nonSerializableExecutionData,
                            gav, className, methodName, executionRuntimeServices.getNodeNameWithDepth(),
                            runEnv.getParentFlowStack().size());
                    break;
                case PYTHON:
                    returnValue = prepareAndRunPythonAction(dependencies, script, callArguments, useJython);
                    break;
                case SEQUENTIAL:
                    returnValue = runSequentialAction(callArguments, gav, steps, Boolean.TRUE.equals(external),
                            execution, runEnv, nextStepId);
                    break;
                default:
                    break;
            }
            if (stepDataConsumer != null) {
                stepDataConsumer.consumeStepData(callArguments, returnValue);
            }
        } catch (RuntimeException ex) {
            fireEvent(
                    executionRuntimeServices,
                    ScoreLangConstants.EVENT_ACTION_ERROR,
                    ex.getMessage(),
                    runEnv.getExecutionPath().getParentPath(),
                    LanguageEventData.StepType.ACTION,
                    null,
                    callArgumentsDeepCopy,
                    Pair.of(LanguageEventData.EXCEPTION, ex.getMessage()));
            logger.error(ex);
            throw (ex);
        }

        ReturnValues returnValues = new ReturnValues(returnValue, null);
        runEnv.putReturnValues(returnValues);
        fireEvent(
                executionRuntimeServices,
                ScoreLangConstants.EVENT_ACTION_END,
                "Action performed",
                runEnv.getExecutionPath().getParentPath(),
                LanguageEventData.StepType.ACTION,
                null,
                callArgumentsDeepCopy
        );

        if (!SEQUENTIAL.equals(actionType.getValue())) {
            /*
            Due to the way Sequential Actions work, we have to pause the execution BEFORE the actual run.
            Thus, we have to populate the run environment with the required data before pausing and persisting.
            We let the sequential action handler do it, since here it would be too late.
             */
            runEnv.putNextStepPosition(nextStepId);
        }
    }

    private Map<String, Value> runSequentialAction(
            Map<String, Value> currentContext,
            String gav,
            List<SeqStep> seqSteps,
            boolean external,
            Serializable execution,
            RunEnvironment runEnv,
            Long nextStepId) {
        runEnv.putNextStepPosition(nextStepId);
        @SuppressWarnings("unchecked")
        Map<String, Serializable> returnMap =
                (Map<String, Serializable>)
                        seqExecutionService.execute(
                                gav,
                                new CloudSlangSequentialExecutionParametersProviderImpl(
                                        currentContext,
                                        seqSteps,
                                        external), execution);
        return (returnMap != null) ? handleSensitiveValues(returnMap, currentContext) :
                new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Value> runJavaAction(Map<String, SerializableSessionObject> serializableSessionData,
                                             Map<String, Value> currentContext,
                                             Map<String, Map<String, Object>> nonSerializableExecutionData,
                                             String gav, String className, String methodName,
                                             String nodeNameWithDepth, int depth) {
        Map<String, Serializable> returnMap = (Map<String, Serializable>) javaExecutionService
                .execute(normalizeJavaGav(gav), className, methodName,
                        new CloudSlangJavaExecutionParameterProvider(serializableSessionData,
                                createActionContext(currentContext), nonSerializableExecutionData, nodeNameWithDepth,
                                depth));
        if (returnMap == null) {
            throw new RuntimeException("Action method did not return Map<String,String>");
        }

        final Serializable exception = returnMap.get(EXCEPTION);
        if (exception != null) {
            logException(exception.toString());
        }

        return handleSensitiveValues(returnMap, currentContext);
    }

    private void logException(String exception) {

        String stacktrace = exception;

        if (REMOVE_MESSAGE_FROM_LOGGED_EX) {
            StringWriter writer = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(writer);

            BufferedReader reader = new BufferedReader(new StringReader(stacktrace));

            final ListIterator<String> iterator = reader.lines().collect(Collectors.toList()).listIterator();

            while (iterator.hasNext()) {
                String line = iterator.next();
                int idx = line.indexOf(MARKER);
                String str = idx == -1 ? line : line.substring(0, idx + MARKER.length());

                printWriter.println(str);
            }

            printWriter.close();

            stacktrace = writer.toString();
        }

        logger.error("Java operation encountered an exception:\n" + stacktrace);
    }

    protected Map<String, Serializable> createActionContext(Map<String, Value> context) {
        Map<String, Serializable> result = new HashMap<>();
        for (Map.Entry<String, Value> entry : context.entrySet()) {
            result.put(entry.getKey(), entry.getValue() == null ? null : entry.getValue().get());
        }
        return result;
    }

    protected Map<String, Value> handleSensitiveValues(Map<String, Serializable> executionResult,
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
                                                         Map<String, Value> callArguments, boolean useJython) {
        if (StringUtils.isNotBlank(pythonScript)) {
            final Map<String, Value> returnedMap = scriptExecutor.executeScript(
                    normalizePythonDependencies(dependencies), pythonScript, callArguments, useJython);

            final Value ex = returnedMap.get(EXCEPTION);
            if (ex != null) {
                logger.error("Python operation encountered an exception: " + ex.toString());
            }

            return returnedMap;
        }

        throw new RuntimeException("Python script not found in action data");
    }

}
