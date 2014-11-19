package com.hp.score.lang.compiler;
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
}

