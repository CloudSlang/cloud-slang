/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package org.openscore.cli.services;

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
