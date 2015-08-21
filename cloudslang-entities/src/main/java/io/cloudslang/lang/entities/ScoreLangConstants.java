/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.entities;

/**
 * User: stoneo
 * Date: 07/10/2014
 * Time: 12:52
 */
public interface ScoreLangConstants {

    String RUN_ENV = "runEnv";
    String HOOKS = "hooks";
    String NODE_NAME_KEY = "nodeName";

    //action scope
    String ACTION_CLASS_KEY = "className";
    String ACTION_TYPE = "actionType";

    String ACTION_METHOD_KEY = "methodName";
    String PYTHON_SCRIPT_KEY = "python_script";
    //navigation
    String NEXT_STEP_ID_KEY = "nextStepId";
    String PREVIOUS_STEP_ID_KEY = "previousStepId";

    //operation scope
    String EXECUTABLE_INPUTS_KEY = "executableInputs";
    String BIND_OUTPUT_FROM_INPUTS_KEY = "fromInputs";
    String USER_INPUTS_KEY = "userInputs";
    String EXECUTABLE_OUTPUTS_KEY = "executableOutputs";
    String EXECUTABLE_RESULTS_KEY = "executableResults";

    //task scope
    String TASK_INPUTS_KEY = "taskInputs";
    String TASK_PUBLISH_KEY = "taskPublishValues";
    String TASK_NAVIGATION_KEY = "taskNavigationValues";
    String REF_ID = "refId";
    String LOOP_KEY = "loop";
    String BREAK_LOOP_KEY = "breakOn";

    // async loop
    String ASYNC_LOOP_KEY = "async_loop";
    String ASYNC_LOOP_STATEMENT_KEY = "asyncLoopStatement";
    String TASK_AGGREGATE_KEY = "taskAggregateValues";
    String BRANCH_BEGIN_STEP_ID_KEY = "branchBeginStep";

    // Events types
	String SLANG_EXECUTION_EXCEPTION = "SLANG_EXECUTION_EXCEPTION";
	String EVENT_ACTION_START = "EVENT_ACTION_START";
	String EVENT_ACTION_END = "EVENT_ACTION_END";
	String EVENT_ACTION_ERROR = "EVENT_ACTION_ERROR";
    String EVENT_TASK_START = "EVENT_TASK_START";
    String EVENT_INPUT_START = "EVENT_INPUT_START";
	String EVENT_INPUT_END = "EVENT_INPUT_END";
	String EVENT_OUTPUT_START = "EVENT_OUTPUT_START";
	String EVENT_OUTPUT_END = "EVENT_OUTPUT_END";
    String EVENT_EXECUTION_FINISHED = "EVENT_EXECUTION_FINISHED";
    String EVENT_BRANCH_START = "EVENT_BRANCH_START";
    String EVENT_BRANCH_END = "EVENT_BRANCH_END";
    String EVENT_SPLIT_BRANCHES = "EVENT_SPLIT_BRANCHES";
    String EVENT_JOIN_BRANCHES_START = "EVENT_JOIN_BRANCHES_START";
    String EVENT_JOIN_BRANCHES_END = "EVENT_JOIN_BRANCHES_END";

    // results
    String SUCCESS_RESULT = "SUCCESS";
    String FAILURE_RESULT = "FAILURE";

}
