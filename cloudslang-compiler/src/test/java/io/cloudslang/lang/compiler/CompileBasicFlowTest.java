/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.compiler;

import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ResultNavigation;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.api.ExecutionStep;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/*
 * Created by orius123 on 05/11/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
public class CompileBasicFlowTest {

    @Autowired
    private SlangCompiler compiler;

    @Test
    public void testCompileFlowBasic() throws Exception {
        URI flow = getClass().getResource("/basic_flow.yaml").toURI();
        URI operation = getClass().getResource("/test_op.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));
        CompilationArtifact compilationArtifact = compiler.compile(SlangSource.fromFile(flow), path);
        ExecutionPlan executionPlan = compilationArtifact.getExecutionPlan();
        Assert.assertNotNull("execution plan is null", executionPlan);
        Assert.assertEquals("there is a different number of steps than expected", 4, executionPlan.getSteps().size());
        Assert.assertEquals("execution plan name is different than expected", "basic_flow", executionPlan.getName());
        Assert.assertEquals("the dependencies size is not as expected", 1, compilationArtifact.getDependencies().size());
        Assert.assertEquals("the inputs size is not as expected", 3, compilationArtifact.getInputs().size());
    }

    @Test
    public void testCompileFlowWithData() throws Exception {
        URI flow = getClass().getResource("/flow_with_data.yaml").toURI();
        URI operation = getClass().getResource("/check_Weather.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));

        CompilationArtifact compilationArtifact = compiler.compile(SlangSource.fromFile(flow), path);
        ExecutionPlan executionPlan = compilationArtifact.getExecutionPlan();
        Assert.assertNotNull("execution plan is null", executionPlan);
        Assert.assertEquals("there is a different number of steps than expected", 4, executionPlan.getSteps().size());
        Assert.assertEquals("execution plan name is different than expected", "flow_with_data", executionPlan.getName());
        Assert.assertEquals("the dependencies size is not as expected", 1, compilationArtifact.getDependencies().size());

        ExecutionStep startStep = executionPlan.getStep(1L);
        @SuppressWarnings("unchecked") List<Input> inputs = (List<Input>) startStep.getActionData().get(ScoreLangConstants.EXECUTABLE_INPUTS_KEY);
        Assert.assertNotNull("inputs doesn't exist", inputs);
        Assert.assertEquals("there is a different number of inputs than expected", 1, inputs.size());

        ExecutionStep beginStepExecutionStep = executionPlan.getStep(2L);
        @SuppressWarnings("unchecked") List<Argument> stepInputs = (List<Argument>) beginStepExecutionStep.getActionData().get(ScoreLangConstants.STEP_INPUTS_KEY);
        Assert.assertNotNull("arguments doesn't exist", stepInputs);
        Assert.assertEquals("there is a different number of arguments than expected", 3, stepInputs.size());
        Assert.assertEquals("city", stepInputs.get(0).getName());
        Assert.assertEquals("country", stepInputs.get(1).getName());
        Assert.assertEquals("CheckWeather", beginStepExecutionStep.getActionData().get(ScoreLangConstants.NODE_NAME_KEY));

        ExecutionStep endStepExecutionStep = executionPlan.getStep(3L);
        @SuppressWarnings("unchecked") List<Output> publish = (List<Output>) endStepExecutionStep.getActionData().get(ScoreLangConstants.STEP_PUBLISH_KEY);
        @SuppressWarnings("unchecked") Map<String, ResultNavigation> navigate =
                (Map<String, ResultNavigation>) endStepExecutionStep.getActionData().get(ScoreLangConstants.STEP_NAVIGATION_KEY);
        Assert.assertEquals("CheckWeather", endStepExecutionStep.getActionData().get(ScoreLangConstants.NODE_NAME_KEY));

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
        URI flowUri = getClass().getResource("/basic_flow.yaml").toURI();
        Executable flow = compiler.preCompile(SlangSource.fromFile(flowUri));

        Assert.assertNotNull("Pre-Compiled meta-data is null", flow);
        Assert.assertEquals("Flow name is wrong", "basic_flow", flow.getName());
        Assert.assertEquals("Flow namespace is wrong", "user.ops", flow.getNamespace());
        Assert.assertEquals("There is a different number of flow inputs than expected", 3, flow.getInputs().size());
        Assert.assertEquals("There is a different number of flow outputs than expected", 0, flow.getOutputs().size());
        Assert.assertEquals("There is a different number of flow results than expected", 2, flow.getResults().size());
        Set<String> dependencies = flow.getExecutableDependencies();
        Assert.assertEquals("There is a different number of flow dependencies than expected", 1, dependencies.size());
        String dependency = dependencies.iterator().next();
        Assert.assertEquals("The flow dependency full name is wrong", "user.ops.test_op", dependency);
    }

    @Test
    public void testValidateSlangModelWithDependenciesBasic() throws Exception {
        URI flowUri = getClass().getResource("/basic_flow.yaml").toURI();
        Executable flow = compiler.preCompile(SlangSource.fromFile(flowUri));

        URI operationUri = getClass().getResource("/test_op.sl").toURI();
        Executable op = compiler.preCompile(SlangSource.fromFile(operationUri));

        Set<Executable> dependencies = new HashSet<>();
        dependencies.add(op);
        List<RuntimeException> errors = compiler.validateSlangModelWithDirectDependencies(flow, dependencies);

        Assert.assertEquals("", 0, errors.size());
    }


    @Test
    public void testValidFlowWithMissingDependencyRequiredInputInGrandchild() throws Exception {
        URI flowUri = getClass().getResource("/corrupted/flow_missing_dependency_required_input_in_grandchild.sl").toURI();
        Executable flowModel = compiler.preCompile(SlangSource.fromFile(flowUri));

        URI operation2Uri = getClass().getResource("/check_op.sl").toURI();
        Executable operation2Model = compiler.preCompile(SlangSource.fromFile(operation2Uri));
        URI subFlowUri = getClass().getResource("/flow_implicit_alias_for_current_namespace.sl").toURI();
        Executable subFlowModel = compiler.preCompile(SlangSource.fromFile(subFlowUri));
        Set<Executable> dependencies = new HashSet<>();
        dependencies.add(subFlowModel);
        dependencies.add(operation2Model);
        List<RuntimeException> errors = compiler.validateSlangModelWithDirectDependencies(flowModel, dependencies);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testCompileFlowNavigateDuplicateKeysFirstIsTaken() throws Exception {
        URI flow = getClass().getResource("/flow_navigate_duplicate_keys.sl").toURI();
        URI operation = getClass().getResource("/check_Weather.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));

        CompilationArtifact compilationArtifact = compiler.compile(SlangSource.fromFile(flow), path);
        ExecutionPlan executionPlan = compilationArtifact.getExecutionPlan();
        Assert.assertNotNull("execution plan is null", executionPlan);
        Assert.assertEquals("there is a different number of steps than expected", 4, executionPlan.getSteps().size());

        ExecutionStep endStepExecutionStep = executionPlan.getStep(3L);
        @SuppressWarnings("unchecked") Map<String, ResultNavigation> actualNavigationValues =
                (Map<String, ResultNavigation>) endStepExecutionStep.getActionData().get(ScoreLangConstants.STEP_NAVIGATION_KEY);
        Map<String, ResultNavigation> expectedNavigationValues = new HashMap<>();
        expectedNavigationValues.put("SUCCESS", new ResultNavigation(0, "RESULT1")); // first in list is taken
        expectedNavigationValues.put("FAILURE", new ResultNavigation(0, "RESULT2"));
        Assert.assertEquals("navigation values not as expected", expectedNavigationValues, actualNavigationValues);
    }

}
