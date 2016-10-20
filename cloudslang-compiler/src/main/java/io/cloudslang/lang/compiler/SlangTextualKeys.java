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

    //action
    String JAVA_ACTION_KEY = "java_action";
    String JAVA_ACTION_CLASS_NAME_KEY = "class_name";
    String JAVA_ACTION_METHOD_NAME_KEY = "method_name";
    String JAVA_ACTION_GAV_KEY = "gav";
    String PYTHON_ACTION_KEY = "python_action";
    String PYTHON_ACTION_SCRIPT_KEY = "script";
    String PYTHON_ACTION_DEPENDENCIES_KEY = "dependencies";

    //step
    String DO_KEY = "do";
    String FOR_KEY = "for";
    String BREAK_KEY = "break";
    String PUBLISH_KEY = "publish";
    String NAVIGATION_KEY = "navigate";
    String PARALLEL_LOOP_KEY = "parallel_loop";

    //inputs
    String VALUE_KEY = "value";
    String DEFAULT_KEY = "default";
    String REQUIRED_KEY = "required";
    String SENSITIVE_KEY = "sensitive";
    String PRIVATE_INPUT_KEY = "private";

    // system properties
    String SYSTEM_PROPERTY_KEY = "properties";

}
