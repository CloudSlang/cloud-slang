/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.cli;

import com.google.common.collect.Lists;
import io.cloudslang.lang.cli.services.ScoreServices;
import io.cloudslang.lang.cli.utils.CompilerHelper;
import io.cloudslang.lang.cli.utils.MetadataHelper;
import io.cloudslang.lang.compiler.modeller.result.CompilationModellingResult;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.score.events.EventConstants;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.events.ScoreEventListener;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lesant
 * @version $Id$
 * @since 11/07/2014
 */
@Component
public class SlangCli implements CommandMarker {

    private static final Logger logger = Logger.getLogger(SlangCli.class);

    private static final String TRIGGERED_FLOW_MSG = "Triggered flow : ";
    private static final String WITH_EXECUTION_ID_MSG = "Execution id: ";
    private static final String FLOW_EXECUTION_TIME_TOOK = ", duration: ";
    private static final String CURRENTLY = "You are CURRENTLY running CloudSlang version: ";
    private static final String RUN_HELP = "triggers a CloudSlang flow";
    private static final String FILE_HELP = "Path to filename. e.g. run --f c:/.../your_flow.sl";
    private static final String CLASSPATH_HELP = "Classpath, a directory comma separated list to flow dependencies, " +
            "by default it will take flow file dir. " +
            "e.g. run --f c:/.../your_flow.sl --i input1=root,input2=25 --cp c:/.../yaml";
    private static final String INPUTS_HELP = "inputs in a key=value comma separated list. " +
            "e.g. run --f c:/.../your_flow.sl --i input1=root,input2=25";
    private static final String INPUT_FILE_HELP = "comma separated list of input file locations. " +
            "e.g. run --f C:/.../your_flow.sl --if C:/.../inputs.yaml";
    private static final String SYSTEM_PROPERTY_FILE_HELP = "comma separated list of system property file locations. " +
            "e.g. run --f c:/.../your_flow.sl --spf c:/.../yaml";
    private static final String ENV_HELP = "Set environment var relevant to the CLI";
    private static final String SET_ASYNC_HELP = "set the async. e.g. env --setAsync true";
    private static final String CSLANG_VERSION_HELP = "Prints the CloudSlang version used";
    private static final String INPUTS_COMMAND_HELP = "Get flow inputs";
    private static final String PATH_TO_FILENAME_HELP = "Path to filename. e.g. /path/to/file.sl";
    private static final String QUIET = "quiet";
    private static final String DEBUG = "debug";
    private static final String DEFAULT = "default";

    @Autowired
    private ScoreServices scoreServices;

    @Autowired
    private CompilerHelper compilerHelper;

    @Autowired
    private MetadataHelper metadataHelper;

    @org.springframework.beans.factory.annotation.Value("${slang.version}")
    private String slangVersion;

    /**
     * This global param holds the state of the CLI, if flows need to run in ASYNC or in SYNC manner.
     */
    private Boolean triggerAsync = false;

    @CliCommand(value = "run", help = RUN_HELP)
    public String run(
            @CliOption(key = {"", "f", "file"}, mandatory = true, help = FILE_HELP) final File file,
            @CliOption(key = {"cp", "classpath"}, mandatory = false, help = CLASSPATH_HELP)
            final List<String> classPath,
            @CliOption(key = {"i", "inputs"}, mandatory = false, help = INPUTS_HELP)
            final Map<String, ? extends Serializable> inputs,
            @CliOption(key = {"if", "input-file"}, mandatory = false, help = INPUT_FILE_HELP)
            final List<String> inputFiles,
            @CliOption(key = {"v", "verbose"}, mandatory = false,
                    help = "default, quiet, debug(print each step outputs). e.g. run --f c:/.../your_flow.sl --v quiet",
                    specifiedDefaultValue = "debug", unspecifiedDefaultValue = "default") final String verbose,
            @CliOption(key = {"spf", "system-property-file"}, mandatory = false, help = SYSTEM_PROPERTY_FILE_HELP)
            final List<String> systemPropertyFiles) {

        if (invalidVerboseInput(verbose)) {
            throw new IllegalArgumentException("Verbose argument is invalid.");
        }

        CompilationArtifact compilationArtifact = compilerHelper.compile(file.getAbsolutePath(), classPath);
        Set<SystemProperty> systemProperties = compilerHelper.loadSystemProperties(systemPropertyFiles);
        Map<String, Value> inputsFromFile = compilerHelper.loadInputsFromFile(inputFiles);
        Map<String, Value> mergedInputs = new HashMap<>();

        if (MapUtils.isNotEmpty(inputsFromFile)) {
            mergedInputs.putAll(inputsFromFile);
        }
        if (MapUtils.isNotEmpty(inputs)) {
            mergedInputs.putAll(io.cloudslang.lang.entities.utils.MapUtils.convertMapNonSensitiveValues(inputs));
        }
        boolean quiet = QUIET.equalsIgnoreCase(verbose);
        boolean debug = DEBUG.equalsIgnoreCase(verbose);

        Long id;
        if (!triggerAsync) {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            id = scoreServices.triggerSync(compilationArtifact, mergedInputs, systemProperties, quiet, debug);
            stopWatch.stop();
            return quiet ? StringUtils.EMPTY : triggerSyncMsg(id, stopWatch.toString());
        }
        id = scoreServices.trigger(compilationArtifact, mergedInputs, systemProperties);
        return quiet ? StringUtils.EMPTY : triggerAsyncMsg(id, compilationArtifact.getExecutionPlan().getName());
    }

