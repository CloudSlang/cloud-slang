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
package org.openscore.lang.compiler;

import org.openscore.lang.compiler.configuration.SlangCompilerSpringConfig;
import org.openscore.lang.entities.CompilationArtifact;
import org.openscore.lang.entities.ResultNavigation;
import org.openscore.lang.entities.ScoreLangConstants;
import org.openscore.api.ExecutionPlan;
import org.openscore.api.ExecutionStep;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * Created by orius123 on 05/11/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
public class CompileFlowWithOnFailureTest {

    @Autowired
    private SlangCompiler compiler;

    @Test
    public void testCompileOnFailureBasic() throws Exception {
        URI flow = getClass().getResource("/flow_with_on_failure.yaml").toURI();
        URI operation = getClass().getResource("/operation.yaml").toURI();

        Set<File> path = new HashSet<>();
        path.add(new File(operation));

        CompilationArtifact compilationArtifact = compiler.compileFlow(new File(flow), path);
        ExecutionPlan executionPlan = compilationArtifact.getExecutionPlan();
        Assert.assertNotNull("execution plan is null", executionPlan);
        Assert.assertEquals("there is a different number of steps than expected", 10, executionPlan.getSteps().size());
        Assert.assertEquals("execution plan name is different than expected", "basic_flow", executionPlan.getName());
        Assert.assertEquals("the dependencies size is not as expected", 1, compilationArtifact.getDependencies().size());
        Assert.assertEquals("the inputs size is not as expected", 1, compilationArtifact.getInputs().size());

        long firstOnFailureStep = 6L;
        long endFlowStep = 0L;

        ExecutionStep firstStep = executionPlan.getStep(3L);
        Assert.assertEquals("first step didn't navigate to on failure", firstOnFailureStep, getFailureNavigationStepId(firstStep));
        ExecutionStep secondStep = executionPlan.getStep(5L);
        Assert.assertEquals(endFlowStep, getFailureNavigationStepId(secondStep));
        ExecutionStep firstOnFailStep = executionPlan.getStep(7L);
        Assert.assertEquals(endFlowStep, getFailureNavigationStepId(firstOnFailStep));
        ExecutionStep secondOnFailStep = executionPlan.getStep(9L);
        Assert.assertEquals(endFlowStep, getFailureNavigationStepId(secondOnFailStep));
        Map<String, ResultNavigation> secondOnFailStepNavigationMap = getNavigationMap(secondOnFailStep);
        ResultNavigation secondOnFailStepResultSuccessNavigation =
                secondOnFailStepNavigationMap.get(ScoreLangConstants.SUCCESS_RESULT);
        Assert.assertEquals("on failure success should navigate to failure",
                ScoreLangConstants.FAILURE_RESULT, secondOnFailStepResultSuccessNavigation.getPresetResult());
    }

	private long getFailureNavigationStepId(ExecutionStep firstStep) {
        Map<String, ResultNavigation> navigationData = getNavigationMap(firstStep);
        return navigationData.get(ScoreLangConstants.FAILURE_RESULT).getNextStepId();
	}

    private Map<String, ResultNavigation> getNavigationMap(ExecutionStep firstStep) {
        @SuppressWarnings("unchecked") Map<String, ResultNavigation> stringResultNavigationMap =
                (Map<String, ResultNavigation>) firstStep.getActionData().get(ScoreLangConstants.TASK_NAVIGATION_KEY);
        return stringResultNavigationMap;
    }

}
