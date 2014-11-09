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

import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.ExecutionStep;
import com.hp.score.lang.compiler.configuration.SpringConfiguration;
import com.hp.score.lang.entities.ScoreLangConstants;
import com.hp.score.lang.entities.bindings.Input;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.net.URL;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringConfiguration.class)
public class CompileOperationTest {

    @Autowired
    private SlangCompiler compiler;

    @Test
    public void testCompileOperationBasic() throws Exception {
        URL resource = getClass().getResource("/operation.yaml");
        ExecutionPlan executionPlan = compiler.compile(new File(resource.toURI()), null);
        Assert.assertNotNull("execution plan is null", executionPlan);
        Assert.assertEquals("there is a different number of steps than expected", 3, executionPlan.getSteps().size());
    }

    @Test
    public void testCompileOperationWithData() throws Exception {
        URL resource = getClass().getResource("/operation_with_data.yaml");
        ExecutionPlan executionPlan = compiler.compile(new File(resource.toURI()), null);

        ExecutionStep startStep = executionPlan.getStep(1L);
        @SuppressWarnings("unchecked") List<Input> inputs = (List<Input>) startStep.getActionData().get(ScoreLangConstants.OPERATION_INPUTS_KEY);
        Assert.assertNotNull("inputs doesn't exist", inputs);
        Assert.assertEquals("there is a different number of inputs than expected", 6, inputs.size());

        ExecutionStep actionStep = executionPlan.getStep(2L);
        String script = (String) actionStep.getActionData().get(ScoreLangConstants.PYTHON_SCRIPT_KEY);
        Assert.assertNotNull("script doesn't exist", script);
        Assert.assertTrue("script is different than expected", script.startsWith("# this is python amigos!!"));

        ExecutionStep endStep = executionPlan.getStep(3L);
        Object outputs = endStep.getActionData().get(ScoreLangConstants.OPERATION_OUTPUTS_KEY);
        Object results = endStep.getActionData().get(ScoreLangConstants.OPERATION_RESULTS_KEY);

        Assert.assertNotNull("outputs don't exist", outputs);
        Assert.assertNotNull("results don't exist", results);

    }


}