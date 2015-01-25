/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.openscore.lang.compiler;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.openscore.api.ExecutionPlan;
import org.openscore.api.ExecutionStep;
import org.openscore.lang.compiler.configuration.SlangCompilerSpringConfig;
import org.openscore.lang.compiler.model.Executable;
import org.openscore.lang.compiler.model.SlangFileType;
import org.openscore.lang.entities.CompilationArtifact;
import org.openscore.lang.entities.ResultNavigation;
import org.openscore.lang.entities.ScoreLangConstants;
import org.openscore.lang.entities.bindings.Input;
import org.openscore.lang.entities.bindings.Output;
import org.openscore.lang.entities.bindings.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Created by orius123 on 05/11/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
public class CompileBasicFlowTest {

    @Autowired
    private SlangCompiler compiler;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testCompileFlowBasic() throws Exception {
        URI flow = getClass().getResource("/flow.yaml").toURI();
        URI operation = getClass().getResource("/operation.yaml").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));
        CompilationArtifact compilationArtifact = compiler.compileFlow(SlangSource.fromFile(flow), path);
        ExecutionPlan executionPlan = compilationArtifact.getExecutionPlan();
        Assert.assertNotNull("execution plan is null", executionPlan);
        Assert.assertEquals("there is a different number of steps than expected", 4, executionPlan.getSteps().size());
        Assert.assertEquals("execution plan name is different than expected", "basic_flow", executionPlan.getName());
        Assert.assertEquals("the dependencies size is not as expected", 1, compilationArtifact.getDependencies().size());
        Assert.assertEquals("the inputs size is not as expected", 2, compilationArtifact.getInputs().size());
    }

    @Test
    public void testCompileFlowWithData() throws Exception {
        URI flow = getClass().getResource("/flow_with_data.yaml").toURI();
        URI operation = getClass().getResource("/operation.yaml").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));

        CompilationArtifact compilationArtifact = compiler.compileFlow(SlangSource.fromFile(flow), path);
        ExecutionPlan executionPlan = compilationArtifact.getExecutionPlan();
        Assert.assertNotNull("execution plan is null", executionPlan);
        Assert.assertEquals("there is a different number of steps than expected", 4, executionPlan.getSteps().size());
        Assert.assertEquals("execution plan name is different than expected", "SimpleFlow", executionPlan.getName());
        Assert.assertEquals("the dependencies size is not as expected", 1, compilationArtifact.getDependencies().size());

        ExecutionStep startStep = executionPlan.getStep(1L);
        @SuppressWarnings("unchecked") List<Input> inputs = (List<Input>) startStep.getActionData().get(ScoreLangConstants.EXECUTABLE_INPUTS_KEY);
        Assert.assertNotNull("inputs doesn't exist", inputs);
        Assert.assertEquals("there is a different number of inputs than expected", 1, inputs.size());

        ExecutionStep beginTaskStep = executionPlan.getStep(2L);
        @SuppressWarnings("unchecked") List<Input> taskArguments = (List<Input>) beginTaskStep.getActionData().get(ScoreLangConstants.TASK_INPUTS_KEY);
        Assert.assertNotNull("arguments doesn't exist", taskArguments);
        Assert.assertEquals("there is a different number of arguments than expected", 2, taskArguments.size());
        Assert.assertEquals("city", taskArguments.get(0).getName());
        Assert.assertEquals("country", taskArguments.get(1).getName());
        Assert.assertEquals("CheckWeather", beginTaskStep.getActionData().get(ScoreLangConstants.NODE_NAME_KEY));

        ExecutionStep FinishTaskSteps = executionPlan.getStep(3L);
        @SuppressWarnings("unchecked") List<Output> publish = (List<Output>) FinishTaskSteps.getActionData().get(ScoreLangConstants.TASK_PUBLISH_KEY);
        @SuppressWarnings("unchecked") Map<String, ResultNavigation> navigate =
                (Map<String, ResultNavigation>) FinishTaskSteps.getActionData().get(ScoreLangConstants.TASK_NAVIGATION_KEY);
        Assert.assertEquals("CheckWeather", FinishTaskSteps.getActionData().get(ScoreLangConstants.NODE_NAME_KEY));

        Assert.assertNotNull("publish don't exist", publish);
        Assert.assertEquals("there is a different number of publish values than expected", 1, publish.size());
        Assert.assertNotNull("navigate don't exist", navigate);
        Assert.assertEquals("last step default success should go to flow success",
                ScoreLangConstants.SUCCESS_RESULT, navigate.get(ScoreLangConstants.SUCCESS_RESULT).getPresetResult());
        Assert.assertEquals("last step default failure should go to flow failure",
                ScoreLangConstants.FAILURE_RESULT, navigate.get(ScoreLangConstants.FAILURE_RESULT).getPresetResult());
        Assert.assertEquals("there is a different number of navigation values than expected", 2, navigate.size());


        ExecutionStep endStep = executionPlan.getStep(0L);
        @SuppressWarnings("unchecked") List<Output> outputs = (List<Output>) endStep.getActionData().get(ScoreLangConstants.EXECUTABLE_OUTPUTS_KEY);
        Assert.assertNotNull("outputs don't exist", outputs);
        Assert.assertEquals("there is a different number of outputs than expected", 1, outputs.size());

        @SuppressWarnings("unchecked") List<Result> results = (List<Result>) endStep.getActionData().get(ScoreLangConstants.EXECUTABLE_RESULTS_KEY);
        Assert.assertNotNull("results don't exist", results);
        Assert.assertEquals("there is a different number of results values than expected", 2, results.size());
    }

    @Test
    public void testPreCompileFlowBasic() throws Exception {
        URI flowUri = getClass().getResource("/flow.yaml").toURI();
        List<Executable> preCompiledFlow = compiler.preCompile(SlangSource.fromFile(flowUri));
        Executable flow = preCompiledFlow.get(0);

        Assert.assertNotNull("Pre-Compiled meta-data is null", flow);
        Assert.assertEquals("Flow name is wrong", "basic_flow", flow.getName());
        Assert.assertEquals("Flow namespace is wrong", "user.ops", flow.getNamespace());
        Assert.assertEquals("There is a different number of flow inputs than expected", 2, flow.getInputs().size());
        Assert.assertEquals("There is a different number of flow outputs than expected", 0, flow.getOutputs().size());
        Assert.assertEquals("There is a different number of flow results than expected", 2, flow.getResults().size());
        Map<String, SlangFileType> dependencies = flow.getDependencies();
        Assert.assertEquals("There is a different number of flow dependencies than expected", 1, dependencies.size());
        Map.Entry<String, SlangFileType> dependency = dependencies.entrySet().iterator().next();
        Assert.assertEquals("There is a different number of flow inputs than expected", SlangFileType.EXECUTABLE, dependency.getValue());
        Assert.assertEquals("The flow dependency full name is wrong", "user.ops.test_op", dependency.getKey());
    }

	@Test
	public void testLoadSystemProperties() throws Exception {
		Map<String, Serializable> expected = new HashMap<>();
		expected.put("test.sys.props.host", "localhost");
		expected.put("test.sys.props.port", 22);
		expected.put("test.sys.props.alla", "balla");
		URI systemProperties = getClass().getResource("/system_properties.yaml").toURI();
		Map<String, ? extends Serializable> result = compiler.loadSystemProperties(SlangSource.fromFile(systemProperties));
		Assert.assertNotNull(result);
		Assert.assertEquals(expected, result);
	}


    @Test
    public void testFlowWithWrongNavigation() throws Exception {
        URI resource = getClass().getResource("/flow_with_navigation_to_missing_task.sl").toURI();
        URI operations = getClass().getResource("/operation.yaml").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operations));
        exception.expect(RuntimeException.class);
        exception.expectMessage("Task1");
        exception.expectMessage("Task2");
        exception.expectMessage("navigation");
        compiler.compileFlow(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithNavigationToMissingDefaultResults() throws Exception {
        URI resource = getClass().getResource("/flow_with_navigation_to_missing_default_results.sl").toURI();
        URI operations = getClass().getResource("/operation.yaml").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operations));
        exception.expect(RuntimeException.class);
        exception.expectMessage("Task1");
        exception.expectMessage("SUCCESS");
        exception.expectMessage("navigation");
        compiler.compileFlow(SlangSource.fromFile(resource), path);
    }

}