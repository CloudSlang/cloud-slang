/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.openscore.lang.systemtests;

import org.openscore.events.ScoreEvent;
import org.openscore.events.ScoreEventListener;
import org.openscore.lang.api.Slang;
import org.openscore.lang.entities.CompilationArtifact;
import org.openscore.lang.runtime.events.LanguageEventData;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.google.common.collect.Sets.newHashSet;
import static org.openscore.lang.entities.ScoreLangConstants.*;

public class TriggerFlows {

    private final static HashSet<String> FINISHED_EVENT =
            newHashSet(EVENT_EXECUTION_FINISHED, SLANG_EXECUTION_EXCEPTION);

    private final static HashSet<String> STEP_EVENTS =
            newHashSet(EVENT_INPUT_END, EVENT_OUTPUT_END);

    @Autowired
    private Slang slang;

    public ScoreEvent runSync(CompilationArtifact compilationArtifact, Map<String, ? extends Serializable> userInputs, Map<String, ? extends Serializable> systemProperties) {
        final BlockingQueue<ScoreEvent> finishEvent = new LinkedBlockingQueue<>();
        ScoreEventListener finishListener = new ScoreEventListener() {
            @Override
            public void onEvent(ScoreEvent event) throws InterruptedException {
                finishEvent.add(event);
            }
        };
        slang.subscribeOnEvents(finishListener, FINISHED_EVENT);

        slang.run(compilationArtifact, userInputs, systemProperties);

        try {
            ScoreEvent event = finishEvent.take();
            if (event.getEventType().equals(SLANG_EXECUTION_EXCEPTION)){
                LanguageEventData languageEvent = (LanguageEventData) event.getData();
                throw new RuntimeException((String) languageEvent.get(LanguageEventData.EXCEPTION));
            }
            slang.unSubscribeOnEvents(finishListener);
            return event;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, StepData> runWithData(CompilationArtifact compilationArtifact, Map<String, ? extends Serializable> userInputs, Map<String, ? extends Serializable> systemProperties) {
        RunDataAggregatorListener listener = new RunDataAggregatorListener();
        slang.subscribeOnEvents(listener, STEP_EVENTS);

        runSync(compilationArtifact, userInputs, systemProperties);

        Map<String, StepData> tasks = listener.aggregate();

        slang.unSubscribeOnEvents(listener);

        return tasks;
    }

}
