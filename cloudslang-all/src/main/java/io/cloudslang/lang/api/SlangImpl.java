/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.api;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import io.cloudslang.lang.compiler.SlangCompiler;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.score.api.Score;
import io.cloudslang.score.api.TriggeringProperties;
import io.cloudslang.score.events.EventBus;
import io.cloudslang.score.events.EventConstants;
import io.cloudslang.score.events.ScoreEventListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static ch.lambdaj.Lambda.filter;

import static org.hamcrest.Matchers.notNullValue;

/**
 * @author stoneo
 * @since 03/12/2014
 * @version $Id$
 */
public class SlangImpl implements Slang {

    private final static Logger logger = Logger.getLogger(SlangImpl.class);

    @Autowired
    private SlangCompiler compiler;
    @Autowired
    private Score score;
    @Autowired
    private EventBus eventBus;

    @Override
    public CompilationArtifact compile(SlangSource source, Set<SlangSource> dependencies) {

        Validate.notNull(source, "Source can not be null");
        Set<SlangSource> dependencySources = new HashSet<>(filter(notNullValue(), dependencies));

        try {
            return compiler.compile(source, dependencySources);
        } catch (Exception e) {
            logger.error("Failed compilation for source : " + source.getName() + " ,Exception is : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

	@Override
	public Long run(CompilationArtifact compilationArtifact, Map<String, ? extends Serializable> runInputs, Map<String, ? extends Serializable> systemProperties) {
		Validate.notNull(compilationArtifact, "Compilation artifact can not be null");
		if(runInputs == null) {
			runInputs = new HashMap<>();
		}
		Map<String, Serializable> executionContext = new HashMap<>();
		RunEnvironment runEnv = new RunEnvironment(systemProperties);
		executionContext.put(ScoreLangConstants.RUN_ENV, runEnv);
	        Map<String, ? extends Serializable> clonedRunInputs = new HashMap<>(runInputs);
		executionContext.put(ScoreLangConstants.USER_INPUTS_KEY, (Serializable) clonedRunInputs);
		TriggeringProperties triggeringProperties = TriggeringProperties.create(compilationArtifact.getExecutionPlan()).setDependencies(compilationArtifact.getDependencies())
			.setContext(executionContext);
		return score.trigger(triggeringProperties);
	}

	@Override
	public Long compileAndRun(SlangSource source, Set<SlangSource> dependencies, Map<String, ? extends Serializable> runInputs,
		Map<String, ? extends Serializable> systemProperties) {
		CompilationArtifact compilationArtifact = compile(source, dependencies);
		return run(compilationArtifact, runInputs, systemProperties);
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
        eventTypes.add(ScoreLangConstants.SLANG_EXECUTION_EXCEPTION);
        eventTypes.add(ScoreLangConstants.EVENT_ACTION_START);
        eventTypes.add(ScoreLangConstants.EVENT_ACTION_END);
        eventTypes.add(ScoreLangConstants.EVENT_ACTION_ERROR);
        eventTypes.add(ScoreLangConstants.EVENT_TASK_START);
        eventTypes.add(ScoreLangConstants.EVENT_INPUT_START);
        eventTypes.add(ScoreLangConstants.EVENT_INPUT_END);
        eventTypes.add(ScoreLangConstants.EVENT_OUTPUT_START);
        eventTypes.add(ScoreLangConstants.EVENT_OUTPUT_END);
        eventTypes.add(ScoreLangConstants.EVENT_BRANCH_START);
        eventTypes.add(ScoreLangConstants.EVENT_BRANCH_END);
        eventTypes.add(ScoreLangConstants.EVENT_SPLIT_BRANCHES);
        eventTypes.add(ScoreLangConstants.EVENT_JOIN_BRANCHES_START);
        eventTypes.add(ScoreLangConstants.EVENT_JOIN_BRANCHES_END);
        eventTypes.add(ScoreLangConstants.EVENT_EXECUTION_FINISHED);
        return eventTypes;
    }

}
