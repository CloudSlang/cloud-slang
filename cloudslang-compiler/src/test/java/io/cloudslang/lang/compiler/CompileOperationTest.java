/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.compiler;

import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.entities.ScoreLangConstants;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.api.ExecutionStep;
import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.entities.bindings.Input;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URL;
import java.util.List;

/*
 * Created by orius123 on 05/11/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
public class CompileOperationTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    private SlangCompiler compiler;

    @Test
    public void testCompileOperationBasic() throws Exception {
        URL resource = getClass().getResource("/test_op.sl");
        ExecutionPlan executionPlan = compiler.compile(SlangSource.fromFile(resource.toURI()), null).getExecutionPlan();
        Assert.assertNotNull("execution plan is null", executionPlan);
        Assert.assertEquals("there is a different number of steps than expected", 3, executionPlan.getSteps().size());
    }

    @Test
    public void testCompileOperationWithData() throws Exception {
        URL resource = getClass().getResource("/operation_with_data.sl");
        ExecutionPlan executionPlan = compiler.compile(SlangSource.fromFile(resource.toURI()), null).getExecutionPlan();

        ExecutionStep startStep = executionPlan.getStep(1L);
        @SuppressWarnings("unchecked") List<Input> inputs = (List<Input>) startStep.getActionData().get(ScoreLangConstants.EXECUTABLE_INPUTS_KEY);
        Assert.assertNotNull("inputs doesn't exist", inputs);
        Assert.assertEquals("there is a different number of inputs than expected", 13, inputs.size());

        ExecutionStep actionStep = executionPlan.getStep(2L);
        String script = (String) actionStep.getActionData().get(ScoreLangConstants.PYTHON_ACTION_SCRIPT_KEY);
        Assert.assertNotNull("script doesn't exist", script);
        Assert.assertTrue("script is different than expected", script.startsWith("# this is python amigos!!"));

        ExecutionStep endStep = executionPlan.getStep(3L);
        Object outputs = endStep.getActionData().get(ScoreLangConstants.EXECUTABLE_OUTPUTS_KEY);
        Object results = endStep.getActionData().get(ScoreLangConstants.EXECUTABLE_RESULTS_KEY);

        Assert.assertNotNull("outputs don't exist", outputs);
        Assert.assertNotNull("results don't exist", results);

    }


    @Test
    public void testPreCompileOperationBasic() throws Exception {
        URL resource = getClass().getResource("/check_Weather.sl");
        Executable operation = compiler.preCompile(SlangSource.fromFile(resource.toURI()));

        Assert.assertNotNull("preCompiledMetaData is null", operation);
        Assert.assertEquals("Operation name is wrong", "check_Weather", operation.getName());
        Assert.assertEquals("Operation namespace is wrong", "user.ops", operation.getNamespace());
        Assert.assertEquals("There is a different number of operation inputs than expected", 1, operation.getInputs().size());
        Assert.assertEquals("There is a different number of operation outputs than expected", 2, operation.getOutputs().size());
        Assert.assertEquals("There is a different number of operation results than expected", 1, operation.getResults().size());
        Assert.assertEquals("There is a different number of operation dependencies than expected", 0, operation.getExecutableDependencies().size());
    }

    @Test
    public void testCompileOperationMissingClassName() throws Exception {
        URL resource = getClass().getResource("/corrupted/operation_missing_class_name.sl");
        exception.expect(RuntimeException.class);
        exception.expectMessage(SlangTextualKeys.JAVA_ACTION_CLASS_NAME_KEY);
        compiler.compile(SlangSource.fromFile(resource.toURI()), null);
    }

    @Test
    public void testCompileOperationMissingMethodName() throws Exception {
        URL resource = getClass().getResource("/corrupted/operation_missing_method_name.sl");
        exception.expect(RuntimeException.class);
        exception.expectMessage(SlangTextualKeys.JAVA_ACTION_METHOD_NAME_KEY);
        compiler.compile(SlangSource.fromFile(resource.toURI()), null);
    }

    @Test
    public void testCompileOperationInvalidActionProperty() throws Exception {
        URL resource = getClass().getResource("/corrupted/operation_invalid_action_property.sl");
        exception.expect(RuntimeException.class);
        exception.expectMessage("IDontBelongHere");
        compiler.compile(SlangSource.fromFile(resource.toURI()), null);
    }

    @Test
    public void testCompileOperationMultipleActionTypes() throws Exception {
        URL resource = getClass().getResource("/corrupted/operation_action_multiple_types.sl");
        exception.expect(RuntimeException.class);
        exception.expectMessage("Conflicting keys");
        compiler.compile(SlangSource.fromFile(resource.toURI()), null);
    }

    @Test
    public void testCompileOperationMissingActionProperties() throws Exception {
        URL resource = getClass().getResource("/corrupted/operation_missing_action_properties.sl");
        exception.expect(RuntimeException.class);
        exception.expectMessage("operation_missing_action_properties");
        exception.expectMessage("no action data");
        compiler.compile(SlangSource.fromFile(resource.toURI()), null);
    }

    @Test
    public void testCompileOperationMissingPythonScript() throws Exception {
        URL resource = getClass().getResource("/corrupted/operation_missing_python_script.sl");
        exception.expect(RuntimeException.class);
        exception.expectMessage("operation_missing_python_script");
        exception.expectMessage("no action data");
        compiler.compile(SlangSource.fromFile(resource.toURI()), null);
    }

    @Test
    public void testCompileOperationWithMissingNamespace() throws Exception {
        URL resource = getClass().getResource("/corrupted/op_without_namespace.sl");
        exception.expect(RuntimeException.class);
        exception.expectMessage("namespace");
        compiler.compile(SlangSource.fromFile(resource.toURI()), null);
    }
}