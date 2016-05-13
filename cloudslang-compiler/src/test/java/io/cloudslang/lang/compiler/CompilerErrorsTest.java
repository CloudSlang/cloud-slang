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
        exception.expectMessage("Source");
        exception.expectMessage("empty");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testNotYamlFile() throws Exception {
        URI resource = getClass().getResource("/corrupted/not_yaml_file.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Source");
        exception.expectMessage("YAML");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testNotOpFlowFile() throws Exception {
        URI resource = getClass().getResource("/corrupted/no_op_flow_file.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("flow/operation");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

	@Test
	public void testSystemProperties() throws Exception {
		URI systemProperties = getClass().getResource("/corrupted/system_properties.yaml").toURI();
		Set<SlangSource> path = new HashSet<>();
		exception.expect(RuntimeException.class);
		exception.expectMessage("problem parsing");
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
		exception.expectMessage("problem parsing");
        compiler.compile(SlangSource.fromFile(flow), path);
    }

    @Test
    public void testFlowWithNavigationToMissingStep() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_with_navigation_to_missing_step.sl").toURI();
        URI operations = getClass().getResource("/java_op.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operations));
        exception.expect(RuntimeException.class);
        exception.expectMessage("Step1");
        exception.expectMessage("Step2");
        exception.expectMessage("navigation");
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
        exception.expectMessage("not supported by CloudSlang");
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
        exception.expectMessage("Step1");
        exception.expectMessage("SUCCESS");
        exception.expectMessage("navigation");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithMissingNavigationFromOperationResult() throws Exception {
        URI resource = getClass().getResource("/corrupted/step_with_missing_navigation_from_operation_result_flow.sl").toURI();
        URI operations = getClass().getResource("/java_op.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operations));
        exception.expect(RuntimeException.class);
        exception.expectMessage("step1");
        exception.expectMessage("FAILURE");
        exception.expectMessage("user.ops.java_op");
        exception.expectMessage("navigation");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithMissingImports() throws Exception {
        URI resource = getClass().getResource("/corrupted/missing_dependencies_imports_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("imports");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testOpWithMissingNamespace() throws Exception {
        URI resource = getClass().getResource("/corrupted/op_without_namespace.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("must have a namespace");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithMissingName() throws Exception {
        URI resource = getClass().getResource("/corrupted/missing_name_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("name");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithInputsAsString() throws Exception {
        URI resource = getClass().getResource("/corrupted/inputs_type_string_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("inputs");
        exception.expectMessage("list");
        exception.expectMessage("string");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithInputsAsMap() throws Exception {
        URI resource = getClass().getResource("/corrupted/inputs_type_map_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("inputs");
        exception.expectMessage("list");
        exception.expectMessage("map");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithIllegalTypeInput() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_with_wrong_type_input.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Input");
        exception.expectMessage("3");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithNoWorkflow() throws Exception {
        URI resource = getClass().getResource("/corrupted/no_workflow_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("no_workflow");
        exception.expectMessage("workflow");
        exception.expectMessage("property");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithNoWorkflowData() throws Exception {
        URI resource = getClass().getResource("/corrupted/no_workflow_data_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("no_workflow");
        exception.expectMessage("workflow");
        exception.expectMessage("data");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowStepWithNoData() throws Exception {
        URI resource = getClass().getResource("/corrupted/no_step_data_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("step1");
        exception.expectMessage("data");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowStepWithTwoKeysUnderDo() throws Exception {
        URI resource = getClass().getResource("/corrupted/multiple_keys_under_do.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("step1");
        exception.expectMessage("to many keys");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithStepsAsList() throws Exception {
        URI resource = getClass().getResource("/corrupted/workflow_with_step_map.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("workflow_with_step_map");
        exception.expectMessage("map");
        exception.expectMessage("list");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithOnFailureStepsAsList() throws Exception {
        URI resource = getClass().getResource("/corrupted/on_failure_with_step_map.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("on_failure_with_step_map");
        exception.expectMessage("map");
        exception.expectMessage("list");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithNoRefStep() throws Exception {
        URI resource = getClass().getResource("/corrupted/step_with_no_ref_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("step1");
        exception.expectMessage("reference");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testStepWithListOfOps() throws Exception {
        URI resource = getClass().getResource("/corrupted/step_with_list_of_ops.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("step1");
        exception.expectMessage("map");
        exception.expectMessage("list");
        exception.expectMessage("-");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testStepWithListOfDos() throws Exception {
        URI resource = getClass().getResource("/corrupted/step_with_list_of_do_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("step1");
        exception.expectMessage("map");
        exception.expectMessage("do:");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithMissingRefInPath() throws Exception {
        URI resource = getClass().getResource("/basic_flow.yaml").toURI();
        URI op = getClass().getResource("/operation_with_data.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(op));
        exception.expect(RuntimeException.class);
        exception.expectMessage("Reference");
        exception.expectMessage("test_op");
        exception.expectMessage("basic_flow");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testInputPrivateAndNoDefault() throws Exception {
        URI resource = getClass().getResource("/private_input_without_default.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("private");
        exception.expectMessage("default");
        exception.expectMessage("input_without_default");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testInputWithInvalidKey() throws Exception {
        URI resource = getClass().getResource("/illegal_key_in_input.sl").toURI();
        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("known property");
        exception.expectMessage("input_with_illegal_key");
        exception.expectMessage("karambula");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testOperationWithNoActionData() throws Exception {
        URI resource = getClass().getResource("/corrupted/operation_with_no_action_data.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("operation_with_no_action_data");
        exception.expectMessage("action");
        exception.expectMessage("data");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testOperationWithListOfActions() throws Exception {
        URI resource = getClass().getResource("/corrupted/operation_with_list_of_actions.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("operation_with_list_of_actions");
        exception.expectMessage("map");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testOperationWithListOfActionTypes() throws Exception {
        URI resource = getClass().getResource("/corrupted/operation_with_list_of_action_types.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("'python_action'");
        exception.expectMessage("there should be a map of values, but instead there is a list");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testParentFlowWithCorruptedSubFlow() throws Exception {
        URI resource = getClass().getResource("/corrupted/parent_flow_to_no_step_data_flow.sl").toURI();
        URI subFlow = getClass().getResource("/corrupted/no_step_data_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(subFlow));
        exception.expect(RuntimeException.class);
        exception.expectMessage("step1");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testStepWithNavigateAsString() throws Exception {
        URI resource = getClass().getResource("/corrupted/step_with_string_navigate_value.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("'navigate'");
        exception.expectMessage("list");
        exception.expectMessage("string");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testStepWithIllegalTypeOfNavigate() throws Exception {
        URI resource = getClass().getResource("/corrupted/step_with_illegal_navigate_type.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("step1");
        exception.expectMessage("navigate");
        exception.expectMessage("3");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testDuplicateStepNamesInFlow() throws Exception {
        URI resource = getClass().getResource("/corrupted/duplicate_step_name.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Step1");
        exception.expectMessage("unique");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testNullValueInputFlow() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_with_null_value_input.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Input");
        exception.expectMessage("input1");
        exception.expectMessage("null");
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
        exception.expectMessage("step1");
        exception.expectMessage("FAILURE");
        exception.expectMessage("user.ops.java_op");
        exception.expectMessage("navigation");
        List<RuntimeException> errors = compiler.validateSlangModelWithDependencies(flowModel, dependencies);
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
        exception.expectMessage("explicit_alias");
        exception.expectMessage("user.ops.test_op");
        exception.expectMessage("mandatory");
        exception.expectMessage("inputs");
        exception.expectMessage("alla");
        List<RuntimeException> errors = compiler.validateSlangModelWithDependencies(flowModel, dependencies);
        Assert.assertEquals(1, errors.size());
        throw errors.get(0);
    }
}
