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

import ch.lambdaj.Lambda;
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

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalTo;

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
        URL resource = getClass().getResource("/operation.yaml");
//URL vars = getClass().getResource("/variables.yaml");
//ExecutionPlan executionPlan = compiler.compile(SlangSource.fromFile(resource.toURI()), "test_op", Collections.singleton(SlangSource.fromFile(vars.toURI()))).getExecutionPlan();
        ExecutionPlan executionPlan = compiler.compile(SlangSource.fromFile(resource.toURI()), "test_op", null).getExecutionPlan();
        Assert.assertNotNull("execution plan is null", executionPlan);
        Assert.assertEquals("there is a different number of steps than expected", 3, executionPlan.getSteps().size());
    }

	@Test
	public void testCompileOperationMissingImport() throws Exception {
		URL resource = getClass().getResource("/operation_mi.yaml");
//URL vars = getClass().getResource("/variables.yaml");
//ExecutionPlan executionPlan = compiler.compile(SlangSource.fromFile(resource.toURI()), "test_op", Collections.singleton(SlangSource.fromFile(vars.toURI()))).getExecutionPlan();
		exception.expect(RuntimeException.class);
		exception.expectMessage("import");
		compiler.compile(SlangSource.fromFile(resource.toURI()), "test_op", null).getExecutionPlan();
	}

    @Test
    public void wrongOperationName() throws Exception {
        URL resource = getClass().getResource("/operation.yaml");
        exception.expect(RuntimeException.class);
        exception.expectMessage("operation.yaml");
        exception.expectMessage("blah");
        compiler.compile(SlangSource.fromFile(resource.toURI()), "blah", null).getExecutionPlan();
    }

    @Test
    public void testCompileOperationWithData() throws Exception {
        URL resource = getClass().getResource("/operation_with_data.yaml");
        ExecutionPlan executionPlan = compiler.compile(SlangSource.fromFile(resource.toURI()), "test_op_2", null).getExecutionPlan();

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
        URL resource = getClass().getResource("/operation.yaml");
        List<Executable> preCompiledOperations = compiler.preCompile(SlangSource.fromFile(resource.toURI()));
        Executable operation = Lambda.selectFirst(preCompiledOperations, having(on(Executable.class).getName(), equalTo("check_Weather")));

        Assert.assertNotNull("preCompiledMetaData is null", operation);
        Assert.assertEquals("Operation name is wrong", "check_Weather", operation.getName());
        Assert.assertEquals("Operation namespace is wrong", "user.ops", operation.getNamespace());
        Assert.assertEquals("There is a different number of operation inputs than expected", 1, operation.getInputs().size());
        Assert.assertEquals("There is a different number of operation outputs than expected", 2, operation.getOutputs().size());
        Assert.assertEquals("There is a different number of operation results than expected", 1, operation.getResults().size());
        Map<String, SlangFileType> dependencies = operation.getDependencies();
        Assert.assertEquals("There is a different number of operation dependencies than expected", 0, dependencies.size());
    }


}