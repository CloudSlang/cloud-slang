/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
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
    String EXECUTABLE_TYPE = "executableType";

    String EXPRESSION_START_DELIMITER = "${";
    String EXPRESSION_START_DELIMITER_ESCAPED = "\\$\\{";
    String EXPRESSION_END_DELIMITER = "}";
    String EXPRESSION_END_DELIMITER_ESCAPED = "\\}";

    String NAMESPACE_DELIMITER = ".";

    //action scope
    String ACTION_TYPE = "actionType";
    String ACTION_DEPENDENCIES = "dependencies";

    String JAVA_ACTION_CLASS_KEY = "className";
    String JAVA_ACTION_METHOD_KEY = "methodName";
    String JAVA_ACTION_GAV_KEY = "gav";

    String PYTHON_ACTION_SCRIPT_KEY = "script";
    String PYTHON_ACTION_DEPENDENCIES_KEY = "dependencies";

    //navigation
    String NEXT_STEP_ID_KEY = "nextStepId";
    String PREVIOUS_STEP_ID_KEY = "previousStepId";

    //operation scope
    String EXECUTABLE_INPUTS_KEY = "executableInputs";
    String USER_INPUTS_KEY = "userInputs";
    String EXECUTABLE_OUTPUTS_KEY = "executableOutputs";
    String EXECUTABLE_RESULTS_KEY = "executableResults";

    //step scope
    String STEP_INPUTS_KEY = "stepInputs";
    String STEP_PUBLISH_KEY = "stepPublishValues";
    String STEP_NAVIGATION_KEY = "stepNavigationValues";
    String REF_ID = "refId";
    String LOOP_KEY = "loop";
    String BREAK_LOOP_KEY = "breakOn";
    String STEP_INPUTS_RESULT_CONTEXT = "stepInputsResultContext";

    // parallel loop
    String PARALLEL_LOOP_KEY = "parallelLoop";
    String PARALLEL_LOOP_STATEMENT_KEY = "parallelLoopStatement";
    String BRANCH_BEGIN_STEP_ID_KEY = "branchBeginStep";
    String BRANCH_RESULT_KEY = "branch_result";

    // Events types
    String SLANG_EXECUTION_EXCEPTION = "SLANG_EXECUTION_EXCEPTION";
    String EVENT_ACTION_START = "EVENT_ACTION_START";
    String EVENT_ACTION_END = "EVENT_ACTION_END";
    String EVENT_ACTION_ERROR = "EVENT_ACTION_ERROR";
    String EVENT_INPUT_START = "EVENT_INPUT_START";
    String EVENT_INPUT_END = "EVENT_INPUT_END";
    String EVENT_STEP_START = "EVENT_STEP_START";
    String EVENT_ARGUMENT_START = "EVENT_ARGUMENT_START";
    String EVENT_ARGUMENT_END = "EVENT_ARGUMENT_END";
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
