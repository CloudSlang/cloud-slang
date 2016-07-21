/**
 * ****************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 * <p/>
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * *****************************************************************************
 */
package io.cloudslang.lang.compiler;

import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.Result;
import java.net.URI;
import java.util.List;
import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Bonczidai Levente
 * @since 7/19/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
public class PreCompileTransformersTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    private SlangCompiler compiler;

    @Test
    public void testOpInvalidInput() throws Exception {
        URI resource = getClass().getResource("/corrupted/transformers/operation_input_private_no_default.sl").toURI();
        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));

        Assert.assertNotNull(result);

        Executable executable = result.getExecutable();
        Assert.assertNotNull(executable);
        List<Input> inputs = executable.getInputs();
        Assert.assertEquals(6, inputs.size());

        List<RuntimeException> errors = result.getErrors();
        Assert.assertNotNull(errors);
        Assert.assertTrue(errors.size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("For operation 'operation_input_private_no_default' syntax is illegal.");
        exception.expectMessage("Input: input_private_no_default is private but no default value was specified");
        throw errors.get(0);
    }

    @Test
    public void testOpWrongOutput() throws Exception {
        URI resource = getClass().getResource("/corrupted/transformers/operation_output_wrong_property.sl").toURI();
        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));

        Assert.assertNotNull(result);

        Executable executable = result.getExecutable();
        Assert.assertNotNull(executable);
        List<Output> outputs = executable.getOutputs();
        Assert.assertEquals(2, outputs.size());

        List<RuntimeException> errors = result.getErrors();
        Assert.assertNotNull(errors);
        Assert.assertTrue(errors.size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("For operation 'operation_output_wrong_property' syntax is illegal.");
        exception.expectMessage("Key: wrong_key in output: output_wrong_key is not a known property");
        throw errors.get(0);
    }

    @Test
    public void testOpWrongResult() throws Exception {
        URI resource = getClass().getResource("/corrupted/transformers/operation_duplicate_result.sl").toURI();
        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));

        Assert.assertNotNull(result);

        Executable executable = result.getExecutable();
        Assert.assertNotNull(executable);
        List<Result> results = executable.getResults();
        Assert.assertEquals(2, results.size());

        List<RuntimeException> errors = result.getErrors();
        Assert.assertNotNull(errors);
        Assert.assertTrue(errors.size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("For operation 'operation_duplicate_result' syntax is illegal.");
        exception.expectMessage("Duplicate result found: SUCCESS");
        throw errors.get(0);
    }

}
