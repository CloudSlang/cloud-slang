/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

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
import org.openscore.lang.entities.ScoreLangConstants;
import org.openscore.lang.entities.bindings.Input;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URL;
import java.util.List;
import java.util.Map;

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
	public void testCompileOperationMissingImport() throws Exception {
		URL resource = getClass().getResource("/operation_with_missing_sys_props_imports.sl");
		exception.expect(RuntimeException.class);
		exception.expectMessage("import");
		compiler.compile(SlangSource.fromFile(resource.toURI()), null).getExecutionPlan();
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
        String script = (String) actionStep.getActionData().get(ScoreLangConstants.PYTHON_SCRIPT_KEY);
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
        Map<String, SlangFileType> dependencies = operation.getDependencies();
        Assert.assertEquals("There is a different number of operation dependencies than expected", 0, dependencies.size());
    }

    @Test
    public void testCompileOperationMissingClassName() throws Exception {
        URL resource = getClass().getResource("/invalid_syntax/operation_missing_className.yaml");
        exception.expect(RuntimeException.class);
        exception.expectMessage("className");
        compiler.compile(SlangSource.fromFile(resource.toURI()), "missing_class_name", null).getExecutionPlan();
    }

    @Test
    public void testCompileOperationMissingMethodName() throws Exception {
        URL resource = getClass().getResource("/invalid_syntax/operation_missing_methodName.yaml");
        exception.expect(RuntimeException.class);
        exception.expectMessage("methodName");
        compiler.compile(SlangSource.fromFile(resource.toURI()), "missing_method_name", null).getExecutionPlan();
    }

    @Test
    public void testCompileOperationInvalidActionProperty() throws Exception {
        URL resource = getClass().getResource("/invalid_syntax/operation_invalid_action_property.yaml");
        exception.expect(RuntimeException.class);
        exception.expectMessage("IDontBelongHere");
        compiler.compile(SlangSource.fromFile(resource.toURI()), "invalid_action_property", null).getExecutionPlan();
    }

    @Test
    public void testCompileOperationMultipleActionTypes() throws Exception {
        URL resource = getClass().getResource("/invalid_syntax/operation_action_multiple_types.yaml");
        exception.expect(RuntimeException.class);
        exception.expectMessage("java action");
        exception.expectMessage("python script");
        compiler.compile(SlangSource.fromFile(resource.toURI()), "operation_action_multiple_types", null).getExecutionPlan();
    }

    @Test
    public void testCompileOperationMissingActionProperties() throws Exception {
        URL resource = getClass().getResource("/invalid_syntax/operation_missing_action_properties.yaml");
        exception.expect(RuntimeException.class);
        exception.expectMessage("Invalid action data");
        compiler.compile(SlangSource.fromFile(resource.toURI()), "missing_action_properties", null).getExecutionPlan();
    }

    @Test
    public void testCompileOperationMissingPythonScript() throws Exception {
        URL resource = getClass().getResource("/invalid_syntax/operation_missing_python_script.yaml");
        exception.expect(RuntimeException.class);
        exception.expectMessage("Invalid action data");
        compiler.compile(SlangSource.fromFile(resource.toURI()), "missing_python_script", null).getExecutionPlan();
    }
}