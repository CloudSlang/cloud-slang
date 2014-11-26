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
package com.hp.score.lang.cli.services;

import com.hp.score.api.Score;
import com.hp.score.api.TriggeringProperties;
import com.hp.score.events.EventBus;
import com.hp.score.events.EventConstants;
import com.hp.score.events.ScoreEvent;
import com.hp.score.events.ScoreEventListener;
import com.hp.score.lang.entities.CompilationArtifact;
import com.hp.score.lang.entities.ScoreLangConstants;
import com.hp.score.lang.runtime.env.RunEnvironment;
import com.hp.score.lang.runtime.events.LanguageEventData;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_EXECUTION_FINISHED;
import static com.hp.score.lang.entities.ScoreLangConstants.SLANG_EXECUTION_EXCEPTION;
import static com.hp.score.lang.runtime.events.LanguageEventData.EXCEPTION;
import static com.hp.score.lang.runtime.events.LanguageEventData.RESULT;
import static org.fusesource.jansi.Ansi.ansi;

/**
 * Date: 11/13/2014
 *
 * @author Bonczidai Levente
 */
@Service
public class ScoreServices {
    public static final String SLANG_STEP_ERROR_MSG = "Slang Error : ";
    public static final String SCORE_ERROR_EVENT_MSG = "Score Error Event :";
    public static final String FLOW_FINISHED_WITH_FAILURE_MSG = "Flow finished with failure";
    //TODO - change this to interface...

    @Autowired
    private Score score;

    @Autowired
    private EventBus eventBus;

    public void subscribe(ScoreEventListener eventHandler, Set<String> eventTypes) {
        eventBus.subscribe(eventHandler, eventTypes);
    }

    /**
     * This method will trigger the flow in an Async matter.
     * @param compilationArtifact
     * @param inputs : flow inputs
     * @return executionId
     */
    public Long trigger(CompilationArtifact compilationArtifact, Map<String, String> inputs) {
        Map<String, Serializable> executionContext = new HashMap<>();
        executionContext.put(ScoreLangConstants.RUN_ENV, new RunEnvironment());
        executionContext.put(ScoreLangConstants.USER_INPUTS_KEY, (Serializable) inputs);

        TriggeringProperties triggeringProperties = TriggeringProperties
                .create(compilationArtifact.getExecutionPlan())
                .setDependencies(compilationArtifact.getDependencies())
                .setContext(executionContext);

        return score.trigger(triggeringProperties);
    }

    /**
     * This method will trigger the flow in a synchronize matter, meaning only one flow can run at a time.
     * @param compilationArtifact
     * @param inputs : flow inputs
     * @return executionId
     */
    public Long triggerSync(CompilationArtifact compilationArtifact, Map<String, String> inputs){
        //add start event
        Set<String> handlerTypes = new HashSet<>();
        handlerTypes.add(EventConstants.SCORE_FINISHED_EVENT);
        handlerTypes.add(EventConstants.SCORE_ERROR_EVENT);
        handlerTypes.add(EventConstants.SCORE_FAILURE_EVENT);
        handlerTypes.add(SLANG_EXECUTION_EXCEPTION);
        handlerTypes.add(EVENT_EXECUTION_FINISHED);

        SyncTriggerEventListener scoreEventListener = new SyncTriggerEventListener();
        eventBus.subscribe(scoreEventListener, handlerTypes);

        Long executionId = trigger(compilationArtifact, inputs);

        while(!scoreEventListener.isFlowFinished()){}//todo : need to add here sleep?

        eventBus.unsubscribe(scoreEventListener);

        return executionId;

    }

    private class SyncTriggerEventListener implements ScoreEventListener{

        private AtomicBoolean flowFinished = new AtomicBoolean(false);

        public boolean isFlowFinished() {
            return flowFinished.get();
        }

        @Override
        public synchronized void onEvent(ScoreEvent scoreEvent) throws InterruptedException {
            Map<String,Serializable> data = (Map<String,Serializable>)scoreEvent.getData();
            switch (scoreEvent.getEventType()){
                case EventConstants.SCORE_FINISHED_EVENT :
                    flowFinished.set(true); break;
                case EventConstants.SCORE_ERROR_EVENT :
                    printWithColor(Ansi.Color.RED, SCORE_ERROR_EVENT_MSG + data.get(EventConstants.SCORE_ERROR_LOG_MSG) + " , " +
                            data.get(EventConstants.SCORE_ERROR_MSG)); break;
                case EventConstants.SCORE_FAILURE_EVENT :
                    printWithColor(Ansi.Color.RED,FLOW_FINISHED_WITH_FAILURE_MSG);
                    flowFinished.set(true); break;
                case ScoreLangConstants.SLANG_EXECUTION_EXCEPTION:
                    printWithColor(Ansi.Color.RED,SLANG_STEP_ERROR_MSG + data.get(EXCEPTION)); break;
                case EVENT_EXECUTION_FINISHED :
                    printFinishEvent(data); break;
            }
        }

        private void printFinishEvent(Map<String, Serializable> data) {
            String flowResult = (String)data.get(RESULT);
            String flowName = (String)data.get(LanguageEventData.levelName.EXECUTABLE_NAME.toString());
            printWithColor(Ansi.Color.CYAN,"Flow : " + flowName + " finished with result : " + flowResult);
        }

        private void printWithColor(Ansi.Color color, String msg){
            AnsiConsole.out().print(ansi().ansi().fg(color).a(msg).newline());
        }
    }


}
