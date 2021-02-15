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
import io.cloudslang.lang.compiler.validator.PreCompileValidator;
import io.cloudslang.lang.compiler.validator.PreCompileValidatorImpl;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Created by Ifat Gavish on 29/02/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PreCompilerErrorsTest {

    @Autowired
    private SlangCompiler compiler;

    @Test
    public void testNotOpFlowFile() throws Exception {
        URI resource = getClass().getResource("/corrupted/no_op_flow_file.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("Error transforming source: no_op_flow_file.sl to a Slang model. " +
                "Source no_op_flow_file.sl has no content associated with " +
                "flow/operation/decision/properties property.", exception.getMessage());
    }

    @Test
    public void testOpWithMissingNamespace() throws Exception {
        URI resource = getClass().getResource("/corrupted/op_without_namespace.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("For source[op_without_namespace.sl] namespace cannot be empty.", exception.getMessage());
    }

    @Test
    public void testOpWithActionAndWorkflow() throws Exception {
        URI resource = getClass().getResource("/corrupted/op_with_action_and_workflow.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("Conflicting keys[workflow, python_action] at: op_with_action_and_workflow", exception.getMessage());
    }

    @Test
    public void testFlowWithMissingName() throws Exception {
        URI resource = getClass().getResource("/corrupted/missing_name_flow.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("Executable has no name", exception.getMessage());
    }

    @Test
    public void testOperationWithNullFileName() throws Exception {
        URI resource = getClass().getResource("/corrupted/wrong_name_operation.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(new SlangSource(SlangSource.fromFile(resource).getContent(), null)));
        assertEquals("should be declared in a file named \"test_op\" " +
                "plus a valid extension(sl, sl.yaml, sl.yml, prop.sl, yaml, yml)", exception.getMessage());
    }

    @Test
    public void testOperationWithWrongName() throws Exception {
        URI resource = getClass().getResource("/corrupted/wrong_name_operation.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("should be declared in a file named \"test_op.sl\"", exception.getMessage());
    }

    @Test
    public void testOperationWithWrongNameSlYamlExtension() throws Exception {
        URI resource = getClass().getResource("/corrupted/wrong_name_operation.sl.yaml").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("should be declared in a file named \"test_op.sl.yaml\"", exception.getMessage());
    }

    @Test
    public void testOperationWithWrongNameSlYmlExtension() throws Exception {
        URI resource = getClass().getResource("/corrupted/wrong_name_operation.sl.yml").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("should be declared in a file named \"test_op.sl.yml\"", exception.getMessage());
    }

    @Test
    public void testOperationWithWrongNameYamlExtension() throws Exception {
        URI resource = getClass().getResource("/corrupted/wrong_name_operation.yaml").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("should be declared in a file named \"test_op.yaml\"", exception.getMessage());
    }

    @Test
    public void testOperationWithWrongNameYmlExtension() throws Exception {
        URI resource = getClass().getResource("/corrupted/wrong_name_operation.yml").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("should be declared in a file named \"test_op.yml\"", exception.getMessage());
    }

    @Test
    public void testOperationWithWrongNameWrongExtension() throws Exception {
        URI resource = getClass().getResource("/corrupted/wrong_name_operation.wrong").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("it should be declared in a file named \"test_op\" " +
                "plus a valid extension(sl, sl.yaml, sl.yml, prop.sl, yaml, yml)", exception.getMessage());
    }

    @Test
    public void testFlowSameInputAndOutputName() throws Exception {
        URI resource = getClass().getResource("/corrupted/same_input_and_output_name.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("Inputs and outputs names should be different for " +
                "\"io.cloudslang.base.json.same_input_and_output_name\". " +
                "Please rename input/output \"json_path\"", exception.getMessage());
    }

    @Test
    public void testFlowSamePrivateInputAndOutputName() throws Exception {
        URI resource = getClass().getResource("/corrupted/same_private_input_and_output_name.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        assertEquals(0, result.getErrors().size());
    }

    @Test
    public void testFlowWithInputsAsString() throws Exception {
        URI resource = getClass().getResource("/corrupted/inputs_type_string_flow.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("For flow 'inputs_type_string_flow' syntax is illegal.\n" +
                "Under property: 'inputs' there should be a list of values, " +
                "but instead there is a string.", exception.getMessage());
    }

    @Test
    public void testFlowWithInputsAsMap() throws Exception {
        URI resource = getClass().getResource("/corrupted/inputs_type_map_flow.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("For flow 'inputs_type_string_flow' syntax is illegal.\n" +
                        "Under property: 'inputs' there should be a list of values, but instead there is a map.\n" +
                        "By the Yaml spec lists properties are marked with a '- ' (dash followed by a space)",
                exception.getMessage());
    }

    @Test
    public void testFlowWithIllegalTypeInput() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_with_wrong_type_input.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("For flow 'flow_with_wrong_type_input' syntax is illegal.\n" +
                "Could not transform Input : 3", exception.getMessage());
    }

    @Test
    public void testFlowWithNoWorkflow() throws Exception {
        URI resource = getClass().getResource("/corrupted/no_workflow_flow.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("Error compiling no_workflow_flow.sl. Flow: no_workflow has no workflow property",
                exception.getMessage());
    }

    @Test
    public void testFlowWithNoWorkflowData() throws Exception {
        URI resource = getClass().getResource("/corrupted/no_workflow_data_flow.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("Error compiling no_workflow_data_flow.sl. Flow: no_workflow_data has " +
                "no workflow property", exception.getMessage());
    }

    @Test
    public void testFlowStepWithNoData() throws Exception {
        URI resource = getClass().getResource("/corrupted/no_step_data_flow.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("Step: step1 has no data", exception.getMessage());
    }

    @Test
    public void testFlowStepWithTwoKeysUnderDo() throws Exception {
        URI resource = getClass().getResource("/corrupted/multiple_keys_under_do.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("For step 'step1' syntax is illegal.\n" +
                "Step has too many keys under the 'do' keyword,\n" +
                "May happen due to wrong indentation", exception.getMessage());
    }

    @Test
    public void testFlowWithStepsAsList() throws Exception {
        URI resource = getClass().getResource("/corrupted/workflow_with_step_map.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("Flow: 'workflow_with_step_map' syntax is illegal.\n" +
                "Below 'workflow' property there should be a list of steps and not a map", exception.getMessage());
    }

    @Test
    public void testFlowWithOnFailureStepsAsList() throws Exception {
        URI resource = getClass().getResource("/corrupted/on_failure_with_step_map.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("Flow: 'on_failure_with_step_map' syntax is illegal.\n" +
                "Below 'on_failure' property there should be a list of steps and not a map", exception.getMessage());
    }


    @Test
    public void testOpResultNamedOnFailure() throws Exception {
        URI resource = getClass().getResource("/corrupted/on_failure/op_1.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("For operation 'op_1' syntax is illegal.\n" +
                "Result cannot be called 'on_failure'.", exception.getMessage());
    }

    @Test
    public void testFlowResultNamedOnFailure() throws Exception {
        URI resource = getClass().getResource("/corrupted/on_failure/flow_1.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("For flow 'flow_1' syntax is illegal.\n" +
                "Result cannot be called 'on_failure'.", exception.getMessage());
    }

    @Test
    public void testDecisionResultNamedOnFailure() throws Exception {
        URI resource = getClass().getResource("/corrupted/on_failure/decision_1.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("For decision 'decision_1' syntax is illegal.\n" +
                "Result cannot be called 'on_failure'.", exception.getMessage());
    }

    @Test
    public void testFlowWithNoRefStep() throws Exception {
        URI resource = getClass().getResource("/corrupted/step_with_no_ref_flow.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("For step 'step1' syntax is illegal.\n" +
                "Step has no reference information.", exception.getMessage());
    }

    @Test
    public void testStepWithListOfOps() throws Exception {
        URI resource = getClass().getResource("/corrupted/step_with_list_of_ops.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("For step 'step1' syntax is illegal.\n" +
                "Under property: 'do' there should be a map of values, but instead there is a list.\n" +
                "By the Yaml spec maps properties are NOT marked with a '- ' (dash followed by a space)", exception.getMessage());
    }

    @Test
    public void testStepWithListOfDos() throws Exception {
        URI resource = getClass().getResource("/corrupted/step_with_list_of_do_flow.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("Step: step1 syntax is illegal.\n" +
                "Below step name, there should be a map of values in the format:\n" +
                "do:\n" +
                "\top_name:", exception.getMessage());
    }

    @Test
    public void testInputPrivateAndNoDefault() throws Exception {
        URI resource = getClass().getResource("/private_input_without_default.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("For operation 'private_input_without_default' syntax is illegal.\n" +
                        PreCompileValidator.VALIDATION_ERROR +
                        "Input: 'input_without_default' is private and required but no default value was specified",
                exception.getMessage());
    }

    @Test
    public void testWrongTag() throws Exception {
        URI resource = getClass().getResource("/corrupted/private_input_without_default_wrong_tag.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("Artifact {private_input_without_default} has unrecognized tag {action}. " +
                "Please take a look at the supported features per versions link", exception.getMessage());

    }

    @Test
    public void testInputWithInvalidKey() throws Exception {
        URI resource = getClass().getResource("/illegal_key_in_input.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("For operation 'illegal_key_in_input' syntax is illegal.\n" +
                "key: karambula in input: input_with_illegal_key is not a known property", exception.getMessage());
    }

    @Test
    public void testOperationWithNoActionData() throws Exception {
        URI resource = getClass().getResource("/corrupted/operation_with_no_action_data.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("Error compiling operation_with_no_action_data.sl. " +
                "Operation: operation_with_no_action_data has no action data", exception.getMessage());
    }

    @Test
    public void testOperationWithListOfActionTypes() throws Exception {
        URI resource = getClass().getResource("/corrupted/operation_with_list_of_action_types.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("Action syntax is illegal.\n" +
                        "Under property: 'python_action' there should be a map of values, but instead there is a list.\n" +
                        "By the Yaml spec maps properties are NOT marked with a '- ' (dash followed by a space)",
                exception.getMessage());
    }

    @Test
    public void testParentFlowWithCorruptedSubFlow() throws Exception {
        URI subFlow = getClass().getResource("/corrupted/no_step_data_flow.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(subFlow)));
        assertEquals("Step: step1 has no data", exception.getMessage());
    }

    @Test
    public void testStepWithNavigateAsString() throws Exception {
        URI resource = getClass().getResource("/corrupted/step_with_string_navigate_value.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("For step 'step1' syntax is illegal.\n" +
                        "Under property: 'navigate' there should be a list of values, but instead there is a string.",
                exception.getMessage());
    }

    @Test
    public void testStepWithIllegalTypeOfNavigate() throws Exception {
        URI resource = getClass().getResource("/corrupted/step_with_illegal_navigate_type.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("For step 'step1' syntax is illegal.\n" +
                "Data for property: navigate -> 3 is illegal.\n" +
                " Transformer is: NavigateTransformer", exception.getMessage());
    }

    @Test
    public void testDuplicateStepNamesInFlow() throws Exception {
        URI resource = getClass().getResource("/corrupted/duplicate_step_name.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("Step name: 'Step1' appears more than once in the workflow. " +
                "Each step name in the workflow must be unique", exception.getMessage());
    }

    @Test
    public void testNullValueInputFlow() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_with_null_value_input.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("For flow 'flow_with_null_value_input' syntax is illegal.\n" +
                "Could not transform Input : {input1=null} since it has a null value.\n" +
                "\n" +
                "Make sure a value is specified or that indentation is properly done.", exception.getMessage());
    }

    @Test
    public void testFlowWithResultExpressions() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_with_result_expressions.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));

        List<RuntimeException> errors = result.getErrors();
        assertEquals(2, errors.size());

        validateExceptionMessage(
                errors.get(0),
                "Flow: 'flow_with_result_expressions' syntax is illegal. Error compiling result: 'SUCCESS'. " +
                        "Explicit values are not allowed for flow results. Correct format is: '- SUCCESS'.",
                "SUCCESS",
                PreCompileValidatorImpl.FLOW_RESULTS_WITH_EXPRESSIONS_MESSAGE
        );
        validateExceptionMessage(
                errors.get(1),
                "Flow: 'flow_with_result_expressions' syntax is illegal. Error compiling result: 'CUSTOM'. " +
                        "Explicit values are not allowed for flow results. Correct format is: '- CUSTOM'.",
                "CUSTOM",
                PreCompileValidatorImpl.FLOW_RESULTS_WITH_EXPRESSIONS_MESSAGE
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

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("For step 'Step1' syntax is illegal.\n" +
                "Under property: 'navigate' there should be a list of values, but instead there is a map.\n" +
                "By the Yaml spec lists properties are marked with a '- ' (dash followed by a space)", exception.getMessage());
    }

    @Test
    public void testFlowOnFailureSkipped() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_on_failure_skipped.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("Error compiling source 'flow_on_failure_skipped.sl'.\n" +
                "Flow: 'flow_on_failure_skipped' has steps with keyword on the same indentation as the step name or " +
                "there is no space between step name and hyphen.", exception.getMessage());
    }

    @Test
    public void testFlowOnFailureMissingStep() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_on_failure_missing_step.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("Flow: 'flow_on_failure_missing_step' syntax is illegal.\n" +
                "There is no step below the 'on_failure' property.", exception.getMessage());
    }

    @Test
    public void testFlowNavigateMultipleElementsForRule() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_navigate_multiple_elements_for_rule.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("For step 'Step1' syntax is illegal.\nEach list item in the navigate" +
                " section should contain exactly one key:value pair.", exception.getMessage());
    }

    @Test
    public void testFlowNavigateIntKey() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_navigate_int_key.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("For step 'Step1' syntax is illegal.\n" +
                "Each key in the navigate section should be a string.", exception.getMessage());
    }

    @Test
    public void testFlowNavigateIntValue() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_navigate_int_value.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("For step 'Step1' syntax is illegal.\n" +
                "Each value in the navigate section should be a string.", exception.getMessage());
    }

    @Test
    public void testFlowWithUnreachableSteps() throws Exception {
        URI resource = getClass().getResource("/corrupted/unreachable_steps.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("Step 'print_message2' is unreachable.", exception.getMessage());
    }

    @Test
    public void testFlowWithUnreachableFlowResult() throws Exception {
        URI resource = getClass().getResource("/corrupted/unreachable_flow_results_explicit_nav.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("The following results are not wired: [UNREACHABLE_RESULT].", exception.getMessage());
    }

    @Test
    public void testFlowWithUnreachableFlowResultMissingNavigation() throws Exception {
        URI resource = getClass().getResource("/corrupted/unreachable_flow_results_missing_nav.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));

        List<RuntimeException> errors = result.getErrors();
        assertTrue(errors.size() == 1);

        assertEquals("Flow unreachable_flow_results_missing_nav has errors:\n" +
                        "Failed to compile step: print_message1. " +
                        "The step/result name: CUSTOM of navigation: FAILURE -> CUSTOM is missing\n" +
                        "The following results are not wired: [UNREACHABLE_RESULT].",
                errors.get(0).getMessage());
    }

    @Test
    public void testFlowWithUnreachableOnFailureStep() throws Exception {
        URI resource = getClass().getResource("/corrupted/unreachable_on_failure_step.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("on_failure step 'print_on_failure_1' is unreachable.", exception.getMessage());
    }

    @Test
    public void testFlowBothResultAndStepNameAfter() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_name_step_result_after.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        List<RuntimeException> errors = result.getErrors();

        assertTrue(errors.size() == 1);
        assertContains(
                errors,
                0,
                "Navigation target: 'COLLISION_ITEM' is declared both as step name and flow result."
        );
    }

    @Test
    public void testFlowBothResultAndStepNameBefore() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_name_step_result_before.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        List<RuntimeException> errors = result.getErrors();

        assertTrue(errors.size() == 1);
        assertContains(
                errors,
                0,
                "Navigation target: 'COLLISION_ITEM' is declared both as step name and flow result."
        );
    }

    @Test
    public void testFlowBothResultAndStepNameCurrent() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_name_step_result_current.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        List<RuntimeException> errors = result.getErrors();

        assertTrue(errors.size() == 1);
        assertContains(
                errors,
                0,
                "Navigation target: 'COLLISION_ITEM' is declared both as step name and flow result."
        );
    }

    @Test
    public void testFlowBothResultAndStepNameAfterCurrent() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_name_step_result_after_current.sl").toURI();

        ExecutableModellingResult result = compiler.preCompileSource(SlangSource.fromFile(resource));
        List<RuntimeException> errors = result.getErrors();

        assertTrue(errors.size() == 1);
        assertContains(
                errors,
                0,
                "Navigation target: 'COLLISION_ITEM' is declared both as step name and flow result."
        );
    }

    @Test
    public void testFlowWithUnreachableStepReachableFromOnFailureStep() throws Exception {
        URI resource = getClass().getResource("/corrupted/on_failure_contains_navigate_section.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("Flow: 'on_failure_contains_navigate_section' syntax is illegal.\n" +
                        "The step below 'on_failure' property should not contain a \"navigate\" section.",
                exception.getMessage());
    }

    @Test
    public void testNavigateToNonExistingStep() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_navigate_to_non_existing_step.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("Failed to compile step: Step1. " +
                "The step/result name: non_existing_step of navigation: " +
                "SUCCESS -> non_existing_step is missing", exception.getMessage());
    }

    @Test
    public void testNavigationRuleIncorrectType() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_navigation_rule_incorrect_type.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("For step 'Step1' syntax is illegal.\n" +
                        "Navigation rule should be a Map. Actual type is java.util.ArrayList: [SUCCESS, SUCCESS]",
                exception.getMessage());
    }

    @Test
    public void testFlowWithInvalidInputs() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_with_invalid_inputs.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("For flow 'flow_with_invalid_inputs' syntax is illegal.\n" +
                        "Invalid syntax after input \"input1\". Please check all inputs are provided as a list " +
                        "and each input is preceded by a hyphen. Input \"input2\" is missing the hyphen.",
                exception.getMessage());
    }

    @Test
    public void testFlowWithUnreachableTasksOneReachableFromUnreachableTask() throws Exception {
        URI resource = getClass()
                .getResource("/corrupted/unreachable_tasks_one_reachable_from_unreachable_task.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("Step 'print_message2' is unreachable.", exception.getMessage());
    }

    @Test
    public void testNullPublishValue() throws Exception {
        URI resource = getClass().getResource("/corrupted/null_publish_value.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("For step 'CheckWeather' syntax is illegal.\n" +
                        "Could not transform Output : {var_with_null_value=null} since it has a null value.",
                exception.getMessage());
    }

    @Test
    public void testDefaultNavigationMissingResult() throws Exception {
        URI resource = getClass().getResource("/corrupted/default_navigation_missing_result.sl").toURI();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.preCompileSource(SlangSource.fromFile(resource)));
        assertEquals("Failed to compile step: jedi_training_1. " +
                "The step/result name: FAILURE of navigation: FAILURE -> FAILURE is missing", exception.getMessage());
    }

    private void assertContains(List<RuntimeException> errors, int index, String message) {
        @SuppressWarnings("all")
        RuntimeException rex1 = errors.get(index);
        Assert.assertTrue(rex1.getMessage().contains(message));
    }

}