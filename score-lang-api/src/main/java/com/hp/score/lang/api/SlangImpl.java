package com.hp.score.lang.api;
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

import com.hp.score.lang.compiler.SlangCompiler;
import com.hp.score.lang.entities.CompilationArtifact;
import com.hp.score.lang.entities.ScoreLangConstants;
import com.hp.score.lang.runtime.env.RunEnvironment;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.eclipse.score.api.Score;
import org.eclipse.score.api.TriggeringProperties;
import org.eclipse.score.events.EventBus;
import org.eclipse.score.events.EventConstants;
import org.eclipse.score.events.ScoreEventListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_ACTION_END;
import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_ACTION_ERROR;
import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_ACTION_START;
import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_EXECUTION_FINISHED;
import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_INPUT_END;
import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_INPUT_START;
import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_OUTPUT_END;
import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_OUTPUT_START;
import static com.hp.score.lang.entities.ScoreLangConstants.SLANG_EXECUTION_EXCEPTION;

/**
 * User: stoneo
 * Date: 03/12/2014
 * Time: 15:20
 */
public class SlangImpl implements Slang{


    @Autowired
    private SlangCompiler compiler;

    @Autowired
    private Score score;

    @Autowired
    private EventBus eventBus;

    private final static Logger logger = Logger.getLogger(SlangImpl.class);

    @Override
    public CompilationArtifact compileOperation(File file, String operationName, Set<File> dependencies) {

        Validate.notNull(file, "File can not be null");

        Validate.isTrue(file.isFile(), "File must lead to a file");

        Set<File> dependencyFiles = new HashSet<>();
        if (dependencies != null) {
            for(File dependency : dependencies) {
                Validate.isTrue(dependency.isFile(), "dependency must lead to a file");
                dependencyFiles.add(dependency);
            }
        }
        try {
            return compiler.compile(file, operationName, dependencyFiles);
        } catch (Exception e) {
            logger.error("Failed compilation for file : " + file.getName() + " ,Exception is : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompilationArtifact compile(File file, Set<File> dependencies) {
        return compileOperation(file, null, dependencies);
    }

    @Override
    public Long run(CompilationArtifact compilationArtifact, Map<String, Serializable> runInputs) {
        Validate.notNull(compilationArtifact, "Compilation artifact can not be null");
        if(runInputs == null){
            runInputs = new HashMap<>();
        }
        Map<String, Serializable> executionContext = new HashMap<>();
        executionContext.put(ScoreLangConstants.RUN_ENV, new RunEnvironment());
        executionContext.put(ScoreLangConstants.USER_INPUTS_KEY, (Serializable) runInputs);

        TriggeringProperties triggeringProperties = TriggeringProperties
                .create(compilationArtifact.getExecutionPlan())
                .setDependencies(compilationArtifact.getDependencies())
                .setContext(executionContext);

        return score.trigger(triggeringProperties);
    }

    @Override
    public Long compileAndRunOperation(File file, String operationName, Set<File> dependencies, Map<String, Serializable> runInputs) {
        CompilationArtifact compilationArtifact = compileOperation(file, operationName, dependencies);
        return run(compilationArtifact, runInputs);
    }

    @Override
    public Long compileAndRun(File file, Set<File> dependencies, Map<String, Serializable> runInputs) {
        return compileAndRunOperation(file, null, dependencies, runInputs);
    }

    @Override
    public void subscribeOnEvents(ScoreEventListener eventListener, Set<String> eventTypes) {
        eventBus.subscribe(eventListener, eventTypes);
    }

    @Override
    public void unSubscribeOnEvents(ScoreEventListener eventListener) {
        eventBus.unsubscribe(eventListener);
    }

    @Override
    public void subscribeOnAllEvents(ScoreEventListener eventListener) {
        subscribeOnEvents(eventListener, getAllEventTypes());
    }

    private Set<String> getAllEventTypes() {
        Set<String> eventTypes = new HashSet<>();
        eventTypes.add(EventConstants.SCORE_FINISHED_EVENT);
        eventTypes.add(EventConstants.SCORE_BRANCH_FAILURE_EVENT);
        eventTypes.add(EventConstants.SCORE_FINISHED_BRANCH_EVENT);
        eventTypes.add(EventConstants.SCORE_NO_WORKER_FAILURE_EVENT);
        eventTypes.add(EventConstants.SCORE_PAUSED_EVENT);
        eventTypes.add(EventConstants.SCORE_ERROR_EVENT);
        eventTypes.add(EventConstants.SCORE_FAILURE_EVENT);
        eventTypes.add(SLANG_EXECUTION_EXCEPTION);
        eventTypes.add(EVENT_ACTION_START);
        eventTypes.add(EVENT_ACTION_END);
        eventTypes.add(EVENT_ACTION_ERROR);
        eventTypes.add(EVENT_INPUT_START);
        eventTypes.add(EVENT_INPUT_END);
        eventTypes.add(EVENT_OUTPUT_START);
        eventTypes.add(EVENT_OUTPUT_END);
        eventTypes.add(EVENT_EXECUTION_FINISHED);
        return eventTypes;
    }
}
