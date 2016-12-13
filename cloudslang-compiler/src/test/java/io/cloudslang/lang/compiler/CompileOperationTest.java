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

import io.cloudslang.lang.compiler.caching.CacheResult;
import io.cloudslang.lang.compiler.caching.CacheValueState;
import io.cloudslang.lang.compiler.caching.CachedPrecompileService;
import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.result.CompilationModellingResult;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.api.ExecutionStep;
import java.net.URL;
import java.util.List;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/*
 * Created by orius123 on 05/11/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CompileOperationTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    private SlangCompiler compiler;

    @Autowired
    private CachedPrecompileService cachedPrecompileService;

    @Test
    public void testCompileOperationBasic() throws Exception {
        URL resource = getClass().getResource("/test_op.sl");
        ExecutionPlan executionPlan = compiler.compile(SlangSource.fromFile(resource.toURI()), null).getExecutionPlan();
        assertNotNull("execution plan is null", executionPlan);
        assertEquals("there is a different number of steps than expected", 3, executionPlan.getSteps().size());
    }

    @Test
    public void testCompileOperationWithData() throws Exception {
        URL resource = getClass().getResource("/operation_with_data.sl");
        ExecutionPlan executionPlan = compiler.compile(SlangSource.fromFile(resource.toURI()), null).getExecutionPlan();

        ExecutionStep startStep = executionPlan.getStep(1L);
        @SuppressWarnings("unchecked")
        List<Input> inputs = (List<Input>) startStep.getActionData().get(ScoreLangConstants.EXECUTABLE_INPUTS_KEY);
        assertNotNull("inputs doesn't exist", inputs);
        assertEquals("there is a different number of inputs than expected", 13, inputs.size());

        ExecutionStep actionStep = executionPlan.getStep(2L);
        String script = (String) actionStep.getActionData().get(ScoreLangConstants.PYTHON_ACTION_SCRIPT_KEY);
        assertNotNull("script doesn't exist", script);
        Assert.assertTrue("script is different than expected", script.startsWith("# this is python amigos!!"));

        ExecutionStep endStep = executionPlan.getStep(3L);
        Object outputs = endStep.getActionData().get(ScoreLangConstants.EXECUTABLE_OUTPUTS_KEY);
        Object results = endStep.getActionData().get(ScoreLangConstants.EXECUTABLE_RESULTS_KEY);

        assertNotNull("outputs don't exist", outputs);
        assertNotNull("results don't exist", results);

    }

    @Test
    public void testPreCompileOperationBasic() throws Exception {
        URL resource = getClass().getResource("/check_Weather.sl");
        Executable operation = compiler.preCompile(SlangSource.fromFile(resource.toURI()));

        assertNotNull("preCompiledMetaData is null", operation);
        assertEquals("Operation name is wrong", "check_Weather", operation.getName());
        assertEquals("Operation namespace is wrong", "user.ops", operation.getNamespace());
        assertEquals("There is a different number of operation inputs than expected", 1, operation.getInputs().size());
        assertEquals("There is a different number of operation outputs than expected",
                2, operation.getOutputs().size());
        assertEquals("There is a different number of operation results than expected",
                2, operation.getResults().size());
        assertEquals("There is a different number of operation dependencies than expected",
                0, operation.getExecutableDependencies().size());
    }

    @Test
    public void testCompileOperationMissingClassName() throws Exception {
        URL resource = getClass().getResource("/corrupted/operation_missing_class_name.sl");
        exception.expect(RuntimeException.class);
        exception.expectMessage("Action syntax is illegal.\n" +
                "Following tags are missing: [" + SlangTextualKeys.JAVA_ACTION_CLASS_NAME_KEY);
        compiler.compile(SlangSource.fromFile(resource.toURI()), null);
    }

    @Test
    public void testCompileOperationMissingMethodName() throws Exception {
        URL resource = getClass().getResource("/corrupted/operation_missing_method_name.sl");
        exception.expect(RuntimeException.class);
        exception.expectMessage("Action syntax is illegal.\n" +
                "Following tags are missing: [" + SlangTextualKeys.JAVA_ACTION_METHOD_NAME_KEY);
        compiler.compile(SlangSource.fromFile(resource.toURI()), null);
    }

    @Test
    public void testCompileOperationInvalidActionProperty() throws Exception {
        URL resource = getClass().getResource("/corrupted/operation_invalid_action_property.sl");
        exception.expect(RuntimeException.class);
        exception.expectMessage("Action syntax is illegal.\n" +
                "Following tags are invalid: [IDontBelongHere]. " +
                "Please take a look at the supported features per versions link");
        compiler.compile(SlangSource.fromFile(resource.toURI()), null);
    }

    @Test
    public void testCompileOperationMultipleActionTypes() throws Exception {
        URL resource = getClass().getResource("/corrupted/operation_action_multiple_types.sl");
        exception.expect(RuntimeException.class);
        exception.expectMessage("Conflicting keys[java_action, python_action] at: operation_action_multiple_types");
        compiler.compile(SlangSource.fromFile(resource.toURI()), null);
    }

    @Test
    public void testCompileOperationMissingActionProperties() throws Exception {
        URL resource = getClass().getResource("/corrupted/operation_missing_action_properties.sl");
        exception.expect(RuntimeException.class);
        exception.expectMessage("Error compiling operation_missing_action_properties.sl. " +
                "Operation: operation_missing_action_properties has no action data");
        compiler.compile(SlangSource.fromFile(resource.toURI()), null);
    }

    @Test
    public void testCompileOperationMissingPythonScript() throws Exception {
        URL resource = getClass().getResource("/corrupted/operation_missing_python_script.sl");
        exception.expect(RuntimeException.class);
        exception.expectMessage("Error compiling operation_missing_python_script.sl. " +
                "Operation: operation_missing_python_script has no action data");
        compiler.compile(SlangSource.fromFile(resource.toURI()), null);
    }

    @Test
    public void testCompileOperationWithMissingNamespace() throws Exception {
        URL resource = getClass().getResource("/corrupted/op_without_namespace.sl");
        exception.expect(RuntimeException.class);
        exception.expectMessage("For source[op_without_namespace.sl] namespace cannot be empty.");
        compiler.compile(SlangSource.fromFile(resource.toURI()), null);
    }

    @Test
    public void testPrecompileCacheCleanup() throws Exception {
        URL resource = getClass().getResource("/corrupted/op_without_namespace.sl");
        SlangSource slangSource = SlangSource.fromFile(resource.toURI());

        CompilationModellingResult result = compiler.compileSource(slangSource, null);
        assertEquals("The compilation result should have one error", 1, result.getErrors().size());
        assertEquals("Wrong error message", "For source[op_without_namespace.sl] namespace cannot be empty.",
                result.getErrors().get(0).getMessage());

        assertNotNull("Cache should contain the ExecutableModellingResult before cache invalidateAllInPreCompileCache",
                cachedPrecompileService.getValueFromCache(slangSource.getFilePath(), slangSource));

        compiler.invalidateAllInPreCompileCache();

        CacheResult cacheResult = cachedPrecompileService.getValueFromCache(slangSource.getFilePath(), slangSource);
        assertEquals(
                "Cache should not contain the ExecutableModellingResult after cache invalidateAllInPreCompileCache",
                CacheValueState.MISSING,
                cacheResult.getState()
        );
    }

    @Test
    public void testPrecompileCacheDisabledByDefault() throws Exception {
        URL resource = getClass().getResource("/corrupted/op_without_namespace.sl");
        SlangSource slangSource = SlangSource.fromFile(resource.toURI());

        CompilationModellingResult result = compiler.compileSource(slangSource, null);
        assertEquals("The compilation result should have one error", 1, result.getErrors().size());
        assertEquals("Wrong error message", "For source[op_without_namespace.sl] namespace cannot be empty.",
                result.getErrors().get(0).getMessage());

        CacheResult cacheResult = cachedPrecompileService.getValueFromCache(slangSource.getFilePath(), slangSource);
        assertEquals(
                "Cache should not contain the ExecutableModellingResult after cache invalidateAllInPreCompileCache",
                CacheValueState.MISSING,
                cacheResult.getState()
        );
    }

}
