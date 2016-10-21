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
import io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static junit.framework.Assert.assertEquals;

/**
 * Created by stoneo on 1/22/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CompilerErrorsTest {

    @Autowired
    private SlangCompiler compiler;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testEmptyFile() throws Exception {
        URI resource = getClass().getResource("/corrupted/empty_file.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Source empty_file.sl cannot be empty");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testNavigateOnSameLevelAsSteps() throws Exception {
        final URI resource = getClass().getResource("/corrupted/flow_navigate_same_level_as_step.sl").toURI();
        final URI operation = getClass().getResource("/test_op.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));

        exception.expect(RuntimeException.class);
        exception.expectMessage("Error compiling source 'flow_navigate_same_level_as_step.sl'.\n" +
                "Flow: 'flow_navigate_same_level_as_step' has steps with keyword " +
                "on the same indentation as the step name " +
                "or there is no space between step name and hyphen.");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testNavigateToNonExistingStep() throws Exception {
        final URI resource = getClass().getResource("/corrupted/flow_navigate_to_non_existing_step.sl").toURI();
        final URI operation = getClass().getResource("/test_op.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));

        exception.expect(RuntimeException.class);
        exception.expectMessage("Failed to compile step: Step1. " +
                "The step/result name: non_existing_step of navigation: " +
                "SUCCESS -> non_existing_step is missing");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testNotYamlFile() throws Exception {
        final URI resource = getClass().getResource("/corrupted/not_yaml_file.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("There was a problem parsing the YAML source: not_yaml_file.sl.\n" +
                "Source not_yaml_file.sl does not contain YAML content");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testNotOpFlowFile() throws Exception {
        final URI resource = getClass().getResource("/corrupted/no_op_flow_file.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Error transforming source: no_op_flow_file.sl to a Slang model. " +
                "Source no_op_flow_file.sl has no content " +
                "associated with flow/operation/decision/properties property.");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testSystemProperties() throws Exception {
        final URI systemProperties = getClass().getResource("/corrupted/system_properties.yaml").toURI();
        final Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("There was a problem parsing the YAML source: system_properties.yaml.\n" +
                "Cannot create property=user.sys.props.host for JavaBean" +
                "=io.cloudslang.lang.compiler.parser.model.ParsedSlang");
        compiler.compile(SlangSource.fromFile(systemProperties), path);
    }

    @Test
    public void testSystemPropertiesAsDep() throws Exception {
        final URI flow = getClass().getResource("/basic_flow.yaml").toURI();
        final URI operation = getClass().getResource("/test_op.sl").toURI();
        final URI systemProperties = getClass().getResource("/corrupted/system_properties.yaml").toURI();
        final Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));
        path.add(SlangSource.fromFile(systemProperties));
        exception.expect(RuntimeException.class);
        exception.expectMessage("There was a problem parsing the YAML source: system_properties.yaml.\n" +
                "Cannot create property=user.sys.props.host for JavaBean" +
                "=io.cloudslang.lang.compiler.parser.model.ParsedSlang");
        compiler.compile(SlangSource.fromFile(flow), path);
    }

    @Test
    public void testFlowWithNavigationToMissingStep() throws Exception {
        final URI resource = getClass().getResource("/corrupted/flow_with_navigation_to_missing_step.sl").toURI();
        final URI operations = getClass().getResource("/java_op.sl").toURI();

        final Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operations));
        exception.expect(RuntimeException.class);
        exception.expectMessage("Failed to compile step: Step1. The step/result name: " +
                "Step2 of navigation: SUCCESS -> Step2 is missing");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testNavigationSectionKeysNotInResultsSection() throws Exception {
        final URI resource = getClass().getResource("/corrupted/navigation/flow_1.yaml").toURI();
        final URI dep1 = getClass().getResource("/corrupted/navigation/op_1.sl").toURI();

        Set<SlangSource> dependencies = new HashSet<>();
        dependencies.add(SlangSource.fromFile(dep1));

        exception.expect(RuntimeException.class);
        exception.expectMessage(
                "Cannot compile flow 'flow_1' since for step 'step_1' the navigation keys " +
                        "[KEY_1, KEY_2] have no matching results in its dependency 'io.cloudslang.op_1'."
        );

        compiler.compile(SlangSource.fromFile(resource), dependencies);
    }

    @Test
    public void testNavigationSectionResultsNotWired() throws Exception {
        final URI resource = getClass().getResource("/corrupted/navigation/flow_2.yaml").toURI();
        final URI dep1 = getClass().getResource("/corrupted/navigation/op_2.sl").toURI();

        Set<SlangSource> dependencies = new HashSet<>();
        dependencies.add(SlangSource.fromFile(dep1));

        exception.expect(RuntimeException.class);
        exception.expectMessage(
                "Cannot compile flow 'flow_2' since for step 'step_1' the results [CUSTOM_1]" +
                        " of its dependency 'io.cloudslang.op_2' have no matching navigation."
        );

        compiler.compile(SlangSource.fromFile(resource), dependencies);
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
        exception.expect(RuntimeException.class);
        exception.expectMessage(ParserExceptionHandler.MAPPING_VALUES_NOT_ALLOWED_HERE_ERROR);
        exception.expectMessage(ParserExceptionHandler.KEY_VALUE_PAIR_MISSING_OR_INDENTATION_PROBLEM_MSG);
        compiler.compile(SlangSource.fromFile(resource), path);
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
        exception.expect(RuntimeException.class);
        exception.expectMessage(ParserExceptionHandler.CANNOT_CREATE_PROPERTY_ERROR);
        exception.expectMessage("Unable to find property");
        exception.expectMessage("is not supported by CloudSlang");
        compiler.compile(SlangSource.fromFile(resource), path);
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
        exception.expect(RuntimeException.class);
        exception.expectMessage(ParserExceptionHandler.CANNOT_CREATE_PROPERTY_ERROR);
        exception.expectMessage(ParserExceptionHandler.KEY_VALUE_PAIR_MISSING_OR_INDENTATION_PROBLEM_MSG);
        compiler.compile(SlangSource.fromFile(resource), path);
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
        exception.expect(RuntimeException.class);
        exception.expectMessage(ParserExceptionHandler.SCANNING_A_SIMPLE_KEY_ERROR);
        exception.expectMessage(ParserExceptionHandler.KEY_VALUE_PAIR_MISSING_OR_INDENTATION_PROBLEM_MSG);
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithNavigationToMissingDefaultResults() throws Exception {
        final URI resource = getClass()
                .getResource("/corrupted/flow_with_navigation_to_missing_default_results.sl").toURI();
        final URI operations = getClass().getResource("/java_op.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operations));
        exception.expect(RuntimeException.class);
        exception.expectMessage("Failed to compile step: Step1. " +
                "The step/result name: SUCCESS of navigation: SUCCESS -> SUCCESS is missing");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithMissingNavigationFromOperationResult() throws Exception {
        final URI resource = getClass()
                .getResource("/corrupted/step_with_missing_navigation_from_operation_result_flow.sl").toURI();
        final URI operations = getClass().getResource("/java_op.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operations));
        exception.expect(RuntimeException.class);
        exception.expectMessage("Cannot compile flow 'step_with_missing_navigation_from_operation_result_flow' " +
                "since for step 'step1' the results [FAILURE] of its dependency 'user.ops.java_op' " +
                "have no matching navigation.");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithMissingImports() throws Exception {
        final URI resource = getClass().getResource("/corrupted/missing_dependencies_imports_flow.sl").toURI();

        final Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Source missing_dependencies_imports_flow has " +
                "dependencies but no path was given to the compiler");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testOpWithMissingNamespace() throws Exception {
        final URI resource = getClass().getResource("/corrupted/op_without_namespace.sl").toURI();

        final Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("For source[op_without_namespace.sl] namespace cannot be empty.");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithMissingName() throws Exception {
        final URI resource = getClass().getResource("/corrupted/missing_name_flow.sl").toURI();

        final Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Executable has no name");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithInputsAsString() throws Exception {
        final URI resource = getClass().getResource("/corrupted/inputs_type_string_flow.sl").toURI();

        final Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("For flow 'inputs_type_string_flow' syntax is illegal.\n" +
                "Under property: 'inputs' there should be a list of values, but instead there is a string.");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithInputsAsMap() throws Exception {
        final URI resource = getClass().getResource("/corrupted/inputs_type_map_flow.sl").toURI();

        final Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("For flow 'inputs_type_string_flow' syntax is illegal.\n" +
                "Under property: 'inputs' there should be a list of values, but instead there is a map.\n" +
                "By the Yaml spec lists properties are marked with a '- ' (dash followed by a space)");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithIllegalTypeInput() throws Exception {
        final URI resource = getClass().getResource("/corrupted/flow_with_wrong_type_input.sl").toURI();

        final Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("For flow 'flow_with_wrong_type_input' syntax is illegal.\n" +
                "Could not transform Input : 3");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithNoWorkflow() throws Exception {
        final URI resource = getClass().getResource("/corrupted/no_workflow_flow.sl").toURI();

        final Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Error compiling no_workflow_flow.sl. Flow: no_workflow has no workflow property");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithNoWorkflowData() throws Exception {
        final URI resource = getClass().getResource("/corrupted/no_workflow_data_flow.sl").toURI();

        final Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Error compiling no_workflow_data_flow.sl. " +
                "Flow: no_workflow_data has no workflow property");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowStepWithNoData() throws Exception {
        final URI resource = getClass().getResource("/corrupted/no_step_data_flow.sl").toURI();

        final Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Step: step1 has no data");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowStepWithTwoKeysUnderDo() throws Exception {
        final URI resource = getClass().getResource("/corrupted/multiple_keys_under_do.sl").toURI();

        final Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("For step 'step1' syntax is illegal.\n" +
                "Step has too many keys under the 'do' keyword,\n" +
                "May happen due to wrong indentation");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithStepsAsList() throws Exception {
        final URI resource = getClass().getResource("/corrupted/workflow_with_step_map.sl").toURI();

        final Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Flow: 'workflow_with_step_map' syntax is illegal.\n" +
                "Below 'workflow' property there should be a list of steps and not a map");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithOnFailureStepsAsList() throws Exception {
        final URI resource = getClass().getResource("/corrupted/on_failure_with_step_map.sl").toURI();

        final Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Flow: 'on_failure_with_step_map' syntax is illegal.\n" +
                "Below 'on_failure' property there should be a list of steps and not a map");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithNoRefStep() throws Exception {
        final URI resource = getClass().getResource("/corrupted/step_with_no_ref_flow.sl").toURI();

        final Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Step: 'step1' has no reference information");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testStepWithListOfOps() throws Exception {
        final URI resource = getClass().getResource("/corrupted/step_with_list_of_ops.sl").toURI();

        final Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("For step 'step1' syntax is illegal.\n" +
                "Under property: 'do' there should be a map of values, but instead there is a list.\n" +
                "By the Yaml spec maps properties are NOT marked with a '- ' (dash followed by a space)");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testStepWithListOfDos() throws Exception {
        final URI resource = getClass().getResource("/corrupted/step_with_list_of_do_flow.sl").toURI();

        final Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Step: step1 syntax is illegal.\n" +
                "Below step name, there should be a map of values in the format:\n" +
                "do:\n" +
                "\top_name:");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithMissingRefInPath() throws Exception {
        final URI resource = getClass().getResource("/basic_flow.yaml").toURI();
        final URI op = getClass().getResource("/operation_with_data.sl").toURI();

        final Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(op));
        exception.expect(RuntimeException.class);
        exception.expectMessage("Reference: 'user.ops.test_op' in executable: 'basic_flow', wasn't found in path");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testInputPrivateAndNoDefault() throws Exception {
        final URI resource = getClass().getResource("/private_input_without_default.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("For operation 'private_input_without_default' syntax is illegal.\n" +
                "Input: 'input_without_default' is private and required but no default value was specified");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testInputWithInvalidKey() throws Exception {
        final URI resource = getClass().getResource("/illegal_key_in_input.sl").toURI();
        final Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("For operation 'illegal_key_in_input' syntax is illegal.\n" +
                "key: karambula in input: input_with_illegal_key is not a known property");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testOperationWithNoActionData() throws Exception {
        final URI resource = getClass().getResource("/corrupted/operation_with_no_action_data.sl").toURI();

        final Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Error compiling operation_with_no_action_data.sl. " +
                "Operation: operation_with_no_action_data has no action data");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testOperationWithListOfActions() throws Exception {
        final URI resource = getClass().getResource("/corrupted/operation_with_list_of_actions.sl").toURI();

        final Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("There was a problem parsing the YAML source: operation_with_list_of_actions.sl.\n" +
                "while parsing a block mapping");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testOperationWithListOfActionTypes() throws Exception {
        final URI resource = getClass().getResource("/corrupted/operation_with_list_of_action_types.sl").toURI();

        final Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Action syntax is illegal.\n" +
                "Under property: 'python_action' there should be a map of values, but instead there is a list.\n" +
                "By the Yaml spec maps properties are NOT marked with a '- ' (dash followed by a space)");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testParentFlowWithCorruptedSubFlow() throws Exception {
        final URI resource = getClass().getResource("/corrupted/parent_flow_to_no_step_data_flow.sl").toURI();
        final URI subFlow = getClass().getResource("/corrupted/no_step_data_flow.sl").toURI();

        final Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(subFlow));
        exception.expect(RuntimeException.class);
        exception.expectMessage("Step: step1 has no data");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testStepWithNavigateAsString() throws Exception {
        final URI resource = getClass().getResource("/corrupted/step_with_string_navigate_value.sl").toURI();

        final Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("For step 'step1' syntax is illegal.\n" +
                "Under property: 'navigate' there should be a list of values, but instead there is a string.");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testStepWithIllegalTypeOfNavigate() throws Exception {
        final URI resource = getClass().getResource("/corrupted/step_with_illegal_navigate_type.sl").toURI();

        final Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("For step 'step1' syntax is illegal.\n" +
                "Data for property: navigate -> 3 is illegal.\n" +
                " Transformer is: NavigateTransformer");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testDuplicateStepNamesInFlow() throws Exception {
        final URI resource = getClass().getResource("/corrupted/duplicate_step_name.sl").toURI();

        final Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Step name: 'Step1' appears more than once in the workflow. " +
                "Each step name in the workflow must be unique");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testNullValueInputFlow() throws Exception {
        final URI resource = getClass().getResource("/corrupted/flow_with_null_value_input.sl").toURI();

        final Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("For flow 'flow_with_null_value_input' syntax is illegal.\n" +
                "Could not transform Input : {input1=null} since it has a null value.\n" +
                "\n" +
                "Make sure a value is specified or that indentation is properly done.");
        compiler.compile(SlangSource.fromFile(resource), path);
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

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Cannot compile flow " +
                "'io.cloudslang.flow_input_in_step_same_name_as_dependency_output'. " +
                "Step 'explicit_alias' has input 'balla' with the same name " +
                "as the one of the outputs of 'user.ops.test_op'.");
        compiler.compile(SlangSource.fromFile(flowUri), dependencies);
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

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Cannot compile flow 'child_flow' since for step 'step01' the results [NEGATIVE]" +
                " of its dependency 'user.ops.get_time_zone' have no matching navigation.");
        compiler.compile(SlangSource.fromFile(resource), dependencies);
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

        exception.expect(RuntimeException.class);
        exception.expectMessage("Duplicate executable found: 'io.CloudSlang.duplicate_fqn_1'");

        compiler.compile(SlangSource.fromFile(resource), dependencies);
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

}