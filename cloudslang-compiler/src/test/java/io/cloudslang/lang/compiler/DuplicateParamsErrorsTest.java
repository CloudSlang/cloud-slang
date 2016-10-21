/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler;

import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;

import java.net.URI;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;

/**
 * Created by Ifat Gavish on 29/02/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
public class DuplicateParamsErrorsTest {

    @Autowired
    private SlangCompiler compiler;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testOpDuplicateInputLowercase() throws Exception {
        URI resource = getClass().getResource("/corrupted/duplicate/duplicate_op_input_lowercase.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        assertTrue(result.getExecutable().getInputs().size() == 4);
        assertTrue(result.getExecutable().getInputs().get(1).getName()
                .equals(result.getExecutable().getInputs().get(3).getName()));
        exception.expect(RuntimeException.class);
        exception.expect(RuntimeException.class);
        exception.expectMessage("For operation 'duplicate_op_input_lowercase' syntax is illegal.");
        exception.expectMessage("Duplicate input found: city");
        throw result.getErrors().get(0);
    }

    @Test
    public void testOpDuplicateInputIgnoreCase() throws Exception {
        URI resource = getClass().getResource("/corrupted/duplicate/duplicate_op_input_ignore_case.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        assertTrue(result.getExecutable().getInputs().size() == 4);
        assertTrue(result.getExecutable().getInputs().get(1).getName()
                .equalsIgnoreCase(result.getExecutable().getInputs().get(3).getName()));
        exception.expect(RuntimeException.class);
        exception.expectMessage("For operation 'duplicate_op_input_ignore_case' syntax is illegal.");
        exception.expectMessage("Duplicate input found: City");
        throw result.getErrors().get(0);
    }

    @Test
    public void testFlowDuplicateInputLowercase() throws Exception {
        URI resource = getClass().getResource("/corrupted/duplicate/duplicate_flow_input_lowercase.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("For flow 'duplicate_flow_input_lowercase' syntax is illegal.");
        exception.expectMessage("Duplicate input found: city");
        throw result.getErrors().get(0);
    }

    @Test
    public void testFlowDuplicateInputIgnoreCase() throws Exception {
        URI resource = getClass().getResource("/corrupted/duplicate/duplicate_flow_input_ignore_case.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("For flow 'duplicate_flow_input_ignore_case' syntax is illegal.");
        exception.expectMessage("Duplicate input found: City");
        throw result.getErrors().get(0);
    }

    @Test
    public void testStepDuplicateInputLowerCase() throws Exception {
        URI resource = getClass().getResource("/corrupted/duplicate/duplicate_step_input_lowercase.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("For step 'step1' syntax is illegal.");
        exception.expectMessage("Duplicate step input found: city");
        throw result.getErrors().get(0);
    }

    @Test
    public void testStepDuplicateIgnoreLowerCase() throws Exception {
        URI resource = getClass().getResource("/corrupted/duplicate/duplicate_step_input_ignore_case.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("For step 'step1' syntax is illegal.");
        exception.expectMessage("Duplicate step input found: City");
        throw result.getErrors().get(0);
    }

    @Test
    public void testOpDuplicateOutputLowercase() throws Exception {
        URI resource = getClass().getResource("/corrupted/duplicate/duplicate_op_output_lowercase.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("For operation 'duplicate_op_output_lowercase' syntax is illegal.");
        exception.expectMessage("Duplicate output / publish value found: city");
        throw result.getErrors().get(0);
    }

    @Test
    public void testOpDuplicateOutputIgnoreCase() throws Exception {
        URI resource = getClass().getResource("/corrupted/duplicate/duplicate_op_output_ignore_case.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("For operation 'duplicate_op_output_ignore_case' syntax is illegal.");
        exception.expectMessage("Duplicate output / publish value found: City");
        throw result.getErrors().get(0);
    }

    @Test
    public void testFlowDuplicateOutputLowercase() throws Exception {
        URI resource = getClass().getResource("/corrupted/duplicate/duplicate_flow_output_lowercase.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("For flow 'duplicate_flow_output_lowercase' syntax is illegal.");
        exception.expectMessage("Duplicate output / publish value found: city");
        throw result.getErrors().get(0);
    }

    @Test
    public void testFlowDuplicateOutputIgnoreCase() throws Exception {
        URI resource = getClass().getResource("/corrupted/duplicate/duplicate_flow_output_ignore_case.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("For flow 'duplicate_flow_output_ignore_case' syntax is illegal.");
        exception.expectMessage("Duplicate output / publish value found: City");
        throw result.getErrors().get(0);
    }

    @Test
    public void testStepDuplicatePublishValueLowercase() throws Exception {
        URI resource = getClass().getResource("/corrupted/duplicate/duplicate_step_publish_value_lowercase.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("For step 'step1' syntax is illegal.");
        exception.expectMessage("Duplicate output / publish value found: city");
        throw result.getErrors().get(0);
    }

    @Test
    public void testStepDuplicatePublishValueIgnoreCase() throws Exception {
        URI resource = getClass()
                .getResource("/corrupted/duplicate/duplicate_step_publish_value_ignore_case.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("For step 'step1' syntax is illegal.");
        exception.expectMessage("Duplicate output / publish value found: City");
        throw result.getErrors().get(0);
    }

    @Test
    public void testOpDuplicateResult() throws Exception {
        URI resource = getClass().getResource("/corrupted/duplicate/duplicate_op_result.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("For operation 'duplicate_op_result' syntax is illegal.");
        exception.expectMessage("Duplicate result found: SUCCESS");
        throw result.getErrors().get(0);
    }

    @Test
    public void testFlowDuplicateResult() throws Exception {
        URI resource = getClass().getResource("/corrupted/duplicate/duplicate_flow_result.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("For flow 'duplicate_flow_result' syntax is illegal.");
        exception.expectMessage("Duplicate result found: SUCCESS");
        throw result.getErrors().get(0);
    }

}
