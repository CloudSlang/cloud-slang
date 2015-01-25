package org.openscore.lang.compiler;
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

    //flow
    String FLOW_NAME_KEY = "name";
    String WORKFLOW_KEY = "workflow";
    String ON_FAILURE_KEY = "on_failure";

    //action
    String JAVA_ACTION = "java_action";

    //operation
    String ACTION_KEY = "action";

    //task
    String DO_KEY = "do";
    String PUBLISH_KEY = "publish";
    String NAVIGATION_KEY = "navigate";

    //inputs
    String DEFAULT_KEY = "default";
    String REQUIRED_KEY = "required";
    String ENCRYPTED_KEY = "encrypted";
    String OVERRIDE_KEY = "override";
    String SYSTEM_PROPERTY_KEY = "system_property";

}

