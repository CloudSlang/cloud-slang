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
import io.cloudslang.score.events.ScoreEventListener;
import io.cloudslang.lang.entities.CompilationArtifact;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * API for using CloudSlang
 *
 * @author stoneo
 * @since 03/12/2014
 * @version $Id$
 */
public interface Slang {

    /**
     * Compile a flow or operation written in CloudSlang
     * @param source the CloudSlang source containing the flow
     * @param dependencies a set of CloudSlang sources of of all the flow or operation's dependencies
     * @return the compiled artifact of the flow
     */
    public CompilationArtifact compile(SlangSource source, Set<SlangSource> dependencies);

	/**
	 * Run a flow or operation written in CloudSlang already compiled to a compilationArtifact
	 * @param compilationArtifact the compiled artifact of the flow or operation
	 * @param runInputs the inputs for the flow or operation run
	 * @param systemProperties the system properties for the flow or operation run
	 * @return the execution ID in score
	 */
	public Long run(CompilationArtifact compilationArtifact, Map<String, ? extends Serializable> runInputs, Map<String, ? extends Serializable> systemProperties);

	/**
	 * Compile and run a flow or operation written in CloudSlang
	 * @param source the CloudSlang source containing the flow or operation
	 * @param dependencies a set of CloudSlang sources of all the flow or operation's dependencies
	 * @param runInputs the inputs for the flow or operation run
	 * @param systemProperties the system properties for the flow or operation run
	 * @return the execution ID in score
	 */
	public Long compileAndRun(SlangSource source, Set<SlangSource> dependencies, Map<String, ? extends Serializable> runInputs, Map<String, ? extends Serializable> systemProperties);

    /**
     * Subscribe to events of score or CloudSlang
     * @param eventListener listener for the events
     * @param eventTypes set of types of events to subscribe to
     */
    public void subscribeOnEvents(ScoreEventListener eventListener, Set<String> eventTypes);

    /**
     * Unsubscribe from events of score or CloudSlang
     * @param eventListener listener for the events
     */
    public void unSubscribeOnEvents(ScoreEventListener eventListener);

    /**
     * Subscribe to all of the events of score and CloudSlang
     * @param eventListener listener for the events
     */
    public void subscribeOnAllEvents(ScoreEventListener eventListener);

}
