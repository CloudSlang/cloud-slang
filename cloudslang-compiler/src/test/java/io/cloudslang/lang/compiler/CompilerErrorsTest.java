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
import io.cloudslang.lang.compiler.modeller.result.CompilationModellingResult;
import io.cloudslang.lang.compiler.validator.PreCompileValidator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import static io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler.CANNOT_CREATE_PROPERTY_ERROR;
import static io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler.KEY_VALUE_PAIR_MISSING_OR_INDENTATION_PROBLEM_MSG;
import static io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler.MAPPING_VALUES_NOT_ALLOWED_HERE_ERROR;
import static io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler.SCANNING_A_SIMPLE_KEY_ERROR;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * Created by stoneo on 1/22/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CompilerErrorsTest {

    @Autowired
    private SlangCompiler compiler;

    @Test
    public void testEmptyFile() throws Exception {
        URI resource = getClass().getResource("/corrupted/empty_file.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("Source empty_file.sl cannot be empty", exception.getMessage());
    }

    @Test
    public void testNavigateOnSameLevelAsSteps() throws Exception {
        final URI resource = getClass().getResource("/corrupted/flow_navigate_same_level_as_step.sl").toURI();
        final URI operation = getClass().getResource("/test_op.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("Error compiling source 'flow_navigate_same_level_as_step.sl'.\n" +
                        "Flow: 'flow_navigate_same_level_as_step' has steps with keyword " +
                        "on the same indentation as the step name " +
                        "or there is no space between step name and hyphen.",
                exception.getMessage());
    }

    @Test
    public void testNavigateToNonExistingStep() throws Exception {
        final URI resource = getClass().getResource("/corrupted/flow_navigate_to_non_existing_step.sl").toURI();
        final URI operation = getClass().getResource("/test_op.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("Flow flow_navigate_to_non_existing_step has errors:\n" +
                "Failed to compile step: Step1. The step/result name: non_existing_step of navigation: " +
                "SUCCESS -> non_existing_step is missing\n" +
                "The following results are not wired: [SUCCESS].", exception.getMessage());
    }

    @Test
    public void testNotYamlFile() throws Exception {
        final URI resource = getClass().getResource("/corrupted/not_yaml_file.sl").toURI();
        Set<SlangSource> path = new HashSet<>();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("There was a problem parsing the YAML source: not_yaml_file.sl.\n" +
                "Source not_yaml_file.sl does not contain YAML content", exception.getMessage());
    }

    @Test
    public void testNotOpFlowFile() throws Exception {
        final URI resource = getClass().getResource("/corrupted/no_op_flow_file.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("Error transforming source: no_op_flow_file.sl to a Slang model. " +
                "Source no_op_flow_file.sl has no content " +
                "associated with flow/operation/decision/properties property.", exception.getMessage());
    }

    @Test
    public void testSystemProperties() throws Exception {
        final URI systemProperties = getClass().getResource("/corrupted/system_properties.yaml").toURI();
        final Set<SlangSource> path = new HashSet<>();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(systemProperties), path));
        Assert.assertTrue(exception.getMessage().contains("There was a problem parsing the YAML source: system_properties.yaml.\n" +
                "Cannot create property=user.sys.props.host for JavaBean" +
                "=io.cloudslang.lang.compiler.parser.model.ParsedSlang"));

    }

    @Test
    public void testSystemPropertiesAsDep() throws Exception {
        final URI flow = getClass().getResource("/basic_flow.yaml").toURI();
        final URI operation = getClass().getResource("/test_op.sl").toURI();
        final URI systemProperties = getClass().getResource("/corrupted/system_properties.yaml").toURI();
        final Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));
        path.add(SlangSource.fromFile(systemProperties));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(flow), path));
        Assert.assertTrue(exception.getMessage().contains("There was a problem parsing the YAML source: " +
                "system_properties.yaml.\n" +
                "Cannot create property=user.sys.props.host for JavaBean" +
                "=io.cloudslang.lang.compiler.parser.model.ParsedSlang"));
    }

    @Test
    public void testFlowWithNavigationToMissingStep() throws Exception {
        final URI resource = getClass().getResource("/corrupted/flow_with_navigation_to_missing_step.sl").toURI();
        final URI operations = getClass().getResource("/java_op.sl").toURI();

        final Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operations));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("Flow flow_with_navigation_to_missing_step has errors:\n" +
                "Failed to compile step: Step1. The step/result name: Step2 of navigation: " +
                "SUCCESS -> Step2 is missing\n" +
                "The following results are not wired: [SUCCESS].", exception.getMessage());
    }

    @Test
    public void testNavigationSectionKeysNotInResultsSection() throws Exception {
        final URI resource = getClass().getResource("/corrupted/navigation/flow_1.yaml").toURI();
        final URI dep1 = getClass().getResource("/corrupted/navigation/op_1.sl").toURI();

        Set<SlangSource> dependencies = new HashSet<>();
        dependencies.add(SlangSource.fromFile(dep1));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), dependencies));
        Assert.assertEquals("Cannot compile flow 'flow_1' since for step 'step_1' the navigation keys " +
                        "[KEY_1, KEY_2] have no matching results in its dependency 'io.cloudslang.op_1'.",
                exception.getMessage());
    }

    @Test
    public void testNavigationSectionResultsNotWired() throws Exception {
        final URI resource = getClass().getResource("/corrupted/navigation/flow_2.yaml").toURI();
        final URI dep1 = getClass().getResource("/corrupted/navigation/op_2.sl").toURI();

        Set<SlangSource> dependencies = new HashSet<>();
        dependencies.add(SlangSource.fromFile(dep1));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), dependencies));
        Assert.assertEquals("Cannot compile flow 'flow_2' since for step 'step_1' the results [CUSTOM_1]" +
                " of its dependency 'io.cloudslang.op_2' have no matching navigation.", exception.getMessage());

    }

    @Test
    public void testFlowWithMissingSpaceBeforeFirstImport() throws Exception {
        //covers "mapping values are not allowed here" error

        final URI resource = getClass()
                .getResource("/corrupted/flow_with_missing_space_before_first_import.sl").toURI();
        final URI operations = getClass().getResource("/java_op.sl").toURI();
        final URI checkWeather = getClass().getResource("/check_Weather.sl").toURI();
        final URI flows = getClass().getResource("/flow_with_data.yaml").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operations));
        path.add(SlangSource.fromFile(flows));
        path.add(SlangSource.fromFile(checkWeather));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertTrue(exception.getMessage().contains(MAPPING_VALUES_NOT_ALLOWED_HERE_ERROR));
        Assert.assertTrue(exception.getMessage().contains(KEY_VALUE_PAIR_MISSING_OR_INDENTATION_PROBLEM_MSG));
    }

    @Test
    public void testFlowWithWrongIndentation() throws Exception {
        //covers "Unable to find property 'X' on class: io.cloudslang.lang.compiler.parser.model.ParsedSlang"

        final URI resource = getClass().getResource("/corrupted/flow_with_wrong_indentation.sl").toURI();
        final URI operations = getClass().getResource("/java_op.sl").toURI();
        final URI flows = getClass().getResource("/flow_with_data.yaml").toURI();
        final URI checkWeather = getClass().getResource("/check_Weather.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operations));
        path.add(SlangSource.fromFile(flows));
        path.add(SlangSource.fromFile(checkWeather));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertTrue(exception.getMessage().contains(CANNOT_CREATE_PROPERTY_ERROR));
        Assert.assertTrue(exception.getMessage().contains("Unable to find property"));
        Assert.assertTrue(exception.getMessage().contains("is not supported by CloudSlang"));
    }

    @Test
    public void testFlowWhereMapCannotBeCreated() throws Exception {
        //covers "No single argument constructor found for interface java.util.Map"

        final URI resource = getClass().getResource("/corrupted/flow_where_map_cannot_be_created.sl").toURI();
        final URI operations = getClass().getResource("/java_op.sl").toURI();
        final URI flows = getClass().getResource("/flow_with_data.yaml").toURI();
        final URI checkWeather = getClass().getResource("/check_Weather.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operations));
        path.add(SlangSource.fromFile(flows));
        path.add(SlangSource.fromFile(checkWeather));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertTrue(exception.getMessage().contains(CANNOT_CREATE_PROPERTY_ERROR));
        Assert.assertTrue(exception.getMessage().contains(KEY_VALUE_PAIR_MISSING_OR_INDENTATION_PROBLEM_MSG));
    }

    @Test
    public void testFlowWithCorruptedKeyInImports() throws Exception {
        //covers problem parsing YAML source "while scanning a simple key"

        final URI resource = getClass().getResource("/corrupted/flow_with_corrupted_key_in_imports.sl").toURI();
        final URI operations = getClass().getResource("/java_op.sl").toURI();
        final URI flows = getClass().getResource("/flow_with_data.yaml").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operations));
        path.add(SlangSource.fromFile(flows));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertTrue(exception.getMessage().contains(SCANNING_A_SIMPLE_KEY_ERROR));
        Assert.assertTrue(exception.getMessage().contains(KEY_VALUE_PAIR_MISSING_OR_INDENTATION_PROBLEM_MSG));
    }

    @Test
    public void testFlowWithNavigationToMissingDefaultResults() throws Exception {
        final URI resource = getClass()
                .getResource("/corrupted/flow_with_navigation_to_missing_default_results.sl").toURI();
        final URI operations = getClass().getResource("/java_op.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operations));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("Flow flow_with_navigation_to_missing_default_results has errors:\n" +
                "Failed to compile step: Step1. The step/result name: " +
                "SUCCESS of navigation: SUCCESS -> SUCCESS is missing", exception.getMessage());
    }

    @Test
    public void testFlowWithMissingNavigationFromOperationResult() throws Exception {
        final URI resource = getClass()
                .getResource("/corrupted/step_with_missing_navigation_from_operation_result_flow.sl").toURI();
        final URI operations = getClass().getResource("/java_op.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operations));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("Cannot compile flow 'step_with_missing_navigation_from_operation_result_flow' " +
                "since for step 'step1' the results [FAILURE] of its dependency 'user.ops.java_op' " +
                "have no matching navigation.", exception.getMessage());
    }

    @Test
    public void testOpWithMissingNamespace() throws Exception {
        final URI resource = getClass().getResource("/corrupted/op_without_namespace.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("For source[op_without_namespace.sl] namespace cannot be empty.", exception.getMessage());
    }

    @Test
    public void testFlowWithMissingName() throws Exception {
        final URI resource = getClass().getResource("/corrupted/missing_name_flow.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("Executable has no name", exception.getMessage());
    }

    @Test
    public void testFlowWithInputsAsString() throws Exception {
        final URI resource = getClass().getResource("/corrupted/inputs_type_string_flow.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("For flow 'inputs_type_string_flow' syntax is illegal.\n" +
                        "Under property: 'inputs' there should be a list of values, but instead there is a string.",
                exception.getMessage());
    }

    @Test
    public void testFlowWithInputsAsMap() throws Exception {
        final URI resource = getClass().getResource("/corrupted/inputs_type_map_flow.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("For flow 'inputs_type_string_flow' syntax is illegal.\n" +
                        "Under property: 'inputs' there should be a list of values, but instead there is a map.\n" +
                        "By the Yaml spec lists properties are marked with a '- ' (dash followed by a space)",
                exception.getMessage());
    }

    @Test
    public void testFlowWithIllegalTypeInput() throws Exception {
        final URI resource = getClass().getResource("/corrupted/flow_with_wrong_type_input.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("For flow 'flow_with_wrong_type_input' syntax is illegal.\n" +
                "Could not transform Input : 3", exception.getMessage());
    }

    @Test
    public void testFlowWithNoWorkflow() throws Exception {
        final URI resource = getClass().getResource("/corrupted/no_workflow_flow.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("Error compiling no_workflow_flow.sl. Flow: no_workflow has no workflow property",
                exception.getMessage());
    }

    @Test
    public void testFlowWithNoWorkflowData() throws Exception {
        final URI resource = getClass().getResource("/corrupted/no_workflow_data_flow.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("Error compiling no_workflow_data_flow.sl. " +
                "Flow: no_workflow_data has no workflow property", exception.getMessage());
    }

    @Test
    public void testFlowStepWithNoData() throws Exception {
        final URI resource = getClass().getResource("/corrupted/no_step_data_flow.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("Step: step1 has no data", exception.getMessage());
    }

    @Test
    public void testFlowStepWithTwoKeysUnderDo() throws Exception {
        final URI resource = getClass().getResource("/corrupted/multiple_keys_under_do.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("For step 'step1' syntax is illegal.\n" +
                "Step has too many keys under the 'do' keyword,\n" +
                "May happen due to wrong indentation.", exception.getMessage());
    }

    @Test
    public void testFlowWithStepsAsList() throws Exception {
        final URI resource = getClass().getResource("/corrupted/workflow_with_step_map.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("Flow: 'workflow_with_step_map' syntax is illegal.\n" +
                "Below 'workflow' property there should be a list of steps and not a map", exception.getMessage());
    }

    @Test
    public void testFlowWithOnFailureStepsAsList() throws Exception {
        final URI resource = getClass().getResource("/corrupted/on_failure_with_step_map.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("Flow: 'on_failure_with_step_map' syntax is illegal.\n" +
                "Below 'on_failure' property there should be a list of steps and not a map", exception.getMessage());
    }

    @Test
    public void testFlowWithNoRefStep() throws Exception {
        final URI resource = getClass().getResource("/corrupted/step_with_no_ref_flow.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("For step 'step1' syntax is illegal.\n" +
                "Step has no reference information.", exception.getMessage());
    }

    @Test
    public void testStepWithListOfOps() throws Exception {
        final URI resource = getClass().getResource("/corrupted/step_with_list_of_ops.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("For step 'step1' syntax is illegal.\n" +
                        "Under property: 'do' there should be a map of values, but instead there is a list.\n" +
                        "By the Yaml spec maps properties are NOT marked with a '- ' (dash followed by a space)",
                exception.getMessage());
    }

    @Test
    public void testStepWithListOfDos() throws Exception {
        final URI resource = getClass().getResource("/corrupted/step_with_list_of_do_flow.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("Step: step1 syntax is illegal.\n" +
                "Below step name, there should be a map of values in the format:\n" +
                "do:\n" +
                "\top_name:", exception.getMessage());
    }

    @Test
    public void testFlowWithMissingRefInPath() throws Exception {
        final URI resource = getClass().getResource("/basic_flow.yaml").toURI();
        final URI op = getClass().getResource("/operation_with_data.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();

        path.add(SlangSource.fromFile(op));
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("Reference: 'user.ops.test_op' in executable: 'basic_flow', wasn't found in path",
                exception.getMessage());
    }

    @Test
    public void testInputPrivateAndNoDefault() throws Exception {
        final URI resource = getClass().getResource("/private_input_without_default.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("For operation 'private_input_without_default' syntax is illegal.\n" +
                        PreCompileValidator.VALIDATION_ERROR +
                        "Input: 'input_without_default' is private and required but no default value was specified",
                exception.getMessage());
    }

    @Test
    public void testInputWithInvalidKey() throws Exception {
        final URI resource = getClass().getResource("/illegal_key_in_input.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("For operation 'illegal_key_in_input' syntax is illegal.\n" +
                "key: karambula in input: input_with_illegal_key is not a known property", exception.getMessage());
    }

    @Test
    public void testOperationWithNoActionData() throws Exception {
        final URI resource = getClass().getResource("/corrupted/operation_with_no_action_data.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("Error compiling operation_with_no_action_data.sl. " +
                "Operation: operation_with_no_action_data has no action data", exception.getMessage());
    }

    @Test
    public void testOperationWithListOfActions() throws Exception {
        final URI resource = getClass().getResource("/corrupted/operation_with_list_of_actions.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertTrue(exception.getMessage().contains("There was a problem parsing the YAML source: operation_with_list_of_actions.sl.\n" +
                "while parsing a block mapping"));
    }

    @Test
    public void testOperationWithListOfActionTypes() throws Exception {
        final URI resource = getClass().getResource("/corrupted/operation_with_list_of_action_types.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("Action syntax is illegal.\n" +
                        "Under property: 'python_action' there should be a map of values, but instead there is a list.\n" +
                        "By the Yaml spec maps properties are NOT marked with a '- ' (dash followed by a space)",
                exception.getMessage());
    }

    @Test
    public void testParentFlowWithCorruptedSubFlow() throws Exception {
        final URI resource = getClass().getResource("/corrupted/parent_flow_to_no_step_data_flow.sl").toURI();
        final URI subFlow = getClass().getResource("/corrupted/no_step_data_flow.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();

        path.add(SlangSource.fromFile(subFlow));
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("Step: step1 has no data", exception.getMessage());
    }

    @Test
    public void testStepWithNavigateAsString() throws Exception {
        final URI resource = getClass().getResource("/corrupted/step_with_string_navigate_value.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("For step 'step1' syntax is illegal.\n" +
                        "Under property: 'navigate' there should be a list of values, but instead there is a string.",
                exception.getMessage());
    }

    @Test
    public void testStepWithIllegalTypeOfNavigate() throws Exception {
        final URI resource = getClass().getResource("/corrupted/step_with_illegal_navigate_type.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("For step 'step1' syntax is illegal.\n" +
                "Data for property: navigate -> 3 is illegal.\n" +
                " Transformer is: NavigateTransformer", exception.getMessage());
    }

    @Test
    public void testDuplicateStepNamesInFlow() throws Exception {
        final URI resource = getClass().getResource("/corrupted/duplicate_step_name.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("Step name: 'Step1' appears more than once in the workflow. " +
                "Each step name in the workflow must be unique", exception.getMessage());
    }

    @Test
    public void testNullValueInputFlow() throws Exception {
        final URI resource = getClass().getResource("/corrupted/flow_with_null_value_input.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), path));
        Assert.assertEquals("For flow 'flow_with_null_value_input' syntax is illegal.\n" +
                "Could not transform Input : {input1=null} since it has a null value.\n" +
                "\n" +
                "Make sure a value is specified or that indentation is properly done.", exception.getMessage());
    }

    @Test
    public void testValidationOfFlowThatCallsCorruptedFlow() throws Exception {
        final URI flowUri = getClass().getResource("/corrupted/flow_that_calls_corrupted_flow.sl").toURI();

        final URI operation1Uri = getClass().getResource("/test_op.sl").toURI();
        final URI operation2Uri = getClass().getResource("/check_op.sl").toURI();
        final URI operation3Uri = getClass()
                .getResource("/corrupted/flow_input_in_step_same_name_as_dependency_output.sl").toURI();

        final Set<SlangSource> dependencies = new HashSet<>();
        dependencies.add(SlangSource.fromFile(operation1Uri));
        dependencies.add(SlangSource.fromFile(operation2Uri));
        dependencies.add(SlangSource.fromFile(operation3Uri));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                compiler.compile(SlangSource.fromFile(flowUri), dependencies));
        Assert.assertEquals("Cannot compile flow " +
                "'io.cloudslang.flow_input_in_step_same_name_as_dependency_output'. " +
                "Step 'explicit_alias' has input 'balla' with the same name " +
                "as the one of the outputs of 'user.ops.test_op'.", exception.getMessage());
    }

    @Test
    public void testValidationMatchingNavigation() throws Exception {
        final URI resource = getClass().getResource("/corrupted/matching-navigation/parent_flow.sl").toURI();

        final URI subFlow = getClass().getResource("/corrupted/matching-navigation/child_flow.sl").toURI();
        final URI operation1 = getClass().getResource("/corrupted/matching-navigation/test_op.sl").toURI();
        final URI operation2 = getClass().getResource("/corrupted/matching-navigation/check_weather.sl").toURI();
        final URI operation3 = getClass().getResource("/corrupted/matching-navigation/get_time_zone.sl").toURI();
        final URI operation4 = getClass().getResource("/corrupted/matching-navigation/check_number.sl").toURI();

        Set<SlangSource> dependencies = new HashSet<>();
        dependencies.add(SlangSource.fromFile(subFlow));
        dependencies.add(SlangSource.fromFile(operation1));
        dependencies.add(SlangSource.fromFile(operation2));
        dependencies.add(SlangSource.fromFile(operation3));
        dependencies.add(SlangSource.fromFile(operation4));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), dependencies));
        Assert.assertEquals("Cannot compile flow 'child_flow' since for step 'step01' the results [NEGATIVE]" +
                " of its dependency 'user.ops.get_time_zone' have no matching navigation.", exception.getMessage());
    }

    @Test
    public void testValidationDuplicateFqnIgnoreCase() throws Exception {
        final URI resource = getClass().getResource("/corrupted/duplicate/duplicate_fqn_1.sl").toURI();
        final URI dependency1 = getClass().getResource("/noop.sl").toURI();
        final URI dependency2 = getClass().getResource("/corrupted/duplicate/duplicate_fqn_2.sl").toURI();
        final URI dependency3 = getClass().getResource("/basic_flow.yaml").toURI();

        SlangSource duplicateFqnInitialSource = SlangSource.fromFile(dependency2);

        Set<SlangSource> dependencies = new HashSet<>();
        dependencies.add(SlangSource.fromFile(dependency1));
        // change file name from source
        dependencies.add(new SlangSource(duplicateFqnInitialSource.getContent(), "duplicate_fqn_1.sl"));
        dependencies.add(SlangSource.fromFile(dependency3));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                compiler.compile(SlangSource.fromFile(resource), dependencies));
        Assert.assertEquals("Duplicate executable found: 'io.CloudSlang.duplicate_fqn_1'",
                exception.getMessage());
    }

    @Test
    public void testCompileSource() throws URISyntaxException {
        final URI flow = getClass().getResource("/compile_errors/loop_with_break_on_non_existing_result.sl").toURI();
        final URI operation = getClass().getResource("/compile_errors/print.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));
        CompilationModellingResult result = compiler.compileSource(SlangSource.fromFile(flow), path);
        assertEquals(2, result.getErrors().size());
        assertEquals("Argument[print_ values] violates character rules.", result.getErrors().get(0).getMessage());
        assertEquals("Cannot compile flow " +
                        "'loops.loop_with_break_on_non_existing_result' " +
                        "since in step 'print_ values' the results [CUSTOM_1, CUSTOM_2] declared in 'break' " +
                        "section are not declared in the dependency 'loops.print' result section.",
                result.getErrors().get(1).getMessage());
    }

    @Test
    public void testFlowOutputsContainRobotProperty() throws URISyntaxException {
        final URI flow = getClass().getResource("/compile_errors/outputs_robot_prop.sl").toURI();
        final URI operation = getClass().getResource("/compile_errors/print.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));
        CompilationModellingResult result = compiler.compileSource(SlangSource.fromFile(flow), path);
        assertEquals(1, result.getErrors().size());
        assertEquals("'robot' property allowed only for outputs of sequential_action. " +
                "Encountered at output output1", result.getErrors().get(0).getMessage());
    }

    @Test
    public void testFlowStepOutputsContainRobotProperty() throws URISyntaxException {
        final URI flow = getClass().getResource("/compile_errors/step_outputs_robot_prop.sl").toURI();
        final URI operation = getClass().getResource("/compile_errors/print.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));
        CompilationModellingResult result = compiler.compileSource(SlangSource.fromFile(flow), path);
        assertEquals(1, result.getErrors().size());
        assertEquals("For step 'step1' syntax is illegal.\n" +
                "Key: robot in output: output_0 is not a known property", result.getErrors().get(0).getMessage());
    }
}