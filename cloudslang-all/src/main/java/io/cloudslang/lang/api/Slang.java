/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.api;

import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.modeller.model.Metadata;
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
     * Run a flow or operation written in CloudSlang already compiled to a compilationArtifact
     *
     * @param compilationArtifact the compiled artifact of the flow or operation
     * @param runInputs           the inputs for the flow or operation run
     * @param systemProperties    the system properties for the flow or operation run
     * @return the execution ID in score
     */
    Long run(CompilationArtifact compilationArtifact, Map<String, Value> runInputs, Set<SystemProperty> systemProperties);

    /**
     * Compile and run a flow or operation written in CloudSlang
     *
     * @param source           the CloudSlang source containing the flow or operation
     * @param dependencies     a set of CloudSlang sources of all the flow or operation's dependencies
     * @param runInputs        the inputs for the flow or operation run
     * @param systemProperties the system properties for the flow or operation run
     * @return the execution ID in score
     */
    Long compileAndRun(SlangSource source, Set<SlangSource> dependencies, Map<String, Value> runInputs, Set<SystemProperty> systemProperties);

    /**
     * Subscribe to events of score or CloudSlang
     *
     * @param eventListener listener for the events
     * @param eventTypes    set of types of events to subscribe to
     */
    void subscribeOnEvents(ScoreEventListener eventListener, Set<String> eventTypes);

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