    private boolean invalidVerboseInput(String verbose) {
        String[] validArguments = {DEFAULT, QUIET, DEBUG};
        return !Arrays.asList(validArguments).contains(verbose.toLowerCase());
    }

    @CliCommand(value = "compile", help = "Display compile errors for an executable")
    public String compileSource(
            @CliOption(key = {"", "d", "directory"}, mandatory = false,
                    help = "Path to directory. e.g. compile --d c:/.../your_directory")
            final List<String> directories,
            @CliOption(key = {"", "f", "file"}, mandatory = false,
                    help = "Path to filename. e.g. compile --f c:/.../your_flow.sl") final File file,
            @CliOption(key = {"cp", "classpath"}, mandatory = false, help = CLASSPATH_HELP)
            final List<String> classPath
    ) {
        if (directories != null) {
            List<CompilationModellingResult> results = compilerHelper.compileFolders(directories);
            return printAllCompileErrors(results);
        } else if (file != null) {
            CompilationModellingResult result = compilerHelper.compileSource(file.getAbsolutePath(), classPath);
            return printCompileErrors(result.getErrors(), file, new StringBuilder());
        } else {
            throw new IllegalArgumentException("You should specify directory(otherwise known as option 'd') " +
                    "or file(otherwise known as option 'f').");
        }
    }

    private String printAllCompileErrors(List<CompilationModellingResult> results) {
        if (results.size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (CompilationModellingResult result : results) {
                printCompileErrors(result.getErrors(), result.getFile(), stringBuilder);
                stringBuilder.append(System.lineSeparator());
            }
            return stringBuilder.toString();
        } else {
            return "No files were found to compile.";
        }
    }

    private String printCompileErrors(List<RuntimeException> exceptions, File file, StringBuilder stringBuilder) {
        if (exceptions.size() > 0) {
            stringBuilder.append("Following exceptions were found:").append(System.lineSeparator());
            for (RuntimeException exception : exceptions) {
                stringBuilder.append("\t");
                stringBuilder.append(exception.getClass());
                stringBuilder.append(": ");
                stringBuilder.append(exception.getMessage());
                stringBuilder.append(System.lineSeparator());
            }
            throw new RuntimeException(stringBuilder.toString());
        } else {
            stringBuilder.append("Compilation was successful for ").append(file.getName());
        }
        return StringUtils.trim(stringBuilder.toString());
    }

    @CliCommand(value = "inspect", help = "Display metadata about an executable")
    public String inspectExecutable(
            @CliOption(key = {"", "f", "file"}, mandatory = true, help = PATH_TO_FILENAME_HELP)
            final File executableFile
    ) {
        return metadataHelper.extractMetadata(executableFile);
    }

    @CliCommand(value = "list", help = "List system properties from a properties file")
    public String listSystemProperties(
            @CliOption(key = {"", "f", "file"}, mandatory = true, help = PATH_TO_FILENAME_HELP)
            final String propertiesFile) {
        Set<SystemProperty> systemProperties = compilerHelper.loadSystemProperties(Lists.newArrayList(propertiesFile));
        return prettyPrintSystemProperties(systemProperties);
    }

    private String prettyPrintSystemProperties(Set<SystemProperty> systemProperties) {
        StringBuilder stringBuilder = new StringBuilder();
        if (CollectionUtils.isEmpty(systemProperties)) {
            stringBuilder.append("No system properties found.");
        } else {
            stringBuilder.append("Following system properties were loaded:").append(System.lineSeparator());
            for (SystemProperty systemProperty : systemProperties) {
                stringBuilder.append("\t");
                stringBuilder.append(systemProperty.getFullyQualifiedName());
                stringBuilder.append(": ");
                stringBuilder.append(systemProperty.getValue());
                stringBuilder.append(System.lineSeparator());
            }
        }
        return StringUtils.trim(stringBuilder.toString());
    }

