/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.cli.services;

import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.score.events.ScoreEventListener;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * @author Bonczidai Levente
 * @since 12/09/2014
 * @version $Id$
 */
public interface ScoreServices {
    void subscribe(ScoreEventListener eventHandler, Set<String> eventTypes);
    Long trigger(CompilationArtifact compilationArtifact, Map<String, ? extends Serializable> inputs, Set<SystemProperty> systemProperties);
    Long triggerSync(CompilationArtifact compilationArtifact, Map<String, ? extends Serializable> inputs, Set<SystemProperty> systemProperties, boolean quiet, boolean debug);
}
