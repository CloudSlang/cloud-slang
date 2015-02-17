package org.openscore.lang.tools.verifier;

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
import org.openscore.lang.compiler.modeller.model.SlangFileType;
import org.openscore.lang.compiler.scorecompiler.ScoreCompiler;
import org.openscore.lang.entities.CompilationArtifact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

/*
 * Created by stoneo on 2/11/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangContentVerifierTest.Config.class)
public class SlangContentVerifierTest {

    @Autowired
    private SlangContentVerifier slangContentVerifier;

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
        slangContentVerifier.verifyAllSlangFilesInDirAreValid(null);
    }

    @Test
    public void testEmptyDirPath() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("path");
        slangContentVerifier.verifyAllSlangFilesInDirAreValid("");
    }

    @Test
    public void testIllegalDirPath() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("c/h/j");
        exception.expectMessage("directory");
        slangContentVerifier.verifyAllSlangFilesInDirAreValid("c/h/j");
    }

    @Test
    public void testPreCompileIllegalSlangFile() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        Mockito.when(slangCompiler.preCompile(any(SlangSource.class))).thenThrow(new RuntimeException());
        exception.expect(RuntimeException.class);
        slangContentVerifier.verifyAllSlangFilesInDirAreValid(resource.getPath());
    }

    @Test
    public void testNotAllSlangFilesWerePreCompiled() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        Mockito.when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(null);
        exception.expect(RuntimeException.class);
        exception.expectMessage("1");
        exception.expectMessage("0");
        exception.expectMessage("compiled");
        slangContentVerifier.verifyAllSlangFilesInDirAreValid(resource.getPath());
    }

    @Test
    public void testCompileValidSlangFileNoDependencies() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        Flow newExecutable = new Flow(null, null, null, "", "", null, null, null, new HashMap<String, SlangFileType>());
        Mockito.when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(newExecutable);
        Mockito.when(scoreCompiler.compile(newExecutable, new HashSet<Executable>())).thenReturn(new CompilationArtifact(new ExecutionPlan(), new HashMap<String, ExecutionPlan>(), null));
        slangContentVerifier.verifyAllSlangFilesInDirAreValid(resource.getPath());
    }

    @Test
    public void testCompileInvalidSlangFile() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        Flow newExecutable = new Flow(null, null, null, "", "", null, null, null, new HashMap<String, SlangFileType>());
        Mockito.when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(newExecutable);
        Mockito.when(scoreCompiler.compile(newExecutable, new HashSet<Executable>())).thenThrow(new RuntimeException());
        exception.expect(RuntimeException.class);
        slangContentVerifier.verifyAllSlangFilesInDirAreValid(resource.getPath());
    }

    @Test
    public void testNotAllSlangFilesWereCompiled() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        Flow newExecutable = new Flow(null, null, null, "", "", null, null, null, new HashMap<String, SlangFileType>());
        Mockito.when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(newExecutable);
        Mockito.when(scoreCompiler.compile(newExecutable, new HashSet<Executable>())).thenReturn(null);
        exception.expect(RuntimeException.class);
        exception.expectMessage("1");
        exception.expectMessage("0");
        exception.expectMessage("compile");
        exception.expectMessage("models");
        slangContentVerifier.verifyAllSlangFilesInDirAreValid(resource.getPath());
    }

    @Test
    public void testCompileValidSlangFileWithMissingDependencies() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        HashMap<String, SlangFileType> flowDependencies = new HashMap<>();
        flowDependencies.put("dep1", SlangFileType.EXECUTABLE);
        Flow newExecutable = new Flow(null, null, null, "", "", null, null, null, flowDependencies);
        Mockito.when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(newExecutable);
        CompilationArtifact compilationArtifact = new CompilationArtifact(new ExecutionPlan(), new HashMap<String, ExecutionPlan>(), null);
        Mockito.when(scoreCompiler.compile(newExecutable, new HashSet<Executable>())).thenReturn(compilationArtifact);
        exception.expect(RuntimeException.class);
        exception.expectMessage("dependency");
        exception.expectMessage("dep1");
        slangContentVerifier.verifyAllSlangFilesInDirAreValid(resource.getPath());
    }

    @Test
    public void testCompileValidSlangFileWithDependencies() throws Exception {
        URI resource = getClass().getResource("/dependencies").toURI();
        HashMap<String, SlangFileType> flowDependencies = new HashMap<>();
        flowDependencies.put("dependency", SlangFileType.EXECUTABLE);
        Flow emptyFlowExecutable = new Flow(null, null, null, "", "empty_flow", null, null, null, flowDependencies);
        Mockito.when(slangCompiler.preCompile(new SlangSource("", "empty_flow.sl"))).thenReturn(emptyFlowExecutable);
        Flow dependencyExecutable = new Flow(null, null, null, "", "dependency", null, null, null, new HashMap<String, SlangFileType>());
        Mockito.when(slangCompiler.preCompile(new SlangSource("", "dependency.sl"))).thenReturn(dependencyExecutable);
        CompilationArtifact compilationArtifact = new CompilationArtifact(new ExecutionPlan(), new HashMap<String, ExecutionPlan>(), null);
        HashSet<Executable> dependencies = new HashSet<>();
        dependencies.add(dependencyExecutable);
        Mockito.when(scoreCompiler.compile(emptyFlowExecutable, dependencies)).thenReturn(compilationArtifact);
        Mockito.when(scoreCompiler.compile(dependencyExecutable, new HashSet<Executable>())).thenReturn(compilationArtifact);
        slangContentVerifier.verifyAllSlangFilesInDirAreValid(resource.getPath());
    }

    @Test
    public void testInvalidNamespaceFlow() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        Flow newExecutable = new Flow(null, null, null, "wrong.namespace", "", null, null, null, new HashMap<String, SlangFileType>());
        Mockito.when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(newExecutable);
        exception.expect(RuntimeException.class);
        exception.expectMessage("Namespace");
        exception.expectMessage("wrong.namespace");
        slangContentVerifier.verifyAllSlangFilesInDirAreValid(resource.getPath());
    }

    @Test
    public void testInvalidFlowName() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        Flow newExecutable = new Flow(null, null, null, "", "wrong_name", null, null, null, new HashMap<String, SlangFileType>());
        Mockito.when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(newExecutable);
        exception.expect(RuntimeException.class);
        exception.expectMessage("Name");
        exception.expectMessage("wrong_name");
        slangContentVerifier.verifyAllSlangFilesInDirAreValid(resource.getPath());
    }

    @Test
    public void testValidFlowNameAndNamespace() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        Flow newExecutable = new Flow(null, null, null, "no_dependencies", "empty_flow", null, null, null, new HashMap<String, SlangFileType>());
        Mockito.when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(newExecutable);
        CompilationArtifact compilationArtifact = new CompilationArtifact(new ExecutionPlan(), new HashMap<String, ExecutionPlan>(), null);
        Mockito.when(scoreCompiler.compile(newExecutable, new HashSet<Executable>())).thenReturn(compilationArtifact);
        slangContentVerifier.verifyAllSlangFilesInDirAreValid(resource.getPath());
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
        public SlangContentVerifier slangContentVerifier() {
            return new SlangContentVerifier();
        }

    }
}
