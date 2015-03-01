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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.openscore.lang.compiler.configuration.SlangCompilerSpringConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.util.HashSet;
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
        URI flow = getClass().getResource("/flow.yaml").toURI();
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
    public void testFlowWithNavigationToMissingTask() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_with_navigation_to_missing_task.sl").toURI();
        URI operations = getClass().getResource("/java_op.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operations));
        exception.expect(RuntimeException.class);
        exception.expectMessage("Task1");
        exception.expectMessage("Task2");
        exception.expectMessage("navigation");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithNavigationToMissingDefaultResults() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_with_navigation_to_missing_default_results.sl").toURI();
        URI operations = getClass().getResource("/java_op.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operations));
        exception.expect(RuntimeException.class);
        exception.expectMessage("Task1");
        exception.expectMessage("SUCCESS");
        exception.expectMessage("navigation");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithMissingNavigationFromOperationResult() throws Exception {
        URI resource = getClass().getResource("/corrupted/task_with_missing_navigation_from_operation_result_flow.sl").toURI();
        URI operations = getClass().getResource("/java_op.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operations));
        exception.expect(RuntimeException.class);
        exception.expectMessage("task1");
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
    public void testFlowWithMissingAlias() throws Exception {
        URI resource = getClass().getResource("/corrupted/missing_dependency_alias_in_imports_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("alias");
        exception.expectMessage("ops");
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
    public void testFlowTaskWithNoData() throws Exception {
        URI resource = getClass().getResource("/corrupted/no_task_data_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("task1");
        exception.expectMessage("data");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithTasksAsList() throws Exception {
        URI resource = getClass().getResource("/corrupted/workflow_with_task_map.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("workflow_with_task_map");
        exception.expectMessage("map");
        exception.expectMessage("list");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithOnFailureTasksAsList() throws Exception {
        URI resource = getClass().getResource("/corrupted/on_failure_with_task_map.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("on_failure_with_task_map");
        exception.expectMessage("map");
        exception.expectMessage("list");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithNoRefTask() throws Exception {
        URI resource = getClass().getResource("/corrupted/task_with_no_ref_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("task1");
        exception.expectMessage("reference");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testTaskWithListOfOps() throws Exception {
        URI resource = getClass().getResource("/corrupted/task_with_list_of_ops.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("task1");
        exception.expectMessage("map");
        exception.expectMessage("list");
        exception.expectMessage("-");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testTaskWithListOfDos() throws Exception {
        URI resource = getClass().getResource("/corrupted/task_with_list_of_do_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("task1");
        exception.expectMessage("map");
        exception.expectMessage("do:");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithMissingRefInPath() throws Exception {
        URI resource = getClass().getResource("/flow.yaml").toURI();
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
        exception.expectMessage("operation_with_list_of_action_types");
        exception.expectMessage("'action'");
        exception.expectMessage("'python_script:'");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testParentFlowWithCorruptedSubFlow() throws Exception {
        URI resource = getClass().getResource("/corrupted/parent_flow_to_no_task_data_flow.sl").toURI();
        URI subFlow = getClass().getResource("/corrupted/no_task_data_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(subFlow));
        exception.expect(RuntimeException.class);
        exception.expectMessage("no_task_data_flow.sl");
        exception.expectMessage("Error");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testTaskWithNavigateAsString() throws Exception {
        URI resource = getClass().getResource("/corrupted/task_with_string_navigate_value.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("'navigate'");
        exception.expectMessage("map");
        exception.expectMessage("string");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testTaskWithIllegalTypeOfNavigate() throws Exception {
        URI resource = getClass().getResource("/corrupted/task_with_illegal_navigate_type.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("task1");
        exception.expectMessage("navigate");
        exception.expectMessage("3");
        compiler.compile(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testDuplicateTaskNamesInFlow() throws Exception {
        URI resource = getClass().getResource("/corrupted/duplicate_task_name.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Task1");
        exception.expectMessage("unique");
        compiler.compile(SlangSource.fromFile(resource), path);
    }
}
