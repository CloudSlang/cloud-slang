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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_STEP_ERROR;

/**
 * Date: 11/13/2014
 *
 * @author Bonczidai Levente
 */
@Service
public class ScoreServices {
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
        handlerTypes.add(EVENT_STEP_ERROR);

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
        public void onEvent(ScoreEvent scoreEvent) throws InterruptedException {
            switch (scoreEvent.getEventType()){
                case EventConstants.SCORE_FINISHED_EVENT :
                    System.out.println("Flow finished");//todo - improve msg here
                    flowFinished.set(true); break;
                case EventConstants.SCORE_ERROR_EVENT :
                    System.out.println("Score Error Event"); break; //todo - improve msg here
                case EventConstants.SCORE_FAILURE_EVENT :
                    System.out.println("Flow finished with failure"); //todo - improve msg here
                    flowFinished.set(true); break;
                case ScoreLangConstants.EVENT_STEP_ERROR :
                    System.out.println("Slang Step Error");break;
            }
        }
    }


}
