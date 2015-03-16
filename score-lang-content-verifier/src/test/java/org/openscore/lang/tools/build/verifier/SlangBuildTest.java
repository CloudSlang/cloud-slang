/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.openscore.lang.tools.build.verifier;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openscore.api.ExecutionPlan;
import org.openscore.lang.compiler.SlangCompiler;
import org.openscore.lang.compiler.SlangSource;
import org.openscore.lang.compiler.modeller.model.Executable;
import org.openscore.lang.compiler.modeller.model.Flow;
import org.openscore.lang.compiler.scorecompiler.ScoreCompiler;
import org.openscore.lang.entities.CompilationArtifact;
import org.openscore.lang.entities.bindings.Input;
import org.openscore.lang.tools.build.SlangBuild;
import org.openscore.lang.tools.build.tester.SlangTestRunner;
import org.openscore.lang.tools.build.tester.parse.TestCasesYamlParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yaml.snakeyaml.Yaml;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

/*
 * Created by stoneo on 2/11/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangBuildTest.Config.class)
public class SlangBuildTest {

	private static final CompilationArtifact emptyCompilationArtifact = new CompilationArtifact(new ExecutionPlan(), new HashMap<String, ExecutionPlan>(), new ArrayList<Input>(), new ArrayList<Input>());
	private static final Flow emptyExecutable = new Flow(null, null, null, "no_dependencies", "empty_flow", null, null, null, new HashSet<String>());

    @Autowired
    private SlangBuild slangBuild;

    @Autowired
    private SlangCompiler slangCompiler;

    @Autowired
    private ScoreCompiler scoreCompiler;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void resetMocks() {
        Mockito.reset(slangCompiler);
        Mockito.reset(scoreCompiler);
    }

    @Test
    public void testNullDirPath() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("path");
        slangBuild.buildSlangContent(null, null, null);
    }

    @Test
    public void testEmptyDirPath() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("path");
        slangBuild.buildSlangContent("", null, null);
    }

    @Test
    public void testIllegalDirPath() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("c/h/j");
        exception.expectMessage("directory");
        slangBuild.buildSlangContent("c/h/j", null, null);
    }

    @Test
    public void testPreCompileIllegalSlangFile() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        Mockito.when(slangCompiler.preCompile(any(SlangSource.class))).thenThrow(new RuntimeException());
        exception.expect(RuntimeException.class);
        slangBuild.buildSlangContent(resource.getPath(), null, null);
    }

    @Test
    public void testNotAllSlangFilesWerePreCompiled() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        Mockito.when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(null);
        exception.expect(RuntimeException.class);
        exception.expectMessage("1");
        exception.expectMessage("0");
        exception.expectMessage("compiled");
        slangBuild.buildSlangContent(resource.getPath(), null, null);
    }

    @Test
    public void testCompileValidSlangFileNoDependencies() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        Mockito.when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(emptyExecutable);
        Mockito.when(scoreCompiler.compile(emptyExecutable, new HashSet<Executable>())).thenReturn(emptyCompilationArtifact);
        int numberOfCompiledSlangFiles = slangBuild.buildSlangContent(resource.getPath(), null, null);
        Assert.assertEquals("Did not compile all Slang files. Expected to compile: 1, but compiled: " + numberOfCompiledSlangFiles, numberOfCompiledSlangFiles, 1);
    }

    @Test
    public void testCompileInvalidSlangFile() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        Mockito.when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(emptyExecutable);
        Mockito.when(scoreCompiler.compile(emptyExecutable, new HashSet<Executable>())).thenThrow(new RuntimeException());
        exception.expect(RuntimeException.class);
        slangBuild.buildSlangContent(resource.getPath(), null, null);
    }

    @Test
    public void testNotAllSlangFilesWereCompiled() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        Mockito.when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(emptyExecutable);
        Mockito.when(scoreCompiler.compile(emptyExecutable, new HashSet<Executable>())).thenReturn(null);
        exception.expect(RuntimeException.class);
        exception.expectMessage("1");
        exception.expectMessage("0");
        exception.expectMessage("compile");
        exception.expectMessage("models");
        slangBuild.buildSlangContent(resource.getPath(), null, null);
    }

    @Test
    public void testCompileValidSlangFileWithMissingDependencies() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        Set<String> flowDependencies = new HashSet<>();
        flowDependencies.add("dep1");
        Flow newExecutable = new Flow(null, null, null, "no_dependencies", "empty_flow", null, null, null, flowDependencies);
        Mockito.when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(newExecutable);
        Mockito.when(scoreCompiler.compile(newExecutable, new HashSet<Executable>())).thenReturn(emptyCompilationArtifact);
        exception.expect(RuntimeException.class);
        exception.expectMessage("dependency");
        exception.expectMessage("dep1");
        slangBuild.buildSlangContent(resource.getPath(), null, null);
    }

    @Test
    public void testCompileValidSlangFileWithDependencies() throws Exception {
        URI resource = getClass().getResource("/dependencies").toURI();
        Set<String> flowDependencies = new HashSet<>();
        flowDependencies.add("dependencies.dependency");
        Flow emptyFlowExecutable = new Flow(null, null, null, "dependencies", "empty_flow", null, null, null, flowDependencies);
        Mockito.when(slangCompiler.preCompile(new SlangSource("", "empty_flow.sl"))).thenReturn(emptyFlowExecutable);
        Flow dependencyExecutable = new Flow(null, null, null, "dependencies", "dependency", null, null, null, new HashSet<String>());
        Mockito.when(slangCompiler.preCompile(new SlangSource("", "dependency.sl"))).thenReturn(dependencyExecutable);
        HashSet<Executable> dependencies = new HashSet<>();
        dependencies.add(dependencyExecutable);
        Mockito.when(scoreCompiler.compile(emptyFlowExecutable, dependencies)).thenReturn(emptyCompilationArtifact);
        Mockito.when(scoreCompiler.compile(dependencyExecutable, new HashSet<Executable>())).thenReturn(emptyCompilationArtifact);
        int numberOfCompiledSlangFiles = slangBuild.buildSlangContent(resource.getPath(), null, null);
        Assert.assertEquals("Did not compile all Slang files. Expected to compile: 2, but compiled: " + numberOfCompiledSlangFiles, numberOfCompiledSlangFiles, 2);
    }

    @Test
    public void testInvalidNamespaceFlow() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        Flow newExecutable = new Flow(null, null, null, "wrong.namespace", "empty_flow", null, null, null, new HashSet<String>());
        Mockito.when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(newExecutable);
        exception.expect(RuntimeException.class);
        exception.expectMessage("Namespace");
        exception.expectMessage("wrong.namespace");
        slangBuild.buildSlangContent(resource.getPath(), null, null);
    }

    @Test
    public void testInvalidFlowName() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        Flow newExecutable = new Flow(null, null, null, "no_dependencies", "wrong_name", null, null, null, new HashSet<String>());
        Mockito.when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(newExecutable);
        exception.expect(RuntimeException.class);
        exception.expectMessage("Name");
        exception.expectMessage("wrong_name");
        slangBuild.buildSlangContent(resource.getPath(), null, null);
    }

    @Test
    public void testValidFlowNameAndNamespace() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        Mockito.when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(emptyExecutable);
        Mockito.when(scoreCompiler.compile(emptyExecutable, new HashSet<Executable>())).thenReturn(emptyCompilationArtifact);
        int numberOfCompiledSlangFiles = slangBuild.buildSlangContent(resource.getPath(), null, null);
        Assert.assertEquals("Did not compile all Slang files. Expected to compile: 1, but compiled: " + numberOfCompiledSlangFiles, numberOfCompiledSlangFiles, 1);
    }

    @Test
    public void testValidFlowNamespaceWithAllValidCharsTypes() throws Exception {
        URI resource = getClass().getResource("/no_dependencies-0123456789").toURI();
        Flow executable = new Flow(null, null, null, "no_dependencies-0123456789", "empty_flow", null, null, null, new HashSet<String>());
        Mockito.when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(executable);
        Mockito.when(scoreCompiler.compile(executable, new HashSet<Executable>())).thenReturn(emptyCompilationArtifact);
        int numberOfCompiledSlangFiles = slangBuild.buildSlangContent(resource.getPath(), null, null);
        Assert.assertEquals("Did not compile all Slang files. Expected to compile: 1, but compiled: " + numberOfCompiledSlangFiles, numberOfCompiledSlangFiles, 1);
    }

    @Test
    public void testValidFlowNamespaceCaseInsensitive() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        Flow executable = new Flow(null, null, null, "No_Dependencies", "empty_flow", null, null, null, new HashSet<String>());
        Mockito.when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(executable);
        Mockito.when(scoreCompiler.compile(executable, new HashSet<Executable>())).thenReturn(emptyCompilationArtifact);
        int numberOfCompiledSlangFiles = slangBuild.buildSlangContent(resource.getPath(), null, null);
        Assert.assertEquals("Did not compile all Slang files. Expected to compile: 1, but compiled: " + numberOfCompiledSlangFiles, numberOfCompiledSlangFiles, 1);
    }

    @Test
    public void testNamespaceWithInvalidCharsFlow() throws Exception {
        URI resource = getClass().getResource("/invalid-chars$").toURI();
        Flow newExecutable = new Flow(null, null, null, "invalid-chars$", "empty_flow", null, null, null, new HashSet<String>());
        Mockito.when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(newExecutable);
        exception.expect(RuntimeException.class);
        exception.expectMessage("invalid-chars$");
        exception.expectMessage("alphanumeric");
        slangBuild.buildSlangContent(resource.getPath(), null, null);
    }

    @Configuration
    static class Config {

        @Bean
        public SlangCompiler slangCompiler() {
            return mock(SlangCompiler.class);
        }

        @Bean
        public ScoreCompiler scoreCompiler() {
            return mock(ScoreCompiler.class);
        }

        @Bean
        public SlangBuild slangBuild() {
            return new SlangBuild();
        }

        @Bean
        public SlangContentVerifier slangContentVerifier() {
            return new SlangContentVerifier();
        }

        @Bean
        public SlangTestRunner slangTestRunner() {
            return new SlangTestRunner();
        }

        @Bean
        public TestCasesYamlParser testCasesYamlParser(){
            return mock(TestCasesYamlParser.class);
        }

        @Bean
        public Yaml yaml(){
            return mock(Yaml.class);
        }
    }
}
