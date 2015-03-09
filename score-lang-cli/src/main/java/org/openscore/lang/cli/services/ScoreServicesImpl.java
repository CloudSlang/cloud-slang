/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.openscore.lang.cli.services;

import org.apache.commons.lang.StringUtils;
import org.openscore.events.EventConstants;
import org.openscore.events.ScoreEventListener;
import org.openscore.lang.api.Slang;
import org.openscore.lang.entities.CompilationArtifact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.openscore.lang.entities.ScoreLangConstants.*;

/**
 * @author Bonczidai Levente
 * @since 11/13/2014
 * @version $Id$
 */
@Service
public class ScoreServicesImpl implements ScoreServices{

    @Autowired
    private Slang slang;

    public void subscribe(ScoreEventListener eventHandler, Set<String> eventTypes) {
        slang.subscribeOnEvents(eventHandler, eventTypes);
    }

    /**
     * This method will trigger the flow in an Async matter.
     * @param compilationArtifact the artifact to trigger
     * @param inputs : flow inputs
     * @return executionId
     */
    @Override
	public Long trigger(CompilationArtifact compilationArtifact, Map<String, ? extends Serializable> inputs, Map<String, ? extends Serializable> systemProperties) {
        return slang.run(compilationArtifact, inputs, systemProperties);
    }

    /**
     * This method will trigger the flow in a synchronize matter, meaning only one flow can run at a time.
     * @param compilationArtifact the artifact to trigger
     * @param inputs : flow inputs
     * @return executionId
     */
    @Override
    public Long triggerSync(CompilationArtifact compilationArtifact, Map<String, ? extends Serializable> inputs, Map<String, ? extends Serializable> systemProperties, boolean isQuiet){
        //add start event
        Set<String> handlerTypes = new HashSet<>();
        if(isQuiet){
            handlerTypes.add(EVENT_EXECUTION_FINISHED);
            handlerTypes.add(EVENT_OUTPUT_END);
        }
        else {
            handlerTypes.add(EventConstants.SCORE_FINISHED_EVENT);
            handlerTypes.add(EventConstants.SCORE_ERROR_EVENT);
            handlerTypes.add(EventConstants.SCORE_FAILURE_EVENT);
            handlerTypes.add(SLANG_EXECUTION_EXCEPTION);
            handlerTypes.add(EVENT_EXECUTION_FINISHED);
            handlerTypes.add(EVENT_INPUT_END);
            handlerTypes.add(EVENT_OUTPUT_END);
        }
        
        SyncTriggerEventListener scoreEventListener = new SyncTriggerEventListener();
        slang.subscribeOnEvents(scoreEventListener, handlerTypes);

        Long executionId = trigger(compilationArtifact, inputs, systemProperties);

        while(!scoreEventListener.isFlowFinished()){
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignore) {}
        }
        slang.unSubscribeOnEvents(scoreEventListener);

        String errorMessageFlowExecution = scoreEventListener.getErrorMessage();
        if (StringUtils.isNotEmpty(errorMessageFlowExecution)) {
            // exception occurred during flow execution
            throw new RuntimeException(errorMessageFlowExecution);
        }

        return executionId;
    }
}
