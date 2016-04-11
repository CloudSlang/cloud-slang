/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.systemtests;

import com.google.common.collect.Sets;
import io.cloudslang.lang.api.Slang;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.events.ScoreEventListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TriggerFlows {

    private final static HashSet<String> FINISHED_EVENTS =
            Sets.newHashSet(ScoreLangConstants.EVENT_EXECUTION_FINISHED, ScoreLangConstants.SLANG_EXECUTION_EXCEPTION);

    private final static HashSet<String> STEP_EVENTS =
            Sets.newHashSet(
                    ScoreLangConstants.EVENT_INPUT_END,
                    ScoreLangConstants.EVENT_OUTPUT_END,
                    ScoreLangConstants.EVENT_ARGUMENT_START,
                    ScoreLangConstants.EVENT_ARGUMENT_END
            );

    private final static HashSet<String> BRANCH_EVENTS = Sets.newHashSet(ScoreLangConstants.EVENT_BRANCH_END);

    private final static HashSet<String> ASYNC_LOOP_EVENTS = Sets.newHashSet(ScoreLangConstants.EVENT_JOIN_BRANCHES_END);

    @Autowired
    private Slang slang;

    public ScoreEvent runSync(
            CompilationArtifact compilationArtifact,
            Map<String, ? extends Serializable> userInputs,
            Set<SystemProperty> systemProperties) {
        final BlockingQueue<ScoreEvent> finishEvent = new LinkedBlockingQueue<>();
        ScoreEventListener finishListener = new ScoreEventListener() {
            @Override
            public synchronized void onEvent(ScoreEvent event) throws InterruptedException {
                finishEvent.add(event);
            }
        };
        slang.subscribeOnEvents(finishListener, FINISHED_EVENTS);

        slang.run(compilationArtifact, userInputs, systemProperties);

        try {
            ScoreEvent event = finishEvent.take();
            if (event.getEventType().equals(ScoreLangConstants.SLANG_EXECUTION_EXCEPTION)){
                LanguageEventData languageEvent = (LanguageEventData) event.getData();
                throw new RuntimeException(languageEvent.getException());
            }
            slang.unSubscribeOnEvents(finishListener);
            return event;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public RuntimeInformation runWithData(CompilationArtifact compilationArtifact, Map<String, ? extends Serializable> userInputs, Set<SystemProperty> systemProperties) {
        RunDataAggregatorListener runDataAggregatorListener = new RunDataAggregatorListener();
        slang.subscribeOnEvents(runDataAggregatorListener, STEP_EVENTS);

        BranchAggregatorListener branchAggregatorListener = new BranchAggregatorListener();
        slang.subscribeOnEvents(branchAggregatorListener, BRANCH_EVENTS);

        JoinAggregatorListener joinAggregatorListener = new JoinAggregatorListener();
        slang.subscribeOnEvents(joinAggregatorListener, ASYNC_LOOP_EVENTS);

        try {
            Thread.sleep(2000L);      /* TODO : remove this! only to test unstable navigation tests*/
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        runSync(compilationArtifact, userInputs, systemProperties);

        try {
            Thread.sleep(2000L);     /* TODO : remove this! only to test unstable navigation tests*/
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Map<String, StepData> steps = runDataAggregatorListener.aggregate();
        Map<String, List<StepData>> branchesByPath = branchAggregatorListener.aggregate();
        Map<String, StepData> asyncSteps = joinAggregatorListener.aggregate();

        RuntimeInformation runtimeInformation = new RuntimeInformation(steps, branchesByPath, asyncSteps);

        slang.unSubscribeOnEvents(joinAggregatorListener);
        slang.unSubscribeOnEvents(branchAggregatorListener);
        slang.unSubscribeOnEvents(runDataAggregatorListener);

        return runtimeInformation;
    }

}
