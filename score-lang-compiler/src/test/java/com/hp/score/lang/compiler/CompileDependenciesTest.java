package com.hp.score.lang.compiler;
/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/

/*
 * Created by orius123 on 05/11/14.
 */

import com.hp.score.api.ExecutionPlan;
import com.hp.score.lang.compiler.configuration.SlangCompilerSpringConfig;
import com.hp.score.lang.entities.CompilationArtifact;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
public class CompileDependenciesTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    private SlangCompiler compiler;

    @Test(expected = IllegalArgumentException.class)
    public void emptyPathButThereAreImports() throws Exception {
        URI flow = getClass().getResource("/flow.yaml").toURI();
        List<File> path = new ArrayList<>();
        compiler.compileFlow(new File(flow), path);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullPathButThereAreImports() throws Exception {
        URI flow = getClass().getResource("/flow.yaml").toURI();
        compiler.compileFlow(new File(flow), null);
    }

    @Test
    public void referenceDoesNoExistInPath() throws Exception {
        URI flow = getClass().getResource("/flow.yaml").toURI();
        URI operation = getClass().getResource("/operation_with_data.yaml").toURI();
        List<File> path = new ArrayList<>();
        path.add(new File(operation));

        exception.expect(RuntimeException.class);
        exception.expectMessage(containsString("ops.test_op"));

        compiler.compileFlow(new File(flow), path);
    }

    @Test
    public void importHasAKeyThatDoesNotExistInPath() throws Exception {
        URI flow = getClass().getResource("/flow.yaml").toURI();
        URI operation = getClass().getResource("/flow_with_data.yaml").toURI();
        List<File> path = new ArrayList<>();
        path.add(new File(operation));

        exception.expect(RuntimeException.class);
        exception.expectMessage(containsString("ops"));

        compiler.compileFlow(new File(flow), path);
    }

    @Test
    public void filesThatAreNotImportedShouldNotBeCompiled() throws Exception {
        URI flow = getClass().getResource("/flow.yaml").toURI();
        URI notImportedOperation = getClass().getResource("/flow_with_data.yaml").toURI();
        URI importedOperation = getClass().getResource("/operation.yaml").toURI();
        List<File> path = new ArrayList<>();
        path.add(new File(notImportedOperation));
        path.add(new File(importedOperation));

        CompilationArtifact compilationArtifact = compiler.compileFlow(new File(flow), path);
        Assert.assertThat(compilationArtifact.getDependencies(), Matchers.<String, ExecutionPlan>hasKey("user.ops.test_op"));
        Assert.assertThat(compilationArtifact.getDependencies(), not(Matchers.<String, ExecutionPlan>hasKey("slang.sample.flows.SimpleFlow")));
    }

    @Test
    public void pathHasDirectoriesInIt() throws Exception {
        URI flow = getClass().getResource("/flow.yaml").toURI();
        URI notImportedOperation = getClass().getResource("/").toURI();
        List<File> path = new ArrayList<>();
        path.add(new File(notImportedOperation));

        CompilationArtifact compilationArtifact = compiler.compileFlow(new File(flow), path);
        Assert.assertThat(compilationArtifact.getDependencies(), Matchers.<String, ExecutionPlan>hasKey("user.ops.test_op"));
        Assert.assertThat(compilationArtifact.getDependencies(), not(Matchers.<String, ExecutionPlan>hasKey("slang.sample.flows.SimpleFlow")));
    }

    @Test
    public void sourceFileIsADirectory() throws Exception {
        URI dir = getClass().getResource("/").toURI();

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("directory"));

        compiler.compileFlow(new File(dir), null);
    }

    @Test
    public void bothFileAreDependentOnTheSameFile() throws Exception {
        URI flow = getClass().getResource("/circular-dependencies/parent_flow.yaml").toURI();
        URI child_flow = getClass().getResource("/circular-dependencies/child_flow.yaml").toURI();
        URI operation = getClass().getResource("/operation.yaml").toURI();
        List<File> path = new ArrayList<>();
        path.add(new File(child_flow));
        path.add(new File(operation));
        CompilationArtifact compilationArtifact = compiler.compileFlow(new File(flow), path);
        ExecutionPlan executionPlan = compilationArtifact.getExecutionPlan();
        Assert.assertNotNull(executionPlan);
        Assert.assertEquals("different number of dependencies than expected", 5, compilationArtifact.getDependencies().size());
    }

    @Test
    public void circularDependencies() throws Exception {
        URI flow = getClass().getResource("/circular-dependencies/parent_flow.yaml").toURI();
        URI child_flow = getClass().getResource("/circular-dependencies/circular_child_flow.yaml").toURI();
        URI operation = getClass().getResource("/operation.yaml").toURI();
        List<File> path = new ArrayList<>();
        path.add(new File(child_flow));
        path.add(new File(operation));
        CompilationArtifact compilationArtifact = compiler.compileFlow(new File(flow), path);
        ExecutionPlan executionPlan = compilationArtifact.getExecutionPlan();
        Assert.assertNotNull(executionPlan);
    }

}