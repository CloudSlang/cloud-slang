/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.openscore.lang.api;

import org.openscore.events.ScoreEventListener;
import org.openscore.lang.compiler.SlangSource;
import org.openscore.lang.entities.CompilationArtifact;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * API for using slang
 *
 * @author stoneo
 * @since 03/12/2014
 * @version $Id$
 */
public interface Slang {

    /**
     * Compile a flow or operation written in slang
     * @param source the slang source containing the flow
     * @param dependencies a set of slang sources of of all the flow or operation's dependencies
     * @return the compiled artifact of the flow
     */
    public CompilationArtifact compile(SlangSource source, Set<SlangSource> dependencies);

	/**
	 * Run a flow or operation written in slang already compiled to a compilationArtifact
	 * @param compilationArtifact the compiled artifact of the flow or operation
	 * @param runInputs the inputs for the flow or operation run
	 * @param systemProperties the system properties for the flow or operation run
	 * @return the execution ID in score
	 */
	public Long run(CompilationArtifact compilationArtifact, Map<String, ? extends Serializable> runInputs, Map<String, ? extends Serializable> systemProperties);

	/**
	 * Compile and run a flow or operation written in slang
	 * @param source the slang source containing the flow or operation
	 * @param dependencies a set of slang sources of all the flow or operation's dependencies
	 * @param runInputs the inputs for the flow or operation run
	 * @param systemProperties the system properties for the flow or operation run
	 * @return the execution ID in score
	 */
	public Long compileAndRun(SlangSource source, Set<SlangSource> dependencies, Map<String, ? extends Serializable> runInputs, Map<String, ? extends Serializable> systemProperties);

    /**
     * Subscribe to events of score or slang
     * @param eventListener listener for the events
     * @param eventTypes set of types of events to subscribe to
     */
    public void subscribeOnEvents(ScoreEventListener eventListener, Set<String> eventTypes);

    /**
     * Unsubscribe from events of score or slang
     * @param eventListener listener for the events
     */
    public void unSubscribeOnEvents(ScoreEventListener eventListener);

    /**
     * Subscribe to all of the events of score and slang
     * @param eventListener listener for the events
     */
    public void subscribeOnAllEvents(ScoreEventListener eventListener);

}
