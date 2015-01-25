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
     * Compile a flow written in slang
     * @param source the slang source containing the flow
     * @param dependencies a set of slang sources of of all the flow's dependencies
     * @return the compiled artifact of the flow
     */
    public CompilationArtifact compile(SlangSource source, Set<SlangSource> dependencies);

    /**
     * Compile an operation written in slang
     * @param source the slang source containing the operation
     * @param operationName the name of the operation to compile from the source
     * @param dependencies a set of slang sources of all the operation's dependencies
     * @return the compiled artifact of the operation
     */
    public CompilationArtifact compileOperation(SlangSource source, String operationName, Set<SlangSource> dependencies);

	/**
	 * Run a flow/operation written in slang already compiled to a compilationArtifact
	 * @param compilationArtifact the compiled artifact of the flow
	 * @param runInputs the inputs for the flow/operation run
	 * @param systemProperties the system properties for the flow/operation run
	 * @return the execution ID in score
	 */
	public Long run(CompilationArtifact compilationArtifact, Map<String, ? extends Serializable> runInputs, Map<String, ? extends Serializable> systemProperties);

	/**
	 * Compile & run a flow written in slang
	 * @param source the slang source containing the flow
	 * @param dependencies a set of slang sources of all the flow's dependencies
	 * @param runInputs the inputs for the flow run
	 * @param systemProperties the system properties for the flow/operation run
	 * @return the execution ID in score
	 */
	public Long compileAndRun(SlangSource source, Set<SlangSource> dependencies, Map<String, ? extends Serializable> runInputs, Map<String, ? extends Serializable> systemProperties);

	/**
	 * Compile & run an operation written in slang
	 * @param source the slang source containing the operation
	 * @param operationName the name of the operation to compile from the source
	 * @param dependencies a set of slang sources of all the operation's dependencies
	 * @param runInputs the inputs for the operation run
	 * @param systemProperties the system properties for the flow/operation run
	 * @return the execution ID in score
	 */
	public Long compileAndRunOperation(SlangSource source, String operationName, Set<SlangSource> dependencies, Map<String, ? extends Serializable> runInputs,
		Map<String, ? extends Serializable> systemProperties);

	/**
	 * Load system property sources written in slang and map them to fully qualified names
	 * @param sources the slang sources containing the system properties
	 * @return map containing all of the system properties with fully qualified keys
	 */
	public Map<String, ? extends Serializable> loadSystemProperties(SlangSource... sources);

    /**
     * Subscribe on events of score or slang
     * @param eventListener listener for the events
     * @param eventTypes set of types of events to subscribe to
     */
    public void subscribeOnEvents(ScoreEventListener eventListener, Set<String> eventTypes);

    /**
     * Subscribe on events of score or slang
     * @param eventListener listener for the events
     */
    public void unSubscribeOnEvents(ScoreEventListener eventListener);

    /**
     * Subscribe on all of the events of score & slang
     * @param eventListener listener for the events
     */
    public void subscribeOnAllEvents(ScoreEventListener eventListener);

}
