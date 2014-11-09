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

/**
 * User: stoneo
 * Date: 07/10/2014
 * Time: 12:52
 */
public interface ScoreLangConstants {

    String RUN_ENV = "runEnv";
    String HOOKS = "hooks";
    //action scope
    String ACTION_CLASS_KEY = "className";
    String ACTION_TYPE = "actionType";

    String ACTION_METHOD_KEY = "methodName";
    String PYTHON_SCRIPT_KEY = "python_script";
    //navigation
    String NEXT_STEP_ID_KEY = "nextStepId";

    //operation scope
    String OPERATION_INPUTS_KEY = "operationInputs";
    String USER_INPUTS_KEY = "userInputs";
    String OPERATION_OUTPUTS_KEY = "operationOutputs";
    String OPERATION_RESULTS_KEY = "operationResults";

    //task scope
    String TASK_INPUTS_KEY = "taskInputs";
    String TASK_PUBLISH_KEY = "taskPublishValues";
    String TASK_NAVIGATION_KEY = "taskNavigationValues";
    String REF_ID = "subFlowId";

	String EVENT_ACTION_START = "EVENT_ACTION_START";
	String EVENT_ACTION_END = "EVENT_ACTION_END";
	String EVENT_ACTION_ERROR = "EVENT_ACTION_ERROR";
	String EVENT_INPUT_START = "EVENT_INPUT_START";
	String EVENT_INPUT_END = "EVENT_INPUT_END";
	String EVENT_OUTPUT_START = "EVENT_OUTPUT_START";
	String EVENT_OUTPUT_END = "EVENT_OUPUT_END";

    // results
    String SUCCESS_RESULT = "SUCCESS";
    String FAILURE_RESULT = "FAILURE";

}
