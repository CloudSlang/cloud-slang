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
package com.hp.score.lang.runtime.env;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * User: stoneo
 * Date: 20/10/2014
 * Time: 10:28
 */
public class RunEnvironment implements Serializable{

    private HashMap<String, Serializable> callArguments;

    private ReturnValues returnValues;

    private Long nextStepPosition;

    // stack holding the contexts of the parent scopes
    private ContextStack contextStack;

    // stack of the parent flows data (fo the sub-flow use-case)
    private ParentFlowStack parentFlowStack;
    private ExecutionPath executionPath;
//    LinkedHashMap<String, Serializable> systemProperties;

    public RunEnvironment() {
        contextStack = new ContextStack();
        parentFlowStack = new ParentFlowStack();
        callArguments = new HashMap<>();
        executionPath = new ExecutionPath();
    }

    public ContextStack getStack(){
        return contextStack;
    }

    public ParentFlowStack getParentFlowStack() {
        return parentFlowStack;
    }

    public Map<String, Serializable> removeCallArguments() {
        Map<String, Serializable> callArgumentsValues = callArguments;
        callArguments = new HashMap<>();
        return callArgumentsValues;
    }

    public void putCallArguments(Map<String, Serializable> callArguments) {
        this.callArguments.putAll(callArguments);
    }

    public ReturnValues removeReturnValues() {
        ReturnValues values = returnValues;
        returnValues = null;
        return values;
    }

    public void putReturnValues(ReturnValues returnValues) {
        this.returnValues = returnValues;
    }

    public Long removeNextStepPosition() {
        Long nextStep = nextStepPosition;
        nextStepPosition = null;
        return nextStep;
    }

    public void putNextStepPosition(Long nextStepPosition) {
        this.nextStepPosition = nextStepPosition;
    }

    public ExecutionPath getExecutionPath() {
        return this.executionPath;
    }

}
