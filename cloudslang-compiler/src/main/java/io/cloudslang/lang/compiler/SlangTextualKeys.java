/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler;

/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/


/*
 * Created by orius123 on 05/11/14.
 */
public interface SlangTextualKeys {

    //executable
    String NAMESPACE_KEY = "namespace";
    String RESULTS_KEY = "results";
    String INPUTS_KEY = "inputs";
    String OUTPUTS_KEY = "outputs";
    String FLOW_TYPE = "flow";
    String OPERATION_TYPE = "operation";
    String DECISION_TYPE = "decision";
    String EXTENSIONS_KEY = "extensions";

    //flow
    String EXECUTABLE_NAME_KEY = "name";
    String WORKFLOW_KEY = "workflow";
    String ON_FAILURE_KEY = "on_failure";
    String NEXT_STEP = "next_step";

    //action
    String JAVA_ACTION_KEY = "java_action";
    String JAVA_ACTION_CLASS_NAME_KEY = "class_name";
    String JAVA_ACTION_METHOD_NAME_KEY = "method_name";
    String JAVA_ACTION_GAV_KEY = "gav";
    String PYTHON_ACTION_KEY = "python_action";
    String PYTHON_ACTION_SCRIPT_KEY = "script";
    String PYTHON_ACTION_VERSION_KEY = "version";
    String PYTHON_ACTION_USE_JYTHON_KEY = "use_jython";
    String PYTHON_ACTION_DEPENDENCIES_KEY = "dependencies";
    String SEQ_ACTION_KEY = "sequential_action";
    String SEQ_STEPS_KEY = "steps";
    String SEQ_ACTION_GAV_KEY = "gav";
    String SEQ_SKILLS_KEY = "skills";
    String SEQ_SETTINGS_KEY = "settings";
    String SEQ_EXTERNAL_KEY = "external";

    //step
    String DO_KEY = "do";
    String DO_EXTERNAL_KEY = "do_external";
    String FOR_KEY = "for";
    String BREAK_KEY = "break";
    String PUBLISH_KEY = "publish";
    String NAVIGATION_KEY = "navigate";
    String PARALLEL_LOOP_KEY = "parallel_loop";
    String WORKER_GROUP = "worker_group"; //&& flow
    String ROBOT_GROUP = "robot_group";

    //seq step
    String SEQ_STEP_ID_KEY = "id";
    String SEQ_STEP_PATH_KEY = "object_path";
    String SEQ_STEP_NAME_KEY = "name";
    String SEQ_STEP_ACTION_KEY = "action";
    String SEQ_STEP_ARGS_KEY = "args";
    String SEQ_STEP_DEFAULT_ARGS_KEY = "default_args";
    String SEQ_STEP_HIGHLIGHT_ID_KEY = "highlight_id";
    String SEQ_STEP_SNAPSHOT_KEY = "snapshot";
    String SEQ_STEP_COMMENT_KEY = "comment";

    //inputs
    String VALUE_KEY = "value";
    String DEFAULT_KEY = "default";
    String REQUIRED_KEY = "required";
    String SENSITIVE_KEY = "sensitive";
    String PRIVATE_INPUT_KEY = "private";
    String SEQ_OUTPUT_ROBOT_KEY = "robot";
    String PROMPT_KEY = "prompt";
    String PROMPT_TYPE_KEY = "type";
    String PROMPT_MESSAGE_KEY = "message";
    String PROMPT_OPTIONS_KEY = "options";
    String PROMPT_DELIMITER_KEY = "delimiter";

    // system properties
    String SYSTEM_PROPERTY_KEY = "properties";

    // object repository
    String OBJECT_REPOSITORY_KEY = "object_repository";

}
