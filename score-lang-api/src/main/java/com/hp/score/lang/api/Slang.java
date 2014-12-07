package com.hp.score.lang.api;
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

import com.hp.score.lang.entities.CompilationArtifact;
import org.eclipse.score.events.ScoreEventListener;

import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * User: stoneo
 * Date: 03/12/2014
 * Time: 11:28
 */

/**
 * API for using slang
 */
public interface Slang {

    /**
     * Compile a flow written in slang
     * @param file the path of the slang file containing the flow
     * @param dependencies a set of paths of the slang files of all the flow's dependencies
     * @return the compiled artifact of the flow
     */
    public CompilationArtifact compile(File file, Set<File> dependencies);

    /**
     * Compile an operation written in slang
     * @param file the path of the slang file containing the operation
     * @param operationName the name of the operation to compile from the file
     * @param dependencies a set of paths of the slang files of all the operation's dependencies
     * @return the compiled artifact of the operation
     */
    public CompilationArtifact compileOperation(File file, String operationName, Set<File> dependencies);

    /**
     * Run a flow/operation written in slang already compiled to a compilationArtifact
     * @param compilationArtifact the compiled artifact of the flow
     * @param runInputs the inputs for the flow/operation run
     * @return the execution ID in score
     */
    public Long run(CompilationArtifact compilationArtifact, Map<String, Serializable> runInputs);

    /**
     * Compile & run a flow written in slang
     * @param file the path of the slang file containing the flow
     * @param dependencies a set of paths of the slang files of all the flow's dependencies
     * @param runInputs the inputs for the flow run
     * @return the execution ID in score
     */
    public Long compileAndRun(File file, Set<File> dependencies, Map<String, Serializable> runInputs);

    /**
     * Compile & run an operation written in slang
     * @param file the path of the slang file containing the operation
     * @param operationName the name of the operation to compile from the file
     * @param dependencies a set of paths of the slang files of all the operation's dependencies
     * @param runInputs the inputs for the operation run
     * @return the execution ID in score
     */
    public Long compileAndRunOperation(File file, String operationName, Set<File> dependencies, Map<String, Serializable> runInputs);

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
