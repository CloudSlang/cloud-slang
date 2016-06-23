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
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by stoneo on 1/22/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
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
        exception.expectMessage("Source empty_file cannot be empty");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testNavigateOnSameLevelAsSteps() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_navigate_same_level_as_step.sl").toURI();
        URI operation = getClass().getResource("/test_op.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));

        exception.expect(RuntimeException.class);
        exception.expectMessage("Error compiling source 'flow_navigate_same_level_as_step'. Flow: " +
                "'flow_navigate_same_level_as_step' has steps with keyword on the same indentation as the step name.");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
     public void testNavigateToNonExistingStep() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_navigate_to_non_existing_step.sl").toURI();
        URI operation = getClass().getResource("/test_op.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));

        exception.expect(RuntimeException.class);
        exception.expectMessage("Failed to compile step: Step1. The step/result name: non_existing_step of navigation: " +
                "SUCCESS -> non_existing_step is missing");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testNotYamlFile() throws Exception {
        URI resource = getClass().getResource("/corrupted/not_yaml_file.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("There was a problem parsing the YAML source: not_yaml_file.\n" +
                "Source not_yaml_file does not contain YAML content");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testNotOpFlowFile() throws Exception {
        URI resource = getClass().getResource("/corrupted/no_op_flow_file.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Error transforming source: no_op_flow_file to a Slang model. " +
                "Source no_op_flow_file has no content associated with flow/operation/properties property.");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

	@Test
	public void testSystemProperties() throws Exception {
		URI systemProperties = getClass().getResource("/corrupted/system_properties.yaml").toURI();
		Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
		exception.expectMessage("There was a problem parsing the YAML source: system_properties.\n" +
                "Cannot create property=user.sys.props.host for JavaBean=io.cloudslang.lang.compiler.parser.model.ParsedSlang");
		compiler.compile(SlangSource.fromFile(systemProperties), path);
	}

    @Test
    public void testSystemPropertiesAsDep() throws Exception {
        URI flow = getClass().getResource("/basic_flow.yaml").toURI();
        URI operation = getClass().getResource("/test_op.sl").toURI();
		URI systemProperties = getClass().getResource("/corrupted/system_properties.yaml").toURI();
        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operation));
        path.add(SlangSource.fromFile(systemProperties));
        exception.expect(RuntimeException.class);
		exception.expectMessage("There was a problem parsing the YAML source: system_properties.\n" +
                "Cannot create property=user.sys.props.host for JavaBean=io.cloudslang.lang.compiler.parser.model.ParsedSlang");
        compiler.compile(SlangSource.fromFile(flow), path);
    }

    @Test
    public void testFlowWithNavigationToMissingStep() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_with_navigation_to_missing_step.sl").toURI();
        URI operations = getClass().getResource("/java_op.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operations));
        exception.expect(RuntimeException.class);
        exception.expectMessage("Failed to compile step: Step1. The step/result name: " +
                "Step2 of navigation: SUCCESS -> Step2 is missing");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithMissingSpaceBeforeFirstImport() throws Exception {
        //covers "mapping values are not allowed here" error

        URI resource = getClass().getResource("/corrupted/flow_with_missing_space_before_first_import.sl").toURI();
        URI operations = getClass().getResource("/java_op.sl").toURI();
        URI checkWeather = getClass().getResource("/check_Weather.sl").toURI();
        URI flows = getClass().getResource("/flow_with_data.yaml").toURI();

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

        URI resource = getClass().getResource("/corrupted/flow_with_wrong_indentation.sl").toURI();
        URI operations = getClass().getResource("/java_op.sl").toURI();
        URI flows = getClass().getResource("/flow_with_data.yaml").toURI();
        URI checkWeather = getClass().getResource("/check_Weather.sl").toURI();

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

        URI resource = getClass().getResource("/corrupted/flow_where_map_cannot_be_created.sl").toURI();
        URI operations = getClass().getResource("/java_op.sl").toURI();
        URI flows = getClass().getResource("/flow_with_data.yaml").toURI();
        URI checkWeather = getClass().getResource("/check_Weather.sl").toURI();

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

        URI resource = getClass().getResource("/corrupted/flow_with_corrupted_key_in_imports.sl").toURI();
        URI operations = getClass().getResource("/java_op.sl").toURI();
        URI flows = getClass().getResource("/flow_with_data.yaml").toURI();

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
        URI resource = getClass().getResource("/corrupted/flow_with_navigation_to_missing_default_results.sl").toURI();
        URI operations = getClass().getResource("/java_op.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operations));
        exception.expect(RuntimeException.class);
        exception.expectMessage("Failed to compile step: Step1. " +
                "The step/result name: SUCCESS of navigation: SUCCESS -> SUCCESS is missing");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithMissingNavigationFromOperationResult() throws Exception {
        URI resource = getClass().getResource("/corrupted/step_with_missing_navigation_from_operation_result_flow.sl").toURI();
        URI operations = getClass().getResource("/java_op.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operations));
        exception.expect(RuntimeException.class);
        exception.expectMessage("Cannot compile flow: 'step_with_missing_navigation_from_operation_result_flow' " +
                "since for step: 'step1', the result 'FAILURE' of its dependency: 'user.ops.java_op' has no matching navigation");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithMissingImports() throws Exception {
        URI resource = getClass().getResource("/corrupted/missing_dependencies_imports_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Source missing_dependencies_imports_flow has " +
                "dependencies but no path was given to the compiler");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testOpWithMissingNamespace() throws Exception {
        URI resource = getClass().getResource("/corrupted/op_without_namespace.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Operation/Flow op_without_namespace must have a namespace");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithMissingName() throws Exception {
        URI resource = getClass().getResource("/corrupted/missing_name_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Executable in source: missing_name_flow has no name");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithInputsAsString() throws Exception {
        URI resource = getClass().getResource("/corrupted/inputs_type_string_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("For flow 'inputs_type_string_flow' syntax is illegal.\n" +
                "Under property: 'inputs' there should be a list of values, but instead there is a string.");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithInputsAsMap() throws Exception {
        URI resource = getClass().getResource("/corrupted/inputs_type_map_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("For flow 'inputs_type_string_flow' syntax is illegal.\n" +
                "Under property: 'inputs' there should be a list of values, but instead there is a map.\n" +
                "By the Yaml spec lists properties are marked with a '- ' (dash followed by a space)");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithIllegalTypeInput() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_with_wrong_type_input.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("For flow 'flow_with_wrong_type_input' syntax is illegal.\n" +
                "Could not transform Input : 3");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithNoWorkflow() throws Exception {
        URI resource = getClass().getResource("/corrupted/no_workflow_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Error compiling no_workflow_flow. Flow: no_workflow has no workflow property");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithNoWorkflowData() throws Exception {
        URI resource = getClass().getResource("/corrupted/no_workflow_data_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Error compiling no_workflow_data_flow. Flow: no_workflow_data has no workflow property");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowStepWithNoData() throws Exception {
        URI resource = getClass().getResource("/corrupted/no_step_data_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Step: step1 has no data");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowStepWithTwoKeysUnderDo() throws Exception {
        URI resource = getClass().getResource("/corrupted/multiple_keys_under_do.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("For step 'step1' syntax is illegal.\n" +
                "Step has too many keys under the 'do' keyword,\n" +
                "May happen due to wrong indentation");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithStepsAsList() throws Exception {
        URI resource = getClass().getResource("/corrupted/workflow_with_step_map.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Flow: 'workflow_with_step_map' syntax is illegal.\n" +
                "Below 'workflow' property there should be a list of steps and not a map");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithOnFailureStepsAsList() throws Exception {
        URI resource = getClass().getResource("/corrupted/on_failure_with_step_map.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Flow: 'on_failure_with_step_map' syntax is illegal.\n" +
                "Below 'on_failure' property there should be a list of steps and not a map");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithNoRefStep() throws Exception {
        URI resource = getClass().getResource("/corrupted/step_with_no_ref_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Step: 'step1' has no reference information");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testStepWithListOfOps() throws Exception {
        URI resource = getClass().getResource("/corrupted/step_with_list_of_ops.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("For step 'step1' syntax is illegal.\n" +
                "Under property: 'do' there should be a map of values, but instead there is a list.\n" +
                "By the Yaml spec maps properties are NOT marked with a '- ' (dash followed by a space)");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testStepWithListOfDos() throws Exception {
        URI resource = getClass().getResource("/corrupted/step_with_list_of_do_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Step: step1 syntax is illegal.\n" +
                "Below step name, there should be a map of values in the format:\n" +
                "do:\n" +
                "\top_name:");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithMissingRefInPath() throws Exception {
        URI resource = getClass().getResource("/basic_flow.yaml").toURI();
        URI op = getClass().getResource("/operation_with_data.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(op));
        exception.expect(RuntimeException.class);
        exception.expectMessage("Reference: 'user.ops.test_op' in executable: 'basic_flow', wasn't found in path");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testInputPrivateAndNoDefault() throws Exception {
        URI resource = getClass().getResource("/private_input_without_default.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("For operation 'private_input_without_default' syntax is illegal.\n" +
                "input: input_without_default is private but no default value was specified");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testInputWithInvalidKey() throws Exception {
        URI resource = getClass().getResource("/illegal_key_in_input.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("For operation 'illegal_key_in_input' syntax is illegal.\n" +
                "key: karambula in input: input_with_illegal_key is not a known property");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testOperationWithNoActionData() throws Exception {
        URI resource = getClass().getResource("/corrupted/operation_with_no_action_data.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Error compiling operation_with_no_action_data. " +
                "Operation: operation_with_no_action_data has no action data");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testOperationWithListOfActions() throws Exception {
        URI resource = getClass().getResource("/corrupted/operation_with_list_of_actions.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("There was a problem parsing the YAML source: operation_with_list_of_actions.\n" +
                "while parsing a block mapping");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testOperationWithListOfActionTypes() throws Exception {
        URI resource = getClass().getResource("/corrupted/operation_with_list_of_action_types.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Action syntax is illegal.\n" +
                "Under property: 'python_action' there should be a map of values, but instead there is a list.\n" +
                "By the Yaml spec maps properties are NOT marked with a '- ' (dash followed by a space)");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testParentFlowWithCorruptedSubFlow() throws Exception {
        URI resource = getClass().getResource("/corrupted/parent_flow_to_no_step_data_flow.sl").toURI();
        URI subFlow = getClass().getResource("/corrupted/no_step_data_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(subFlow));
        exception.expect(RuntimeException.class);
        exception.expectMessage("Step: step1 has no data");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testStepWithNavigateAsString() throws Exception {
        URI resource = getClass().getResource("/corrupted/step_with_string_navigate_value.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("For step 'step1' syntax is illegal.\n" +
                "Under property: 'navigate' there should be a list of values, but instead there is a string.");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testStepWithIllegalTypeOfNavigate() throws Exception {
        URI resource = getClass().getResource("/corrupted/step_with_illegal_navigate_type.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("For step 'step1' syntax is illegal.\n" +
                "Data for property: navigate -> 3 is illegal.\n" +
                " Transformer is: NavigateTransformer");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testDuplicateStepNamesInFlow() throws Exception {
        URI resource = getClass().getResource("/corrupted/duplicate_step_name.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Step name: 'Step1' appears more than once in the workflow. " +
                "Each step name in the workflow must be unique");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testNullValueInputFlow() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_with_null_value_input.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("For flow 'flow_with_null_value_input' syntax is illegal.\n" +
                "Could not transform Input : {input1=null} since it has a null value.\n" +
                "\n" +
                "Make sure a value is specified or that indentation is properly done.");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testValidationOfFlowWithMissingNavigationFromOperationResult()throws Exception {
        URI flowUri = getClass().getResource("/corrupted/step_with_missing_navigation_from_operation_result_flow.sl").toURI();
        Executable flowModel = compiler.preCompile(SlangSource.fromFile(flowUri));

        URI operationUri = getClass().getResource("/java_op.sl").toURI();
        Executable operationModel = compiler.preCompile(SlangSource.fromFile(operationUri));
        Set<Executable> dependencies = new HashSet<>();
        dependencies.add(operationModel);

        exception.expect(RuntimeException.class);
        exception.expectMessage("Cannot compile flow: 'step_with_missing_navigation_from_operation_result_flow' " +
                "since for step: 'step1', the result 'FAILURE' of its dependency: 'user.ops.java_op' " +
                "has no matching navigation");
        List<RuntimeException> errors = compiler.validateSlangModelWithDirectDependencies(flowModel, dependencies);
        Assert.assertEquals(1, errors.size());
        throw errors.get(0);
    }

    @Test
    public void testValidationOfFlowWithMissingDependencyRequiredInputInStep()throws Exception {
        URI flowUri = getClass().getResource("/corrupted/flow_missing_dependency_required_input_in_step.sl").toURI();
        Executable flowModel = compiler.preCompile(SlangSource.fromFile(flowUri));

        URI operation1Uri = getClass().getResource("/test_op.sl").toURI();
        Executable operation1Model = compiler.preCompile(SlangSource.fromFile(operation1Uri));
        URI operation2Uri = getClass().getResource("/check_op.sl").toURI();
        Executable operation2Model = compiler.preCompile(SlangSource.fromFile(operation2Uri));
        Set<Executable> dependencies = new HashSet<>();
        dependencies.add(operation1Model);
        dependencies.add(operation2Model);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Cannot compile flow 'io.cloudslang.flow_missing_dependency_required_input_in_step'. " +
                "Step 'explicit_alias' does not declare all the mandatory inputs of its reference. " +
                "The following inputs of 'user.ops.test_op' are not private, required and with no default value: alla.");
        List<RuntimeException> errors = compiler.validateSlangModelWithDirectDependencies(flowModel, dependencies);
        Assert.assertEquals(1, errors.size());
        throw errors.get(0);
    }

    @Test
    public void testValidationOfFlowInputInStepWithSameNameAsDependencyOutput()throws Exception {
        URI flowUri = getClass().getResource("/corrupted/flow_input_in_step_same_name_as_dependency_output.sl").toURI();
        Executable flowModel = compiler.preCompile(SlangSource.fromFile(flowUri));

        URI operation1Uri = getClass().getResource("/test_op.sl").toURI();
        Executable operation1Model = compiler.preCompile(SlangSource.fromFile(operation1Uri));
        URI operation2Uri = getClass().getResource("/check_op.sl").toURI();
        Executable operation2Model = compiler.preCompile(SlangSource.fromFile(operation2Uri));
        Set<Executable> dependencies = new HashSet<>();
        dependencies.add(operation1Model);
        dependencies.add(operation2Model);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Cannot compile flow 'io.cloudslang.flow_input_in_step_same_name_as_dependency_output'. " +
                "Step 'explicit_alias' has input 'balla' with the same name as the one of the outputs of 'user.ops.test_op'.");
        List<RuntimeException> errors = compiler.validateSlangModelWithDirectDependencies(flowModel, dependencies);
        Assert.assertEquals(1, errors.size());
        throw errors.get(0);
    }

    @Test
    public void testValidationOfFlowThatCallsCorruptedFlow()throws Exception {
        URI flowUri = getClass().getResource("/corrupted/flow_that_calls_corrupted_flow.sl").toURI();

        URI operation1Uri = getClass().getResource("/test_op.sl").toURI();
        URI operation2Uri = getClass().getResource("/check_op.sl").toURI();
        URI operation3Uri = getClass().getResource("/corrupted/flow_input_in_step_same_name_as_dependency_output.sl").toURI();

        Set<SlangSource> dependencies = new HashSet<>();
        dependencies.add(SlangSource.fromFile(operation1Uri));
        dependencies.add(SlangSource.fromFile(operation2Uri));
        dependencies.add(SlangSource.fromFile(operation3Uri));

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Cannot compile flow 'io.cloudslang.flow_input_in_step_same_name_as_dependency_output'. " +
                "Step 'explicit_alias' has input 'balla' with the same name as the one of the outputs of 'user.ops.test_op'.");
        compiler.compile(SlangSource.fromFile(flowUri), dependencies);
    }

    @Test
    public void testValidationMatchingNavigation() throws Exception {
        URI resource = getClass().getResource("/corrupted/matching-navigation/parent_flow.sl").toURI();

        URI subFlow = getClass().getResource("/corrupted/matching-navigation/child_flow.sl").toURI();
        URI operation1 = getClass().getResource("/corrupted/matching-navigation/test_op.sl").toURI();
        URI operation2 = getClass().getResource("/corrupted/matching-navigation/check_weather.sl").toURI();
        URI operation3 = getClass().getResource("/corrupted/matching-navigation/get_time_zone.sl").toURI();
        URI operation4 = getClass().getResource("/corrupted/matching-navigation/check_number.sl").toURI();

        Set<SlangSource> dependencies = new HashSet<>();
        dependencies.add(SlangSource.fromFile(subFlow));
        dependencies.add(SlangSource.fromFile(operation1));
        dependencies.add(SlangSource.fromFile(operation2));
        dependencies.add(SlangSource.fromFile(operation3));
        dependencies.add(SlangSource.fromFile(operation4));

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Cannot compile flow: 'child_flow' since for step: 'step01', the result 'NEGATIVE' " +
                "of its dependency: 'user.ops.get_time_zone' has no matching navigation");
        compiler.compile(SlangSource.fromFile(resource), dependencies);
    }
}
