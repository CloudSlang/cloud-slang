/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.api;

import io.cloudslang.lang.compiler.MetadataExtractor;
import io.cloudslang.lang.compiler.PrecompileStrategy;
import io.cloudslang.lang.compiler.SlangCompiler;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.modeller.model.Metadata;
import io.cloudslang.lang.compiler.modeller.result.CompilationModellingResult;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.score.api.Score;
import io.cloudslang.score.api.TriggeringProperties;
import io.cloudslang.score.events.EventBus;
import io.cloudslang.score.events.EventConstants;
import io.cloudslang.score.events.ScoreEventListener;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author stoneo
 * @version $Id$
 * @since 03/12/2014
 */
public class SlangImpl implements Slang {

    private static final Logger logger = Logger.getLogger(SlangImpl.class);

    @Autowired
    private SlangCompiler compiler;

    @Autowired
    private MetadataExtractor metadataExtractor;

    @Autowired
    private Score score;

    @Autowired
    private EventBus eventBus;

    @Override
    public CompilationArtifact compile(SlangSource source, Set<SlangSource> dependencies) {
        return compile(source, dependencies, PrecompileStrategy.WITHOUT_CACHE);
    }

    @Override
    public CompilationArtifact compile(
            SlangSource source,
            Set<SlangSource> dependencies,
            PrecompileStrategy precompileStrategy) {
        Validate.notNull(source, "Source can not be null");
        Validate.notNull(precompileStrategy, "Pre-compile strategy can not be null");
        Set<SlangSource> dependencySources = filterOutNullSources(dependencies);

        try {
            return compiler.compile(source, dependencySources, precompileStrategy);
        } catch (Exception e) {
            logger.error("Failed compilation for source : " + source.getName() + " ,Exception is : " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void invalidateAllInPreCompileCache() {
        compiler.invalidateAllInPreCompileCache();
    }

    @Override
    public CompilationModellingResult compileSource(SlangSource source, Set<SlangSource> dependencies) {
        return compileSource(source, dependencies, PrecompileStrategy.WITHOUT_CACHE);
    }

    @Override
    public CompilationModellingResult compileSource(
            SlangSource source,
            Set<SlangSource> dependencies,
            PrecompileStrategy precompileStrategy) {
        Validate.notNull(source, "Source can not be null");
        Validate.notNull(precompileStrategy, "Pre-compile strategy can not be null");
        Set<SlangSource> dependencySources = filterOutNullSources(dependencies);

        try {
            return compiler.compileSource(source, dependencySources, precompileStrategy);
        } catch (Exception e) {
            logger.error("Failed compilation for source : " + source.getName() + " ,Exception is : " + e.getMessage());
            throw new RuntimeException("Failed compilation for source : " + source.getName() +
                    " ,Exception is : " + e.getMessage(), e);
        }
    }

    private Set<SlangSource> filterOutNullSources(Set<SlangSource> dependencies) {
        Set<SlangSource> dependencySources = new HashSet<>();
        if (dependencies != null) {
            for (SlangSource dependency : dependencies) {
                if (dependency != null) {
                    dependencySources.add(dependency);
                }
            }
        }
        return dependencySources;
    }

    @Override
    public Metadata extractMetadata(SlangSource source) {
        Validate.notNull(source, "Source can not be null");
        try {
            return metadataExtractor.extractMetadata(source);
        } catch (Exception e) {
            logger.error("Failed metadata extraction for source : " + source.getName() + " ,Exception is : " +
                    e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Long run(CompilationArtifact compilationArtifact, Map<String, Value> runInputs,
                    Set<SystemProperty> systemProperties) {
        Validate.notNull(compilationArtifact, "Compilation artifact can not be null");
        if (runInputs == null) {
            runInputs = new HashMap<>();
        }

        Map<String, Serializable> executionContext = new HashMap<>();
        RunEnvironment runEnv = new RunEnvironment(systemProperties);
        executionContext.put(ScoreLangConstants.RUN_ENV, runEnv);

        executionContext.put(ScoreLangConstants.USER_INPUTS_KEY, (Serializable) runInputs);
        TriggeringProperties triggeringProperties =
                TriggeringProperties.create(compilationArtifact.getExecutionPlan())
                .setDependencies(compilationArtifact.getDependencies())
                .setContext(executionContext);
        return score.trigger(triggeringProperties);
    }

    @Override
    public Long compileAndRun(
            SlangSource source,
            Set<SlangSource> dependencies,
            Map<String, Value> runInputs,
            Set<SystemProperty> systemProperties) {
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

    @Override
    public Set<SystemProperty> loadSystemProperties(SlangSource source) {
        Validate.notNull(source, "Source can not be null");
        return compiler.loadSystemProperties(source);
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
        eventTypes.add(ScoreLangConstants.EVENT_STEP_START);
        eventTypes.add(ScoreLangConstants.EVENT_INPUT_START);
        eventTypes.add(ScoreLangConstants.EVENT_INPUT_END);
        eventTypes.add(ScoreLangConstants.EVENT_ARGUMENT_START);
        eventTypes.add(ScoreLangConstants.EVENT_ARGUMENT_END);
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