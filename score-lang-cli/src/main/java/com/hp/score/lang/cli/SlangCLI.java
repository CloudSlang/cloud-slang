package com.hp.score.lang.cli;

/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/


import com.hp.score.events.EventConstants;
import com.hp.score.events.ScoreEvent;
import com.hp.score.events.ScoreEventListener;
import com.hp.score.lang.cli.services.ScoreServices;
import com.hp.score.lang.cli.utils.CompilerHelper;
import com.hp.score.lang.entities.CompilationArtifact;
import com.hp.score.lang.entities.bindings.Input;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

import static com.hp.score.lang.entities.ScoreLangConstants.*;

/**
 * Date: 11/7/2014
 *
 * @author lesant
 */

@Component
public class SlangCLI implements CommandMarker {

    public static final String TRIGGERED_FLOW_MSG = "Triggered flow : ";
    public static final String WITH_EXECUTION_ID_MSG = " , with execution id : ";
    public static final String FLOW_EXECUTION_TIME_TOOK = "Flow execution time took  ";

    @Autowired
    private ScoreServices scoreServices;

    @Autowired
    private CompilerHelper compilerHelper;

    /**
     * This global param holds the state of the CLI, if flows need to run in ASYNC or in SYNC manner.
     */
    private Boolean triggerAsync = false ;

    private final static Logger logger = Logger.getLogger(SlangCLI.class);

    private static final String currently = "You are currently running Score version: ";
    private static final String SCORE_VERSION = "0.1.229"; //todo get version

    @CliCommand(value = "run", help = "triggers a slang flow")
    public String run(
            @CliOption(key = "f", mandatory = true, help = "Path to filename. e.g. slang run --f C:\\Slang\\flow.yaml") final String filePath,
            @CliOption(key = "cp", mandatory = false,
                    help = "Classpath , a file directory to flow dependencies, by default it will take flow file dir") final String classPath,
            //@CliOption(key = "sp", mandatory = false, help = "System property file location") final String systemProperty,//not supported for now...
            @CliOption(key = "D", mandatory = false, help = "inputs in a key=value comma separated list") final Map<String, String> inputs) throws IOException {


        CompilationArtifact compilationArtifact = compilerHelper.compile(filePath,null,classPath);

        Long id ;
        if(!triggerAsync){
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            id = scoreServices.triggerSync(compilationArtifact, inputs);
            stopWatch.stop();
            return FLOW_EXECUTION_TIME_TOOK +stopWatch.toString() + WITH_EXECUTION_ID_MSG + id ;
        }
        id = scoreServices.trigger(compilationArtifact, inputs);
        return triggerMsg(id,compilationArtifact.getExecutionPlan().getName());
    }

    @CliCommand(value = "env", help = "Set environment var relevant to the CLI")
    public String setEnvVar(
            @CliOption(key = "setAsync", mandatory = true, help = "set the async") final boolean switchAsync) throws IOException {
        triggerAsync = switchAsync ;
        return "flow execution ASYNC execution was changed to : " + triggerAsync;
    }

    @CliCommand(value = "inputs", help = "Get flow inputs")
    public List<String> getFlowInputs(
            @CliOption(key = "f", mandatory = true, help = "Path to filename. e.g. slang run --f C:\\Slang\\flow.yaml") final String filePath,
            @CliOption(key = "cp", mandatory = false,
                    help = "Classpath, a file directory to flow dependencies, by default it will take flow file dir") final String classPath)
            throws IOException {
        CompilationArtifact compilationArtifact = compilerHelper.compile(filePath,null,classPath);
        List<Input> inputs = compilationArtifact.getInputs();
        List<String> inputsResult = new ArrayList<>();
        for(Input input:inputs){
            if(!input.isOverride()){
                inputsResult.add(input.getName());
            }
        }
        return inputsResult;
    }

    private String triggerMsg(Long id, String flowName) {
        return TRIGGERED_FLOW_MSG + flowName + WITH_EXECUTION_ID_MSG + id;
    }

    @CliCommand(value = "slang -version", help = "Prints the score version used")
    public String version() {
        return currently + SCORE_VERSION;
    }

    public static String getVersion() {
        return SCORE_VERSION;
    }

    @PostConstruct
    private void registerEventHandlers() {
        Set<String> handlerTypes = new HashSet<>();
        handlerTypes.add(EventConstants.SCORE_FINISHED_EVENT);
        handlerTypes.add(EventConstants.SCORE_ERROR_EVENT);
        handlerTypes.add(EventConstants.SCORE_FAILURE_EVENT);
        handlerTypes.add(EVENT_ACTION_START);
        handlerTypes.add(EVENT_ACTION_END);
        handlerTypes.add(EVENT_ACTION_ERROR);
        handlerTypes.add(EVENT_INPUT_START);
        handlerTypes.add(EVENT_INPUT_END);
        handlerTypes.add(EVENT_OUTPUT_START);
        handlerTypes.add(EVENT_OUTPUT_END);
        handlerTypes.add(EVENT_STEP_ERROR);
        handlerTypes.add(EVENT_EXECUTION_FINISHED);
        scoreServices.subscribe(new ScoreEventListener() {
            @Override
            public void onEvent(ScoreEvent event) {
                logEvent(event);
            }
        }, handlerTypes);
    }

    private void logEvent(ScoreEvent event) {
        logger.info(("Event received: " + event.getEventType() + " Data is: " + event.getData()));
    }

}
