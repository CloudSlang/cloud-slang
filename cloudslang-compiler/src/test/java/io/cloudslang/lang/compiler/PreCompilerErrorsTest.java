/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

package io.cloudslang.lang.compiler;

import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.compiler.modeller.ExecutableBuilder;
import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Ifat Gavish on 29/02/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
public class PreCompilerErrorsTest {

    @Autowired
    private SlangCompiler compiler;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testNotOpFlowFile() throws Exception {
        URI resource = getClass().getResource("/corrupted/no_op_flow_file.sl").toURI();

        exception.expect(RuntimeException.class);
        exception.expectMessage("flow/operation");
        compiler.preCompileSource(SlangSource.fromFile(resource));
    }

    @Test
    public void testOpWithMissingNamespace() throws Exception {
        URI resource = getClass().getResource("/corrupted/op_without_namespace.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("must have a namespace");
        throw result.getErrors().get(0);
    }

    @Test
    public void testFlowWithMissingName() throws Exception {
        URI resource = getClass().getResource("/corrupted/missing_name_flow.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("name");
        throw result.getErrors().get(0);
    }

    @Test
    public void testOperationWithNullFileName() throws Exception {
        URI resource = getClass().getResource("/corrupted/wrong_name_operation.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(new SlangSource(SlangSource.fromFile(resource).getSource(), null));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("should be declared in a file named \"test_op\" " +
                "plus a valid extension(sl, sl.yaml, sl.yml, prop.sl, yaml, yml)");
        throw result.getErrors().get(0);
    }

    @Test
     public void testOperationWithWrongName() throws Exception {
        URI resource = getClass().getResource("/corrupted/wrong_name_operation.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("should be declared in a file named \"test_op.sl\"");
        throw result.getErrors().get(0);
    }

    @Test
     public void testOperationWithWrongNameSLYAMLExtension() throws Exception {
        URI resource = getClass().getResource("/corrupted/wrong_name_operation.sl.yaml").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("should be declared in a file named \"test_op.sl.yaml\"");
        throw result.getErrors().get(0);
    }

    @Test
    public void testOperationWithWrongNameSLYMLExtension() throws Exception {
        URI resource = getClass().getResource("/corrupted/wrong_name_operation.sl.yml").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("should be declared in a file named \"test_op.sl.yml\"");
        throw result.getErrors().get(0);
    }

    @Test
    public void testOperationWithWrongNameYAMLExtension() throws Exception {
        URI resource = getClass().getResource("/corrupted/wrong_name_operation.yaml").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("should be declared in a file named \"test_op.yaml\"");
        throw result.getErrors().get(0);
    }

    @Test
    public void testOperationWithWrongNameYMLExtension() throws Exception {
        URI resource = getClass().getResource("/corrupted/wrong_name_operation.yml").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("should be declared in a file named \"test_op.yml\"");
        throw result.getErrors().get(0);
    }

    @Test
    public void testOperationWithWrongNameWrongExtension() throws Exception {
        URI resource = getClass().getResource("/corrupted/wrong_name_operation.wrong").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("it should be declared in a file named \"test_op\" " +
                "plus a valid extension(sl, sl.yaml, sl.yml, prop.sl, yaml, yml)");
        throw result.getErrors().get(0);
    }

    @Test
    public void testFlowWithInputsAsString() throws Exception {
        URI resource = getClass().getResource("/corrupted/inputs_type_string_flow.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("inputs");
        exception.expectMessage("list");
        exception.expectMessage("string");
        throw result.getErrors().get(0);
    }

    @Test
    public void testFlowWithInputsAsMap() throws Exception {
        URI resource = getClass().getResource("/corrupted/inputs_type_map_flow.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("inputs");
        exception.expectMessage("list");
        exception.expectMessage("map");
        throw result.getErrors().get(0);
    }

    @Test
    public void testFlowWithIllegalTypeInput() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_with_wrong_type_input.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("Input");
        exception.expectMessage("3");
        throw result.getErrors().get(0);
    }

    @Test
    public void testFlowWithNoWorkflow() throws Exception {
        URI resource = getClass().getResource("/corrupted/no_workflow_flow.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("no_workflow");
        exception.expectMessage("workflow");
        exception.expectMessage("property");
        throw result.getErrors().get(0);
    }

    @Test
    public void testFlowWithNoWorkflowData() throws Exception {
        URI resource = getClass().getResource("/corrupted/no_workflow_data_flow.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("no_workflow");
        exception.expectMessage("workflow");
        exception.expectMessage("data");
        throw result.getErrors().get(0);
    }

    @Test
    public void testFlowStepWithNoData() throws Exception {
        URI resource = getClass().getResource("/corrupted/no_step_data_flow.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("step1");
        exception.expectMessage("data");
        throw result.getErrors().get(0);
    }

    @Test
    public void testFlowStepWithTwoKeysUnderDo() throws Exception {
        URI resource = getClass().getResource("/corrupted/multiple_keys_under_do.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("step1");
        exception.expectMessage("to many keys");
        throw result.getErrors().get(0);
    }

    @Test
    public void testFlowWithStepsAsList() throws Exception {
        URI resource = getClass().getResource("/corrupted/workflow_with_step_map.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("workflow_with_step_map");
        exception.expectMessage("map");
        exception.expectMessage("list");
        throw result.getErrors().get(0);
    }

    @Test
    public void testFlowWithOnFailureStepsAsList() throws Exception {
        URI resource = getClass().getResource("/corrupted/on_failure_with_step_map.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("on_failure_with_step_map");
        exception.expectMessage("map");
        exception.expectMessage("list");
        throw result.getErrors().get(0);
    }

    @Test
    public void testFlowWithNoRefStep() throws Exception {
        URI resource = getClass().getResource("/corrupted/step_with_no_ref_flow.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("step1");
        exception.expectMessage("reference");
        throw result.getErrors().get(0);
    }

    @Test
    public void testStepWithListOfOps() throws Exception {
        URI resource = getClass().getResource("/corrupted/step_with_list_of_ops.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("step1");
        exception.expectMessage("map");
        exception.expectMessage("list");
        exception.expectMessage("-");
        throw result.getErrors().get(0);
    }

    @Test
    public void testStepWithListOfDos() throws Exception {
        URI resource = getClass().getResource("/corrupted/step_with_list_of_do_flow.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("step1");
        exception.expectMessage("map");
        exception.expectMessage("do:");
        throw result.getErrors().get(0);
    }

    @Test
    public void testInputPrivateAndNoDefault() throws Exception {
        URI resource = getClass().getResource("/private_input_without_default.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("private");
        exception.expectMessage("default");
        exception.expectMessage("input_without_default");
        throw result.getErrors().get(0);
    }

    @Test
    public void testInputWithInvalidKey() throws Exception {
        URI resource = getClass().getResource("/illegal_key_in_input.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("known property");
        exception.expectMessage("input_with_illegal_key");
        exception.expectMessage("karambula");
        throw result.getErrors().get(0);
    }

    @Test
    public void testOperationWithNoActionData() throws Exception {
        URI resource = getClass().getResource("/corrupted/operation_with_no_action_data.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("operation_with_no_action_data");
        exception.expectMessage("action");
        exception.expectMessage("data");
        throw result.getErrors().get(0);
    }

    @Test
    public void testOperationWithListOfActionTypes() throws Exception {
        URI resource = getClass().getResource("/corrupted/operation_with_list_of_action_types.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("'python_action'");
        exception.expectMessage("there should be a map of values, but instead there is a list");
        throw result.getErrors().get(0);
    }

    @Test
    public void testParentFlowWithCorruptedSubFlow() throws Exception {
        URI subFlow = getClass().getResource("/corrupted/no_step_data_flow.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(subFlow));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("step1");
        throw result.getErrors().get(0);
    }

    @Test
    public void testStepWithNavigateAsString() throws Exception {
        URI resource = getClass().getResource("/corrupted/step_with_string_navigate_value.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("'navigate'");
        exception.expectMessage("list");
        exception.expectMessage("string");
        throw result.getErrors().get(0);
    }

    @Test
    public void testStepWithIllegalTypeOfNavigate() throws Exception {
        URI resource = getClass().getResource("/corrupted/step_with_illegal_navigate_type.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("step1");
        exception.expectMessage("navigate");
        exception.expectMessage("3");
        throw result.getErrors().get(0);
    }

    @Test
    public void testDuplicateStepNamesInFlow() throws Exception {
        URI resource = getClass().getResource("/corrupted/duplicate_step_name.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("Step1");
        exception.expectMessage("unique");
        throw result.getErrors().get(0);
    }

    @Test
    public void testNullValueInputFlow() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_with_null_value_input.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("Input");
        exception.expectMessage("input1");
        exception.expectMessage("null");
        throw result.getErrors().get(0);
    }

    @Test
    public void testFlowWithResultExpressions() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_with_result_expressions.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));

        List<RuntimeException> errors = result.getErrors();
        assertEquals(2, errors.size());

        validateExceptionMessage(
                errors.get(0),
                "flow_with_result_expressions",
                "SUCCESS",
                ExecutableBuilder.FLOW_RESULTS_WITH_EXPRESSIONS_MESSAGE
        );
        validateExceptionMessage(
                errors.get(1),
                "flow_with_result_expressions",
                "CUSTOM",
                ExecutableBuilder.FLOW_RESULTS_WITH_EXPRESSIONS_MESSAGE
        );
    }

    private void validateExceptionMessage(
            RuntimeException ex,
            String flowName,
            String resultName,
            String expressionMessage) {
        String errorMessage = ex.getMessage();
        assertTrue(errorMessage.contains(flowName));
        assertTrue(errorMessage.contains(resultName));
        assertTrue(errorMessage.contains(expressionMessage));
    }


    @Test
    public void testFlowNavigateNull() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_navigate_map.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("Step1");
        exception.expectMessage("navigate");
        exception.expectMessage("instead there is a map");
        throw result.getErrors().get(0);
    }

    @Test
    public void testFlowNavigateMultipleElementsForRule() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_navigate_multiple_elements_for_rule.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("Step1");
        exception.expectMessage("navigate");
        exception.expectMessage("exactly one key:value pair");
        throw result.getErrors().get(0);
    }

    @Test
    public void testFlowNavigateIntKey() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_navigate_int_key.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("Step1");
        exception.expectMessage("navigate");
        exception.expectMessage("key");
        exception.expectMessage("string");
        throw result.getErrors().get(0);
    }

    @Test
    public void testFlowNavigateIntValue() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_navigate_int_value.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("Step1");
        exception.expectMessage("navigate");
        exception.expectMessage("value");
        exception.expectMessage("string");
        throw result.getErrors().get(0);
    }

    @Test
    public void testFlowWithUnreachableSteps() throws Exception {
        URI resource = getClass().getResource("/corrupted/unreachable_steps.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("Step");
        exception.expectMessage("print_message2");
        exception.expectMessage("unreachable");
        throw result.getErrors().get(0);
    }

    @Test
    public void testFlowWithUnreachableOnFailureStep() throws Exception {
        URI resource = getClass().getResource("/corrupted/unreachable_on_failure_step.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() == 0);
    }

    @Test
    public void testFlowWithUnreachableStepReachableFromOnFailureStep() throws Exception {
        URI resource = getClass().getResource("/corrupted/unreachable_step_reachable_from_on_failure.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() == 0);
    }

    @Test
    public void testFlowWithUnreachableTasksOneReachableFromOnFailureTask() throws Exception {
        URI resource = getClass().getResource("/corrupted/unreachable_tasks_one_reachable_from_on_failure.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertTrue(result.getErrors().size() > 0);
        exception.expect(RuntimeException.class);
        exception.expectMessage("Step");
        exception.expectMessage("print_message2");
        exception.expectMessage("unreachable");
        throw result.getErrors().get(0);
    }
}
