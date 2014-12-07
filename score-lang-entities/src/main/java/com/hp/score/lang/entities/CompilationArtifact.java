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
package com.hp.score.lang.entities;

import com.hp.score.lang.entities.bindings.Input;
import org.eclipse.score.api.ExecutionPlan;

import java.util.List;
import java.util.Map;

/*
 * Created by orius123 on 10/11/14.
 */
public class CompilationArtifact {

    private final ExecutionPlan executionPlan;
    private final Map<String, ExecutionPlan> dependencies;
    private final List<Input> inputs;

    public CompilationArtifact(ExecutionPlan executionPlan, Map<String, ExecutionPlan> dependencies, List<Input> inputs) {
        this.executionPlan = executionPlan;
        this.dependencies = dependencies;
        this.inputs = inputs;
    }

    public ExecutionPlan getExecutionPlan() {
        return executionPlan;
    }

    public Map<String, ExecutionPlan> getDependencies() {
        return dependencies;
    }

    public List<Input> getInputs() {
        return inputs;
    }
}