    @CliCommand(value = "env", help = ENV_HELP)
    public String setEnvVar(
            @CliOption(key = "setAsync", mandatory = true, help = SET_ASYNC_HELP) final boolean switchAsync) {
        triggerAsync = switchAsync;
        return setEnvMessage(triggerAsync);
    }

    @CliCommand(value = "inputs", help = INPUTS_COMMAND_HELP)
    public List<String> getFlowInputs(
            @CliOption(key = {"", "f", "file"}, mandatory = true, help = FILE_HELP) final File file,
            @CliOption(key = {"cp", "classpath"}, mandatory = false, help = CLASSPATH_HELP)
            final List<String> classPath) {
        CompilationArtifact compilationArtifact = compilerHelper.compile(file.getAbsolutePath(), classPath);
        List<Input> inputs = compilationArtifact.getInputs();
        List<String> inputsResult = new ArrayList<>();
        for (Input input : inputs) {
            if (!input.isPrivateInput()) {
                inputsResult.add(input.getName());
            }
        }
        return inputsResult;
    }

    @CliCommand(value = "cslang --version", help = CSLANG_VERSION_HELP)
    public String version() {
        return CURRENTLY + slangVersion;
    }

    public static String triggerSyncMsg(Long id, String duration) {
        return WITH_EXECUTION_ID_MSG + id + FLOW_EXECUTION_TIME_TOOK + duration;
    }

    public static String triggerAsyncMsg(Long id, String flowName) {
        return TRIGGERED_FLOW_MSG + flowName + WITH_EXECUTION_ID_MSG + id;
    }

    public static String setEnvMessage(boolean triggerAsync) {
        return "flow execution ASYNC execution was changed to : " + triggerAsync;
    }

    @PostConstruct
    private void registerEventHandlers() {
        Set<String> slangHandlerTypes = new HashSet<>();
        slangHandlerTypes.add(ScoreLangConstants.EVENT_ACTION_START);
        slangHandlerTypes.add(ScoreLangConstants.EVENT_ACTION_END);
        slangHandlerTypes.add(ScoreLangConstants.EVENT_ACTION_ERROR);
        slangHandlerTypes.add(ScoreLangConstants.EVENT_STEP_START);
        slangHandlerTypes.add(ScoreLangConstants.EVENT_INPUT_START);
        slangHandlerTypes.add(ScoreLangConstants.EVENT_INPUT_END);
        slangHandlerTypes.add(ScoreLangConstants.EVENT_ARGUMENT_START);
        slangHandlerTypes.add(ScoreLangConstants.EVENT_ARGUMENT_END);
        slangHandlerTypes.add(ScoreLangConstants.EVENT_OUTPUT_START);
        slangHandlerTypes.add(ScoreLangConstants.EVENT_OUTPUT_END);
        slangHandlerTypes.add(ScoreLangConstants.EVENT_BRANCH_START);
        slangHandlerTypes.add(ScoreLangConstants.EVENT_BRANCH_END);
        slangHandlerTypes.add(ScoreLangConstants.EVENT_SPLIT_BRANCHES);
        slangHandlerTypes.add(ScoreLangConstants.EVENT_JOIN_BRANCHES_START);
        slangHandlerTypes.add(ScoreLangConstants.EVENT_JOIN_BRANCHES_END);
        slangHandlerTypes.add(ScoreLangConstants.SLANG_EXECUTION_EXCEPTION);
        slangHandlerTypes.add(ScoreLangConstants.EVENT_EXECUTION_FINISHED);

        Set<String> scoreHandlerTypes = new HashSet<>();
        scoreHandlerTypes.add(EventConstants.SCORE_FINISHED_EVENT);
        scoreHandlerTypes.add(EventConstants.SCORE_ERROR_EVENT);
        scoreHandlerTypes.add(EventConstants.SCORE_FAILURE_EVENT);

        scoreServices.subscribe(new ScoreEventListener() {
            @Override
            public void onEvent(ScoreEvent event) {
                logSlangEvent(event);
            }
        }, slangHandlerTypes);
        scoreServices.subscribe(new ScoreEventListener() {
            @Override
            public void onEvent(ScoreEvent event) {
                logScoreEvent(event);
            }
        }, scoreHandlerTypes);
    }

    private void logSlangEvent(ScoreEvent event) {
        LanguageEventData eventData = (LanguageEventData) event.getData();
        logger.info(("[ " + eventData.getPath() + " - " + eventData.getStepName() + " ] " +
                event.getEventType() + " - Inputs: " + eventData.getInputs() + ", Outputs: " + eventData.getOutputs() +
                ", Result: " + eventData.getResult() + ", Raw Data: " + event.getData()));
    }

    private void logScoreEvent(ScoreEvent event) {
        logger.info((event.getEventType() + " - " + event.getData()));
    }

}
