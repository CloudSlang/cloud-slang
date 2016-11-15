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

import io.cloudslang.lang.compiler.PrecompileStrategy;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.modeller.model.Metadata;
import io.cloudslang.lang.compiler.modeller.result.CompilationModellingResult;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.score.events.ScoreEventListener;
import java.util.Map;
import java.util.Set;

/**
 * API for using CloudSlang
 *
 * @author stoneo
 * @version $Id$
 * @since 03/12/2014
 */
public interface Slang {

    /**
     * Compile a CloudSlang source
     *
     * @param source the CloudSlang source file
     * @param dependencies  a set of CloudSlang sources containing the source dependencies
     * @return the model (may be partially correct) and the accumulated errors
     */
    CompilationModellingResult compileSource(SlangSource source, Set<SlangSource> dependencies);

    /**
     * Compile a CloudSlang source
     *
     * @param source the CloudSlang source file
     * @param dependencies  a set of CloudSlang sources containing the source dependencies
     * @param precompileStrategy with / without cache
     * @return the model (may be partially correct) and the accumulated errors
     */
    CompilationModellingResult compileSource(
            SlangSource source,
            Set<SlangSource> dependencies,
            PrecompileStrategy precompileStrategy);

    /**
     * Extract the metadata of a flow or operation written in CloudSlang
     *
     * @param source the CloudSlang source containing the flow
     * @return the metadata of the flow
     */
    Metadata extractMetadata(SlangSource source);

    /**
     * Compile a flow or operation written in CloudSlang
     *
     * @param source       the CloudSlang source containing the flow
     * @param dependencies a set of CloudSlang sources of of all the flow or operation's dependencies
     * @return the compiled artifact of the flow
     */
    CompilationArtifact compile(SlangSource source, Set<SlangSource> dependencies);

    /**
     * Compile a flow or operation written in CloudSlang
     *
     * @param source             the CloudSlang source containing the flow
     * @param dependencies       a set of CloudSlang sources of of all the flow or operation's dependencies
     * @param precompileStrategy with / without cache
     * @return
     */
    CompilationArtifact compile(
            SlangSource source,
            Set<SlangSource> dependencies,
            PrecompileStrategy precompileStrategy);

    /**
     * Remove all elements in pre-compile cache. No-cached calls are not affected.
     */
    void invalidateAllInPreCompileCache();

    /**
     * Run a flow or operation written in CloudSlang already compiled to a compilationArtifact
     *
     * @param compilationArtifact the compiled artifact of the flow or operation
     * @param runInputs           the inputs for the flow or operation run
     * @param systemProperties    the system properties for the flow or operation run
     * @return the execution ID in score
     */
    Long run(CompilationArtifact compilationArtifact,
             Map<String, Value> runInputs,
             Set<SystemProperty> systemProperties);

    /**
     * Compile and run a flow or operation written in CloudSlang
     *
     * @param source           the CloudSlang source containing the flow or operation
     * @param dependencies     a set of CloudSlang sources of all the flow or operation's dependencies
     * @param runInputs        the inputs for the flow or operation run
     * @param systemProperties the system properties for the flow or operation run
     * @return the execution ID in score
     */
    Long compileAndRun(SlangSource source,
                       Set<SlangSource> dependencies,
                       Map<String, Value> runInputs,
                       Set<SystemProperty> systemProperties);

    /**
     * Subscribe to events of score or CloudSlang
     *
     * @param eventListener listener for the events
     * @param eventTypes    set of types of events to subscribe to
     */
    void subscribeOnEvents(ScoreEventListener eventListener,
                           Set<String> eventTypes);

    /**
     * Unsubscribe from events of score or CloudSlang
     *
     * @param eventListener listener for the events
     */
    void unSubscribeOnEvents(ScoreEventListener eventListener);

    /**
     * Subscribe to all of the events of score and CloudSlang
     *
     * @param eventListener listener for the events
     */
    void subscribeOnAllEvents(ScoreEventListener eventListener);

    Set<SystemProperty> loadSystemProperties(SlangSource source);

}
