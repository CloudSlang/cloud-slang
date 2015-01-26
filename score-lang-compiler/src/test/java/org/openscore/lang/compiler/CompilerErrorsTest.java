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
        compiler.compileFlow(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testNotYamlFile() throws Exception {
        URI resource = getClass().getResource("/corrupted/not_yaml_file.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("Source");
        exception.expectMessage("YAML");
        compiler.compileFlow(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testNotOpFlowOrSysPropFile() throws Exception {
        URI resource = getClass().getResource("/corrupted/no_op_flow_prop_file.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("flow/operations/system_properties");
        compiler.compileFlow(SlangSource.fromFile(resource), path);
    }


    @Test
    public void testFlowWithWrongNavigation() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_with_navigation_to_missing_task.sl").toURI();
        URI operations = getClass().getResource("/operation.yaml").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operations));
        exception.expect(RuntimeException.class);
        exception.expectMessage("Task1");
        exception.expectMessage("Task2");
        exception.expectMessage("navigation");
        compiler.compileFlow(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithNavigationToMissingDefaultResults() throws Exception {
        URI resource = getClass().getResource("/corrupted/flow_with_navigation_to_missing_default_results.sl").toURI();
        URI operations = getClass().getResource("/operation.yaml").toURI();

        Set<SlangSource> path = new HashSet<>();
        path.add(SlangSource.fromFile(operations));
        exception.expect(RuntimeException.class);
        exception.expectMessage("Task1");
        exception.expectMessage("SUCCESS");
        exception.expectMessage("navigation");
        compiler.compileFlow(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithMissingImports() throws Exception {
        URI resource = getClass().getResource("/corrupted/missing_dependencies_imports_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("imports");
        compiler.compileFlow(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithMissingAlias() throws Exception {
        URI resource = getClass().getResource("/corrupted/missing_dependency_alias_in_imports_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("alias");
        exception.expectMessage("ops");
        compiler.compileFlow(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithMissingName() throws Exception {
        URI resource = getClass().getResource("/corrupted/missing_name_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("name");
        compiler.compileFlow(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithInputsAsString() throws Exception {
        URI resource = getClass().getResource("/corrupted/inputs_type_string_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("inputs");
        exception.expectMessage("list");
        exception.expectMessage("string");
        compiler.compileFlow(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithInputsAsMap() throws Exception {
        URI resource = getClass().getResource("/corrupted/inputs_type_map_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("inputs");
        exception.expectMessage("list");
        exception.expectMessage("map");
        compiler.compileFlow(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithNoWorkflow() throws Exception {
        URI resource = getClass().getResource("/corrupted/no_workflow_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("no_workflow");
        exception.expectMessage("workflow");
        exception.expectMessage("property");
        compiler.compileFlow(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithNoWorkflowData() throws Exception {
        URI resource = getClass().getResource("/corrupted/no_workflow_data_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("no_workflow");
        exception.expectMessage("workflow");
        exception.expectMessage("data");
        compiler.compileFlow(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowTaskWithNoData() throws Exception {
        URI resource = getClass().getResource("/corrupted/no_task_data_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("task1");
        exception.expectMessage("data");
        compiler.compileFlow(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithTasksAsList() throws Exception {
        URI resource = getClass().getResource("/corrupted/tasks_type_list_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("tasks_type_list");
        exception.expectMessage("map");
        exception.expectMessage("list");
        compiler.compileFlow(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithNoRefTask() throws Exception {
        URI resource = getClass().getResource("/corrupted/task_with_no_ref_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("task1");
        exception.expectMessage("reference");
        compiler.compileFlow(SlangSource.fromFile(resource), path);
    }

    @Test
    public void testFlowWithRefAsListTask() throws Exception {
        URI resource = getClass().getResource("/corrupted/task_with_list_of_ref_flow.sl").toURI();

        Set<SlangSource> path = new HashSet<>();
        exception.expect(RuntimeException.class);
        exception.expectMessage("task1");
        exception.expectMessage("map");
        exception.expectMessage("list");
        exception.expectMessage("-");
        compiler.compileFlow(SlangSource.fromFile(resource), path);
    }


//    @Test
//    public void testFlowWithMissingRefTask() throws Exception {
//        URI resource = getClass().getResource("/corrupted/task_with_missing_ref_flow.sl").toURI();
//        URI op = getClass().getResource("/corrupted/no_op.sl").toURI();
//
//        Set<SlangSource> path = new HashSet<>();
//        path.add(SlangSource.fromFile(op));
//        exception.expect(RuntimeException.class);
//        exception.expectMessage("task1");
//        exception.expectMessage("reference");
//        compiler.compileFlow(SlangSource.fromFile(resource), path);
//    }

}
