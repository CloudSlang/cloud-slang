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
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.google.common.collect.Sets.newHashSet;
import static org.openscore.events.EventConstants.*;
import static org.openscore.lang.entities.ScoreLangConstants.*;

public class TriggerFlows {

    private final static HashSet<String> FINISHED_EVENT =
            newHashSet(SCORE_FINISHED_EVENT, SCORE_FAILURE_EVENT, SLANG_EXECUTION_EXCEPTION);

    private final static HashSet<String> TASK_EVENTS =
            newHashSet(EVENT_INPUT_END, EVENT_OUTPUT_END);

    @Autowired
    private Slang slang;

    private BlockingQueue<ScoreEvent> finishEvent;

    public ScoreEvent runSync(CompilationArtifact compilationArtifact, Map<String, Serializable> userInputs) {
        finishEvent = new LinkedBlockingQueue<>();
        ScoreEventListener finishListener = new ScoreEventListener() {
            @Override
            public void onEvent(ScoreEvent event) throws InterruptedException {
                finishEvent.add(event);
            }
        };
        slang.subscribeOnEvents(finishListener, FINISHED_EVENT);

        slang.run(compilationArtifact, userInputs);

        try {
            ScoreEvent event = finishEvent.take();
            slang.unSubscribeOnEvents(finishListener);
            return event;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, PathData> run(CompilationArtifact compilationArtifact, Map<String, Serializable> userInputs) {
        RunDataAggregatorListener listener = new RunDataAggregatorListener();
        slang.subscribeOnEvents(listener, TASK_EVENTS);

        runSync(compilationArtifact, userInputs);

        //Damn ugly workaround for events sync issues.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

        Map<String, PathData> tasks = listener.aggregate();

        slang.unSubscribeOnEvents(listener);

        return tasks;
    }

}
