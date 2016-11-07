/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.cli.services;

import io.cloudslang.lang.api.Slang;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.score.events.EventConstants;
import io.cloudslang.score.events.ScoreEventListener;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.cloudslang.lang.entities.ScoreLangConstants.EVENT_EXECUTION_FINISHED;
import static io.cloudslang.lang.entities.ScoreLangConstants.EVENT_OUTPUT_END;
import static io.cloudslang.lang.entities.ScoreLangConstants.EVENT_STEP_START;
import static io.cloudslang.lang.entities.ScoreLangConstants.SLANG_EXECUTION_EXCEPTION;

/**
 * @author Bonczidai Levente
 * @version $Id$
 * @since 11/13/2014
 */
@Service
public class ScoreServicesImpl implements ScoreServices {

    @Autowired
    private Slang slang;

    @Autowired
    private AutowireCapableBeanFactory autowireCapableBeanFactory;

    public void subscribe(ScoreEventListener eventHandler, Set<String> eventTypes) {
        slang.subscribeOnEvents(eventHandler, eventTypes);
    }

    /**
     * This method will trigger the flow in an Async matter.
     *
     * @param compilationArtifact the artifact to trigger
     * @param inputs              : flow inputs
     * @return executionId
     */
    @Override
    public Long trigger(CompilationArtifact compilationArtifact, Map<String, Value> inputs,
                        Set<SystemProperty> systemProperties) {
        return slang.run(compilationArtifact, inputs, systemProperties);
    }

    /**
     * This method will trigger the flow in a synchronize matter, meaning only one flow can run at a time.
     *
     * @param compilationArtifact the artifact to trigger
     * @param inputs              : flow inputs
     * @return executionId
     */
    @Override
    public Long triggerSync(CompilationArtifact compilationArtifact, Map<String, Value> inputs,
                            Set<SystemProperty> systemProperties, boolean isQuiet, boolean debug) {
        //add start event
        Set<String> handlerTypes = new HashSet<>();
        if (isQuiet) {
            handlerTypes.add(EVENT_EXECUTION_FINISHED);
            handlerTypes.add(EVENT_OUTPUT_END);
        } else {
            handlerTypes.add(EventConstants.SCORE_FINISHED_EVENT);
            handlerTypes.add(EventConstants.SCORE_ERROR_EVENT);
            handlerTypes.add(EventConstants.SCORE_FAILURE_EVENT);
            handlerTypes.add(EventConstants.MAVEN_DEPENDENCY_BUILD);
            handlerTypes.add(EventConstants.MAVEN_DEPENDENCY_BUILD_FINISHED);
            handlerTypes.add(SLANG_EXECUTION_EXCEPTION);
            handlerTypes.add(EVENT_EXECUTION_FINISHED);
            handlerTypes.add(EVENT_STEP_START);
            handlerTypes.add(EVENT_OUTPUT_END);
        }

        SyncTriggerEventListener scoreEventListener = new SyncTriggerEventListener();
        autowireCapableBeanFactory.autowireBean(scoreEventListener);
        scoreEventListener.setIsDebugMode(debug);
        slang.subscribeOnEvents(scoreEventListener, handlerTypes);

        final Long executionId = trigger(compilationArtifact, inputs, systemProperties);

        while (!scoreEventListener.isFlowFinished()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignore) {
            }
        }
        slang.unSubscribeOnEvents(scoreEventListener);

        String errorMessageFlowExecution = scoreEventListener.getErrorMessage();
        if (StringUtils.isNotEmpty(errorMessageFlowExecution)) {

            if (errorMessageFlowExecution.contains("supplied using a system property")) {
                errorMessageFlowExecution += "\n\nA system property file can be included using --spf <path_to_file>";
            }
            // exception occurred during flow execution
            throw new RuntimeException(errorMessageFlowExecution);
        }

        return executionId;
    }
}
