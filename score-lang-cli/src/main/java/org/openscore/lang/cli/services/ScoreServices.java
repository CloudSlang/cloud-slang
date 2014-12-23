/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.lang.cli.services;

import org.openscore.lang.entities.CompilationArtifact;

import org.openscore.events.ScoreEventListener;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Date: 12/9/2014
 *
 * @author Bonczidai Levente
 */
public interface ScoreServices {
    void subscribe(ScoreEventListener eventHandler, Set<String> eventTypes);
    Long trigger(CompilationArtifact compilationArtifact, Map<String, Serializable> inputs);
    Long triggerSync(CompilationArtifact compilationArtifact, Map<String, Serializable> inputs);
}
