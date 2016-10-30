/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.tools.build;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.cloudslang.lang.api.Slang;
import io.cloudslang.lang.commons.services.api.SlangSourceService;
import io.cloudslang.lang.compiler.MetadataExtractor;
import io.cloudslang.lang.compiler.SlangCompiler;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.compiler.modeller.model.Metadata;
import io.cloudslang.lang.compiler.scorecompiler.ScoreCompiler;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.tools.build.tester.IRunTestResults;
import io.cloudslang.lang.tools.build.tester.RunTestsResults;
import io.cloudslang.lang.tools.build.tester.SlangTestRunner;
import io.cloudslang.lang.tools.build.tester.TestRun;
import io.cloudslang.lang.tools.build.tester.parallel.report.ThreadSafeRunTestResults;
import io.cloudslang.lang.tools.build.tester.parallel.services.ParallelTestCaseExecutorService;
import io.cloudslang.lang.tools.build.tester.parallel.services.TestCaseEventDispatchService;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import io.cloudslang.lang.tools.build.tester.parse.TestCasesYamlParser;
import io.cloudslang.lang.tools.build.validation.StaticValidator;
import io.cloudslang.lang.tools.build.validation.StaticValidatorImpl;
import io.cloudslang.lang.tools.build.verifier.SlangContentVerifier;
import io.cloudslang.score.api.ExecutionPlan;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yaml.snakeyaml.Yaml;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/*
 * Created by stoneo on 2/11/2015.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangBuildTest.Config.class)
public class SlangBuildTest {

    private static final Set<String> SYSTEM_PROPERTY_DEPENDENCIES = Collections.emptySet();
    private static final CompilationArtifact EMPTY_COMPILATION_ARTIFACT =
            new CompilationArtifact(
                    new ExecutionPlan(),
                    new HashMap<String, ExecutionPlan>(),
                    new ArrayList<Input>(),
                    new HashSet<String>()
            );
    private static final Flow EMPTY_EXECUTABLE = new Flow(null, null, null, "no_dependencies", "empty_flow", null, null, null, new HashSet<String>(), SYSTEM_PROPERTY_DEPENDENCIES);
    private static final Metadata EMPTY_METADATA = new Metadata();

    @Autowired
    private SlangBuilder slangBuilder;

    @Autowired
    private SlangCompiler slangCompiler;

    @Autowired
    private ScoreCompiler scoreCompiler;

    @Autowired
    private MetadataExtractor metadataExtractor;

    @Autowired
    private StaticValidator staticValidator;

    @Autowired
    private SlangTestRunner slangTestRunner;

    @Autowired
    public ParallelTestCaseExecutorService parallelTestCaseExecutorService;

    @Autowired
    public TestCaseEventDispatchService testCaseEventDispatchService;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void resetMocks() {
        reset(slangCompiler);
        reset(scoreCompiler);
        reset(slangTestRunner);
    }

    @Test
    public void testNullDirPath() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("path");
        slangBuilder.buildSlangContent(null, null, null, null, false, false);
    }

    @Test
    public void testEmptyDirPath() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("path");
        slangBuilder.buildSlangContent("", "content", null, null, false, false);
    }

    @Test
    public void testParallelFlag() throws Exception {
        Path testPath = null;
        try {
            String projectPath = "aaa/bb/cc";
            List<String> suites = Lists.newArrayList("suite1", "suite2");
            testPath = Files.createTempDirectory("testPath");
            String testPathString = testPath.toString();

            doReturn(new ThreadSafeRunTestResults()).when(slangTestRunner).runAllTestsParallel(eq(projectPath), anyMap(), anyMap(), eq(suites));
            doReturn(Maps.newHashMap()).when(slangTestRunner).createTestCases(anyString(), anySet());
            doReturn(new ThreadSafeRunTestResults()).when(slangTestRunner).runAllTestsParallel(anyString(), anyMap(), anyMap(), anyList());

            slangBuilder.runTests(Maps.<String, Executable>newHashMap(), projectPath, testPathString, suites, true);
            verify(slangTestRunner).runAllTestsParallel(eq(projectPath), anyMap(), anyMap(), eq(suites));
            verify(slangTestRunner, never()).runAllTestsSequential(anyString(), anyMap(), anyMap(), anyList());

        } finally {
            if (testPath != null) {
                FileUtils.deleteQuietly(testPath.toFile());
            }
        }
    }

    @Test
    public void testIllegalDirPath() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("c/h/j");
        exception.expectMessage("directory");
        slangBuilder.buildSlangContent("c/h/j", "c/h/j/content", null, null, false, false);
    }

    @Test
    public void testPreCompileIllegalSlangFile() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        when(slangCompiler.preCompile(any(SlangSource.class))).thenThrow(new RuntimeException());
        exception.expect(RuntimeException.class);
        SlangBuildResults slangBuildResults = slangBuilder.buildSlangContent(resource.getPath(), resource.getPath(), null, null, false, false);
        assertNotNull(slangBuildResults.getCompilationExceptions());
        assertTrue(slangBuildResults.getCompilationExceptions().size() > 0);
        throw slangBuildResults.getCompilationExceptions().get(0);
    }

    @Test
    public void testNotAllSlangFilesWerePreCompiled() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(null);
        exception.expect(RuntimeException.class);
        exception.expectMessage("1");
        exception.expectMessage("0");
        exception.expectMessage("compiled");
        SlangBuildResults slangBuildResults = slangBuilder.buildSlangContent(resource.getPath(), resource.getPath(), null, null, false, false);
        assertNotNull(slangBuildResults.getCompilationExceptions());
        assertTrue(slangBuildResults.getCompilationExceptions().size() > 0);
        throw slangBuildResults.getCompilationExceptions().get(0);
    }

    @Test
    public void testCompileValidSlangFileNoDependencies() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(EMPTY_EXECUTABLE);
        when(scoreCompiler.compile(EMPTY_EXECUTABLE, new HashSet<Executable>())).thenReturn(EMPTY_COMPILATION_ARTIFACT);
        SlangBuildResults buildResults = slangBuilder.buildSlangContent(resource.getPath(), resource.getPath(), null, null, false, false);
        int numberOfCompiledSlangFiles = buildResults.getNumberOfCompiledSources();
        assertEquals("Did not compile all Slang files. Expected to compile: 1, but compiled: " + numberOfCompiledSlangFiles, numberOfCompiledSlangFiles, 1);
    }

    @Test
    public void testCompileInvalidSlangFile() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(EMPTY_EXECUTABLE);
        when(scoreCompiler.compile(EMPTY_EXECUTABLE, new HashSet<Executable>())).thenThrow(new RuntimeException());
        exception.expect(RuntimeException.class);
        slangBuilder.buildSlangContent(resource.getPath(), resource.getPath(), null, null, false, false);
    }

    @Test
    public void testNotAllSlangFilesWereCompiled() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(EMPTY_EXECUTABLE);
        when(scoreCompiler.compile(EMPTY_EXECUTABLE, new HashSet<Executable>())).thenReturn(null);
        exception.expect(RuntimeException.class);
        exception.expectMessage("1");
        exception.expectMessage("0");
        exception.expectMessage("compile");
        exception.expectMessage("models");
        slangBuilder.buildSlangContent(resource.getPath(), resource.getPath(), null, null, false, false);
    }

    @Test
    public void testCompileValidSlangFileWithMissingDependencies() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        Set<String> flowDependencies = new HashSet<>();
        flowDependencies.add("dep1");
        Flow newExecutable = new Flow(null, null, null, "no_dependencies", "empty_flow", null, null, null, flowDependencies, SYSTEM_PROPERTY_DEPENDENCIES);
        when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(newExecutable);
        when(scoreCompiler.compile(newExecutable, new HashSet<Executable>())).thenReturn(EMPTY_COMPILATION_ARTIFACT);
        when(metadataExtractor.extractMetadata(any(SlangSource.class))).thenReturn(EMPTY_METADATA);
        doCallRealMethod().when(staticValidator).validateSlangFile(any(File.class), eq(newExecutable), eq(EMPTY_METADATA), eq(false));
        exception.expect(RuntimeException.class);
        exception.expectMessage("dependency");
        exception.expectMessage("dep1");
        slangBuilder.buildSlangContent(resource.getPath(), resource.getPath(), null, null, false ,false);
    }

    @Test
    public void testCompileValidSlangFileWithDependencies() throws Exception {
        URI resource = getClass().getResource("/dependencies").toURI();
        Set<String> flowDependencies = new HashSet<>();
        flowDependencies.add("dependencies.dependency");
        Flow emptyFlowExecutable = new Flow(null, null, null, "dependencies", "empty_flow", null, null, null, flowDependencies, SYSTEM_PROPERTY_DEPENDENCIES);
        when(slangCompiler.preCompile(new SlangSource("", "empty_flow"))).thenReturn(emptyFlowExecutable);
        Flow dependencyExecutable = new Flow(null, null, null, "dependencies", "dependency", null, null, null, new HashSet<String>(), SYSTEM_PROPERTY_DEPENDENCIES);
        when(slangCompiler.preCompile(new SlangSource("", "dependency"))).thenReturn(dependencyExecutable);
        HashSet<Executable> dependencies = new HashSet<>();
        dependencies.add(dependencyExecutable);
        when(scoreCompiler.compile(emptyFlowExecutable, dependencies)).thenReturn(EMPTY_COMPILATION_ARTIFACT);
        when(scoreCompiler.compile(dependencyExecutable, new HashSet<Executable>())).thenReturn(EMPTY_COMPILATION_ARTIFACT);
        SlangBuildResults buildResults = slangBuilder.buildSlangContent(resource.getPath(), resource.getPath(), null, null, false, false);
        int numberOfCompiledSlangFiles = buildResults.getNumberOfCompiledSources();
        // properties file should be ignored
        assertEquals("Did not compile all Slang files. Expected to compile: 2, but compiled: " + numberOfCompiledSlangFiles, numberOfCompiledSlangFiles, 2);
    }

    @Test
    public void testInvalidNamespaceFlow() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        Flow newExecutable = new Flow(null, null, null, "wrong.namespace", "empty_flow", null, null, null, new HashSet<String>(), SYSTEM_PROPERTY_DEPENDENCIES);
        when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(newExecutable);
        when(metadataExtractor.extractMetadata(any(SlangSource.class))).thenReturn(EMPTY_METADATA);
        doCallRealMethod().when(staticValidator).validateSlangFile(any(File.class), eq(newExecutable), eq(EMPTY_METADATA), eq(false));
        exception.expect(RuntimeException.class);
        exception.expectMessage("Namespace");
        exception.expectMessage("wrong.namespace");
        SlangBuildResults slangBuildResults = slangBuilder.buildSlangContent(resource.getPath(), resource.getPath(), null, null, false, false);
        assertNotNull(slangBuildResults.getCompilationExceptions());
        assertTrue(slangBuildResults.getCompilationExceptions().size() > 0);
        throw slangBuildResults.getCompilationExceptions().get(0);
    }

    @Test
    public void testInvalidFlowName() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        Flow newExecutable = new Flow(null, null, null, "no_dependencies", "wrong_name", null, null, null, new HashSet<String>(), SYSTEM_PROPERTY_DEPENDENCIES);
        when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(newExecutable);
        when(metadataExtractor.extractMetadata(any(SlangSource.class))).thenReturn(EMPTY_METADATA);
        doCallRealMethod().when(staticValidator).validateSlangFile(any(File.class), eq(newExecutable), eq(EMPTY_METADATA), eq(false));
        exception.expect(RuntimeException.class);
        exception.expectMessage("Name");
        exception.expectMessage("wrong_name");
        SlangBuildResults slangBuildResults = slangBuilder.buildSlangContent(resource.getPath(), resource.getPath(), null, null, false, false);
        assertNotNull(slangBuildResults.getCompilationExceptions());
        assertTrue(slangBuildResults.getCompilationExceptions().size() > 0);
        throw slangBuildResults.getCompilationExceptions().get(0);
    }

    @Test
    public void testValidFlowNamespaceWithAllValidCharsTypes() throws Exception {
        URI resource = getClass().getResource("/no_dependencies-0123456789").toURI();
        Flow executable = new Flow(null, null, null, "no_dependencies-0123456789", "empty_flow", null, null, null, new HashSet<String>(), SYSTEM_PROPERTY_DEPENDENCIES);
        when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(executable);
        when(scoreCompiler.compile(executable, new HashSet<Executable>())).thenReturn(EMPTY_COMPILATION_ARTIFACT);
        SlangBuildResults buildResults = slangBuilder.buildSlangContent(resource.getPath(), resource.getPath(), null, null, false, false);
        int numberOfCompiledSlangFiles = buildResults.getNumberOfCompiledSources();
        assertEquals("Did not compile all Slang files. Expected to compile: 1, but compiled: " + numberOfCompiledSlangFiles, numberOfCompiledSlangFiles, 1);
    }

    @Test
    public void testValidFlowNamespaceCaseInsensitive() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        Flow executable = new Flow(null, null, null, "No_Dependencies", "empty_flow", null, null, null, new HashSet<String>(), SYSTEM_PROPERTY_DEPENDENCIES);
        when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(executable);
        when(scoreCompiler.compile(executable, new HashSet<Executable>())).thenReturn(EMPTY_COMPILATION_ARTIFACT);
        SlangBuildResults buildResults = slangBuilder.buildSlangContent(resource.getPath(), resource.getPath(), null, null, false, false);
        int numberOfCompiledSlangFiles = buildResults.getNumberOfCompiledSources();
        assertEquals("Did not compile all Slang files. Expected to compile: 1, but compiled: " + numberOfCompiledSlangFiles, numberOfCompiledSlangFiles, 1);
    }

    @Test
    public void testNamespaceWithInvalidCharsFlow() throws Exception {
        URI resource = getClass().getResource("/invalid-chars$").toURI();
        Flow newExecutable = new Flow(null, null, null, "invalid-chars$", "empty_flow", null, null, null, new HashSet<String>(), SYSTEM_PROPERTY_DEPENDENCIES);
        when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(newExecutable);
        when(metadataExtractor.extractMetadata(any(SlangSource.class))).thenReturn(EMPTY_METADATA);
        doCallRealMethod().when(staticValidator).validateSlangFile(any(File.class), eq(newExecutable), eq(EMPTY_METADATA), eq(false));
        exception.expect(RuntimeException.class);
        exception.expectMessage("invalid-chars$");
        exception.expectMessage("alphanumeric");
        SlangBuildResults slangBuildResults = slangBuilder.buildSlangContent(resource.getPath(), resource.getPath(), null, null, false, false);
        assertNotNull(slangBuildResults.getCompilationExceptions());
        assertTrue(slangBuildResults.getCompilationExceptions().size() > 0);
        throw slangBuildResults.getCompilationExceptions().get(0);
    }

    @Test
    public void testCompileSlangFileAndRunTests() throws Exception {
        URI contentResource = getClass().getResource("/no_dependencies").toURI();
        URI testResource = getClass().getResource("/test/valid").toURI();
        when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(EMPTY_EXECUTABLE);
        when(scoreCompiler.compile(EMPTY_EXECUTABLE, new HashSet<Executable>())).thenReturn(EMPTY_COMPILATION_ARTIFACT);
        RunTestsResults runTestsResults = new RunTestsResults();
        runTestsResults.addFailedTest("test1", new TestRun(new SlangTestCase("test1", "", null, null, null, null, null, null, null), "message"));
        when(slangTestRunner.runAllTestsSequential((any(String.class)), anyMap(), anyMap(), anyList())).thenReturn(runTestsResults);
        SlangBuildResults buildResults = slangBuilder.buildSlangContent(contentResource.getPath(), contentResource.getPath(), testResource.getPath(), null, false, false);
        int numberOfCompiledSlangFiles = buildResults.getNumberOfCompiledSources();
        IRunTestResults actualRunTestsResults = buildResults.getRunTestsResults();
        assertEquals("Did not compile all Slang files. Expected to compile: 1, but compiled: " + numberOfCompiledSlangFiles, numberOfCompiledSlangFiles, 1);
        assertEquals("1 test case should fail", 1, actualRunTestsResults.getFailedTests().size());
    }

    @Test
    public void testTestCaseWithIncorrectTestFlowReference() throws Exception {
        URI contentResource = getClass().getResource("/no_dependencies").toURI();
        URI testResource = getClass().getResource("/test/valid").toURI();
        when(slangCompiler.preCompile(any(SlangSource.class))).thenReturn(EMPTY_EXECUTABLE);
        when(scoreCompiler.compile(EMPTY_EXECUTABLE, new HashSet<Executable>())).thenReturn(EMPTY_COMPILATION_ARTIFACT);
        RunTestsResults runTestsResults = new RunTestsResults();
        runTestsResults.addFailedTest("test1", new TestRun(new SlangTestCase("test1", "", null, null, null, null, null, null, null), "message"));
        when(
                slangTestRunner.runAllTestsSequential(
                        any(String.class),
                        anyMapOf(String.class, SlangTestCase.class),
                        anyMapOf(String.class, CompilationArtifact.class),
                        anyListOf(String.class)
                )
        ).thenReturn(runTestsResults);

        Map<String, SlangTestCase> testCases = new HashMap<>();
        SlangTestCase testCaseWithIncorrectFlowPath = new SlangTestCase(
                "i_don_t_exist",
                "a.b.c.i_don_t_exist",
                "",
                Collections.<String>emptyList(),
                "",
                Collections.<Map>emptyList(),
                Collections.<Map>emptyList(),
                false,
                ""
        );
        testCases.put("i_don_t_exist", testCaseWithIncorrectFlowPath);
        when(slangTestRunner.createTestCases(anyString(), anySetOf(String.class))).thenReturn(testCases);

        SlangBuildResults buildResults = slangBuilder.buildSlangContent(contentResource.getPath(), contentResource.getPath(), testResource.getPath(), null, false, false);

        // test case: test flow path points to non existing executable
        // validate execution does not return when detects this situation and coverage data is added to results
        assertEquals(1, buildResults.getRunTestsResults().getUncoveredExecutables().size());
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
        public SlangBuilder slangBuild() {
            return new SlangBuilder();
        }

        @Bean
        public SlangContentVerifier slangContentVerifier() {
            return new SlangContentVerifier();
        }

        @Bean
        public SlangTestRunner slangTestRunner() {
            return mock(SlangTestRunner.class);
        }

        @Bean
        public TestCasesYamlParser testCasesYamlParser() {
            return mock(TestCasesYamlParser.class);
        }

        @Bean
        public Yaml yaml() {
            return mock(Yaml.class);
        }

        @Bean
        public Slang slang() {
            return mock(Slang.class);
        }

        @Bean
        public ParallelTestCaseExecutorService parallelTestCaseExecutorService() {
            return mock(ParallelTestCaseExecutorService.class);
        }

        @Bean
        public TestCaseEventDispatchService testCaseEventDispatchService() {
            return mock(TestCaseEventDispatchService.class);
        }

        @Bean
        public SlangSourceService slangSourceService() {
            return mock(SlangSourceService.class);
        }

        @Bean
        public MetadataExtractor metadataExtractor() {
            return mock(MetadataExtractor.class);
        }

        @Bean
        public StaticValidator staticValidator() {
            return spy(StaticValidatorImpl.class);
        }
    }
}