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
package com.hp.score.lang.tests.operation;

import org.openscore.lang.entities.CompilationArtifact;
import org.openscore.events.EventConstants;
import org.openscore.events.ScoreEvent;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * User: stoneo
 * Date: 11/11/2014
 * Time: 11:55
 */

public class OperationSystemTest extends SystemsTestsParent {

    @Test
    public void testCompileAndRunOperationBasic() throws Exception {
        URL resource = getClass().getResource("/yaml/simple_operations.yaml");
        CompilationArtifact compilationArtifact = slang.compileOperation(new File(resource.toURI()), "test_op", null);
        //Trigger ExecutionPlan
        Map<String, Serializable> userInputs = new HashMap<>();
        ScoreEvent event = trigger(compilationArtifact, userInputs);
        Assert.assertEquals(EventConstants.SCORE_FINISHED_EVENT, event.getEventType());
    }

    @Test
    public void testCompileAndRunOperationWithData() throws Exception {
        URL resource = getClass().getResource("/yaml/operation_with_data.yaml");
        CompilationArtifact compilationArtifact = slang.compileOperation(new File(resource.toURI()), "test_op_2", null);
        //Trigger ExecutionPlan
        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("input1", "value1");
        userInputs.put("input2", "value2");
        userInputs.put("input4", "value4");
        userInputs.put("input5", "value5");
        ScoreEvent event = trigger(compilationArtifact, userInputs);
        Assert.assertEquals(EventConstants.SCORE_FINISHED_EVENT, event.getEventType());
    }
}