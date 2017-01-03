/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build;

import com.google.common.collect.Maps;
import io.cloudslang.lang.api.Slang;
import io.cloudslang.lang.commons.services.api.SlangCompilationService;
import io.cloudslang.lang.commons.services.api.SlangSourceService;
import io.cloudslang.lang.commons.services.impl.SlangCompilationServiceImpl;
import io.cloudslang.lang.compiler.MetadataExtractor;
import io.cloudslang.lang.compiler.SlangCompiler;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.modeller.DependenciesHelper;
import io.cloudslang.lang.compiler.modeller.TransformersHandler;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.compiler.modeller.model.Metadata;
import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import io.cloudslang.lang.compiler.modeller.result.MetadataModellingResult;
import io.cloudslang.lang.compiler.modeller.transformers.PublishTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.ResultsTransformer;
import io.cloudslang.lang.compiler.scorecompiler.ScoreCompiler;
import io.cloudslang.lang.compiler.validator.ExecutableValidator;
import io.cloudslang.lang.compiler.validator.ExecutableValidatorImpl;
import io.cloudslang.lang.compiler.validator.PreCompileValidator;
import io.cloudslang.lang.compiler.validator.PreCompileValidatorImpl;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidator;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidatorImpl;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.logging.LoggingService;
import io.cloudslang.lang.logging.LoggingServiceImpl;
import io.cloudslang.lang.tools.build.SlangBuildMain.BulkRunMode;
import io.cloudslang.lang.tools.build.tester.IRunTestResults;
import io.cloudslang.lang.tools.build.tester.RunTestsResults;
import io.cloudslang.lang.tools.build.tester.SlangTestRunner;
import io.cloudslang.lang.tools.build.tester.SlangTestRunner.TestCaseRunState;
import io.cloudslang.lang.tools.build.tester.TestRun;
import io.cloudslang.lang.tools.build.tester.parallel.report.LoggingSlangTestCaseEventListener;
import io.cloudslang.lang.tools.build.tester.parallel.report.ThreadSafeRunTestResults;
import io.cloudslang.lang.tools.build.tester.parallel.services.ParallelTestCaseExecutorService;
import io.cloudslang.lang.tools.build.tester.parallel.services.TestCaseEventDispatchService;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import io.cloudslang.lang.tools.build.tester.parse.TestCasesYamlParser;
import io.cloudslang.lang.tools.build.tester.runconfiguration.BuildModeConfig;
import io.cloudslang.lang.tools.build.tester.runconfiguration.TestRunInfoService;
import io.cloudslang.lang.tools.build.tester.runconfiguration.TestRunInfoServiceImpl;
import io.cloudslang.lang.tools.build.tester.runconfiguration.strategy.ConflictResolutionStrategy;
import io.cloudslang.lang.tools.build.tester.runconfiguration.strategy.DefaultResolutionStrategy;
import io.cloudslang.lang.tools.build.validation.StaticValidator;
import io.cloudslang.lang.tools.build.validation.StaticValidatorImpl;
import io.cloudslang.lang.tools.build.verifier.SlangContentVerifier;
import io.cloudslang.score.api.ExecutionPlan;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.collect.Lists.newArrayList;
import static io.cloudslang.lang.tools.build.SlangBuildMain.BulkRunMode.ALL_PARALLEL;
import static io.cloudslang.lang.tools.build.SlangBuildMain.BulkRunMode.ALL_SEQUENTIAL;
import static io.cloudslang.lang.tools.build.SlangBuildMain.BulkRunMode.POSSIBLY_MIXED;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.python.google.common.collect.Sets.newHashSet;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangBuilderTest.Config.class)
public class SlangBuilderTest {

    private static final Set<String> SYSTEM_PROPERTY_DEPENDENCIES = Collections.emptySet();
    private static final CompilationArtifact EMPTY_COMPILATION_ARTIFACT =
            new CompilationArtifact(
                    new ExecutionPlan(),
                    new HashMap<String, ExecutionPlan>(),
                    new ArrayList<Input>(),
                    new HashSet<String>()
            );
    private static final Flow EMPTY_EXECUTABLE = new Flow(null, null, null, "no_dependencies", "empty_flow",
            null, null, null, new HashSet<String>(), SYSTEM_PROPERTY_DEPENDENCIES);

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

    @Autowired
    private TestRunInfoService testRunInfoService;

    @Autowired
    private LoggingService loggingService;

    @Autowired
    private LoggingSlangTestCaseEventListener loggingSlangTestCaseEventListener;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private SlangBuildMain.BuildMode buildMode = SlangBuildMain.BuildMode.BASIC;
    private Set<String> changedFiles = new HashSet<>();
    private BuildModeConfig buildModeConfig = BuildModeConfig.createBasicBuildModeConfig();

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
        slangBuilder.buildSlangContent(null, null, null, null, false, false, ALL_SEQUENTIAL, buildMode, changedFiles);
    }

    @Test
    public void testEmptyDirPath() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("path");
        slangBuilder.buildSlangContent("", "content", null,
                null, false, false, ALL_SEQUENTIAL, buildMode, changedFiles);
    }

    @Test
    public void testParallelFlag() throws Exception {
        Path testPath = null;
        try {
            String projectPath = "aaa/bb/cc";
            final List<String> suites = newArrayList("suite1", "suite2");
            testPath = Files.createTempDirectory("testPath");
            String testPathString = testPath.toString();

            final ThreadSafeRunTestResults runTestsResults = new ThreadSafeRunTestResults();
            doNothing().when(slangTestRunner)
                    .runTestsParallel(eq(projectPath), anyMap(), anyMap(), any(ThreadSafeRunTestResults.class));
            doReturn(Maps.newHashMap()).when(slangTestRunner).createTestCases(anyString(), anySet());

            slangBuilder.runTests(Maps.<String, Executable>newHashMap(), projectPath,
                    testPathString, suites, ALL_PARALLEL, buildMode, changedFiles);
            verify(slangTestRunner).runTestsParallel(eq(projectPath), anyMap(), anyMap(), eq(runTestsResults));
            verify(slangTestRunner, never())
                    .runTestsSequential(anyString(), anyMap(), anyMap(), any(RunTestsResults.class));

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
        slangBuilder.buildSlangContent("c/h/j", "c/h/j/content", null,
                null, false, false, ALL_SEQUENTIAL, buildMode, changedFiles);
    }

    @Test
    public void testPreCompileIllegalSlangFile() throws Exception {
        URI resource = getClass().getResource("/no_dependencies").toURI();
        when(slangCompiler.preCompile(any(SlangSource.class))).thenThrow(new RuntimeException());
        exception.expect(RuntimeException.class);
        SlangBuildResults slangBuildResults = slangBuilder.buildSlangContent(resource.getPath(), resource.getPath(),
                null, null, false, false, ALL_SEQUENTIAL, buildMode, changedFiles);
        assertNotNull(slangBuildResults.getCompilationExceptions());
        assertTrue(slangBuildResults.getCompilationExceptions().size() > 0);
        throw slangBuildResults.getCompilationExceptions().get(0);
    }

    @Test
    public void testNotAllSlangFilesWerePreCompiled() throws Exception {
        final URI resource = getClass().getResource("/no_dependencies").toURI();
        when(slangCompiler.preCompileSource(any(SlangSource.class)))
                .thenReturn(new ExecutableModellingResult(null, new ArrayList<RuntimeException>()));
        when(metadataExtractor.extractMetadataModellingResult(any(SlangSource.class), eq(false)))
                .thenReturn(new MetadataModellingResult(EMPTY_METADATA, new ArrayList<RuntimeException>()));
        exception.expect(RuntimeException.class);
        exception.expectMessage("1");
        exception.expectMessage("0");
        exception.expectMessage("compiled");
        SlangBuildResults slangBuildResults = slangBuilder.buildSlangContent(resource.getPath(), resource.getPath(),
                null, null, false, false, ALL_SEQUENTIAL, buildMode, changedFiles);
        assertNotNull(slangBuildResults.getCompilationExceptions());
        assertTrue(slangBuildResults.getCompilationExceptions().size() > 0);
        throw slangBuildResults.getCompilationExceptions().get(0);
    }

    @Test
    public void testCompileValidSlangFileNoDependencies() throws Exception {
        final URI resource = getClass().getResource("/no_dependencies").toURI();
        when(slangCompiler.preCompileSource(any(SlangSource.class)))
                .thenReturn(new ExecutableModellingResult(EMPTY_EXECUTABLE, new ArrayList<RuntimeException>()));
        when(metadataExtractor.extractMetadataModellingResult(any(SlangSource.class), eq(false)))
                .thenReturn(new MetadataModellingResult(null, new ArrayList<RuntimeException>()));
        when(scoreCompiler.compile(EMPTY_EXECUTABLE, new HashSet<Executable>()))
                .thenReturn(EMPTY_COMPILATION_ARTIFACT);

        SlangBuildResults buildResults = slangBuilder.buildSlangContent(resource.getPath(), resource.getPath(),
                null, null, false, false, ALL_SEQUENTIAL, buildMode, changedFiles);
        int numberOfCompiledSlangFiles = buildResults.getNumberOfCompiledSources();
        assertEquals("Did not compile all Slang files. Expected to compile: 1, but compiled: " +
                numberOfCompiledSlangFiles, numberOfCompiledSlangFiles, 1);
    }

    @Test
    public void testCompileInvalidSlangFile() throws Exception {
        final URI resource = getClass().getResource("/no_dependencies").toURI();
        when(slangCompiler.preCompileSource(any(SlangSource.class)))
                .thenReturn(new ExecutableModellingResult(EMPTY_EXECUTABLE, new ArrayList<RuntimeException>()));
        when(metadataExtractor.extractMetadataModellingResult(any(SlangSource.class), eq(false)))
                .thenReturn(new MetadataModellingResult(null, new ArrayList<RuntimeException>()));
        when(scoreCompiler.compile(EMPTY_EXECUTABLE, new HashSet<Executable>())).thenThrow(new RuntimeException());
        exception.expect(RuntimeException.class);
        SlangBuildResults results = slangBuilder.buildSlangContent(resource.getPath(), resource.getPath(),
                null, null, false, false,
                ALL_SEQUENTIAL, buildMode, changedFiles);

        throw results.getCompilationExceptions().get(0);
    }

    @Test
    public void testNotAllSlangFilesWereCompiled() throws Exception {
        final URI resource = getClass().getResource("/no_dependencies").toURI();
        when(slangCompiler.preCompileSource(any(SlangSource.class)))
                .thenReturn(new ExecutableModellingResult(EMPTY_EXECUTABLE, new ArrayList<RuntimeException>()));
        when(metadataExtractor.extractMetadataModellingResult(any(SlangSource.class), eq(false)))
                .thenReturn(new MetadataModellingResult(null, new ArrayList<RuntimeException>()));
        when(scoreCompiler.compile(EMPTY_EXECUTABLE, new HashSet<Executable>())).thenReturn(null);
        exception.expect(RuntimeException.class);
        exception.expectMessage("1");
        exception.expectMessage("0");
        exception.expectMessage("compile");
        exception.expectMessage("models");
        SlangBuildResults results = slangBuilder.buildSlangContent(resource.getPath(), resource.getPath(),
                null, null, false, false,
                ALL_SEQUENTIAL, buildMode, changedFiles);

        throw results.getCompilationExceptions().get(0);
    }

    @Test
    public void testCompileValidSlangFileWithMissingDependencies() throws Exception {
        final URI resource = getClass().getResource("/no_dependencies").toURI();
        Set<String> flowDependencies = new HashSet<>();
        flowDependencies.add("dep1");
        Flow newExecutable = new Flow(null, null, null, "no_dependencies", "empty_flow", null, null, null,
                flowDependencies, SYSTEM_PROPERTY_DEPENDENCIES);
        when(slangCompiler.preCompileSource(any(SlangSource.class)))
                .thenReturn(new ExecutableModellingResult(newExecutable, new ArrayList<RuntimeException>()));
        when(scoreCompiler.compile(newExecutable, new HashSet<Executable>()))
                .thenReturn(EMPTY_COMPILATION_ARTIFACT);
        when(metadataExtractor.extractMetadataModellingResult(any(SlangSource.class), eq(false)))
                .thenReturn(new MetadataModellingResult(EMPTY_METADATA, new ArrayList<RuntimeException>()));
        doCallRealMethod().when(staticValidator)
                .validateSlangFile(any(File.class), eq(newExecutable), eq(EMPTY_METADATA),
                        eq(false), any(Queue.class));
        exception.expect(RuntimeException.class);
        exception.expectMessage("dependency");
        exception.expectMessage("dep1");
        SlangBuildResults results = slangBuilder.buildSlangContent(resource.getPath(),
                resource.getPath(), null, null, false, false,
                ALL_SEQUENTIAL, buildMode, changedFiles);

        throw results.getCompilationExceptions().get(0);
    }

    @Test
    public void testCompileValidSlangFileWithDependencies() throws Exception {
        final URI resource = getClass().getResource("/dependencies").toURI();
        final URI emptyFlowUri = getClass().getResource("/dependencies/empty_flow.sl").toURI();
        final URI dependencyUri = getClass().getResource("/dependencies/dependency.sl").toURI();
        SlangSource emptyFlowSource = SlangSource.fromFile(emptyFlowUri);
        SlangSource dependencySource = SlangSource.fromFile(dependencyUri);

        Set<String> flowDependencies = new HashSet<>();
        flowDependencies.add("dependencies.dependency");
        Flow emptyFlowExecutable = new Flow(null, null, null, "dependencies", "empty_flow", null, null, null,
                flowDependencies, SYSTEM_PROPERTY_DEPENDENCIES);
        when(slangCompiler.preCompileSource(emptyFlowSource))
                .thenReturn(new ExecutableModellingResult(emptyFlowExecutable, new ArrayList<RuntimeException>()));
        Flow dependencyExecutable = new Flow(null, null, null, "dependencies", "dependency", null, null, null,
                new HashSet<String>(), SYSTEM_PROPERTY_DEPENDENCIES);
        when(slangCompiler.preCompileSource(dependencySource))
                .thenReturn(new ExecutableModellingResult(dependencyExecutable, new ArrayList<RuntimeException>()));
        HashSet<Executable> dependencies = new HashSet<>();
        dependencies.add(dependencyExecutable);
        when(scoreCompiler.compile(emptyFlowExecutable, dependencies))
                .thenReturn(EMPTY_COMPILATION_ARTIFACT);
        when(scoreCompiler.compile(dependencyExecutable, new HashSet<Executable>()))
                .thenReturn(EMPTY_COMPILATION_ARTIFACT);
        when(metadataExtractor.extractMetadataModellingResult(any(SlangSource.class), eq(false)))
                .thenReturn(new MetadataModellingResult(EMPTY_METADATA, new ArrayList<RuntimeException>()));
        SlangBuildResults buildResults = slangBuilder.buildSlangContent(resource.getPath(), resource.getPath(),
                null, null, false, false, ALL_SEQUENTIAL, buildMode, changedFiles);
        int numberOfCompiledSlangFiles = buildResults.getNumberOfCompiledSources();
        // properties file should be ignored
        assertEquals("Did not compile all Slang files. Expected to compile: 2, but compiled: " +
                numberOfCompiledSlangFiles, numberOfCompiledSlangFiles, 2);
    }

    @Test
    public void testInvalidNamespaceFlow() throws Exception {
        final URI resource = getClass().getResource("/no_dependencies").toURI();
        final Flow newExecutable = new Flow(null, null, null, "wrong.namespace", "empty_flow", null, null, null,
                new HashSet<String>(), SYSTEM_PROPERTY_DEPENDENCIES);
        when(slangCompiler.preCompileSource(any(SlangSource.class)))
                .thenReturn(new ExecutableModellingResult(newExecutable, new ArrayList<RuntimeException>()));
        when(metadataExtractor.extractMetadataModellingResult(any(SlangSource.class), eq(false)))
                .thenReturn(new MetadataModellingResult(EMPTY_METADATA, new ArrayList<RuntimeException>()));
        doCallRealMethod().when(staticValidator)
                .validateSlangFile(any(File.class), eq(newExecutable), eq(EMPTY_METADATA),
                        eq(false), any(Queue.class));
        exception.expect(RuntimeException.class);
        exception.expectMessage("Namespace");
        exception.expectMessage("wrong.namespace");
        SlangBuildResults slangBuildResults = slangBuilder.buildSlangContent(resource.getPath(), resource.getPath(),
                null, null, false, false, ALL_SEQUENTIAL, buildMode, changedFiles);
        assertNotNull(slangBuildResults.getCompilationExceptions());
        assertTrue(slangBuildResults.getCompilationExceptions().size() > 0);
        throw slangBuildResults.getCompilationExceptions().get(0);
    }

    @Test
    public void testInvalidFlowName() throws Exception {
        final URI resource = getClass().getResource("/no_dependencies").toURI();
        final Flow newExecutable = new Flow(null, null, null, "no_dependencies", "wrong_name", null, null, null,
                new HashSet<String>(), SYSTEM_PROPERTY_DEPENDENCIES);
        when(slangCompiler.preCompileSource(any(SlangSource.class)))
                .thenReturn(new ExecutableModellingResult(newExecutable, new ArrayList<RuntimeException>()));
        when(metadataExtractor.extractMetadataModellingResult(any(SlangSource.class), eq(false)))
                .thenReturn(new MetadataModellingResult(EMPTY_METADATA, new ArrayList<RuntimeException>()));
        doCallRealMethod().when(staticValidator)
                .validateSlangFile(any(File.class), eq(newExecutable),
                        eq(EMPTY_METADATA), eq(false), any(Queue.class));

        exception.expect(RuntimeException.class);
        exception.expectMessage("Name");
        exception.expectMessage("wrong_name");

        SlangBuildResults slangBuildResults = slangBuilder.buildSlangContent(resource.getPath(),
                resource.getPath(), null, null, false, false, ALL_SEQUENTIAL, buildMode, changedFiles);
        assertNotNull(slangBuildResults.getCompilationExceptions());
        assertTrue(slangBuildResults.getCompilationExceptions().size() > 0);

        throw slangBuildResults.getCompilationExceptions().get(0);
    }

    @Test
    public void testValidFlowNamespaceWithAllValidCharsTypes() throws Exception {
        final URI resource = getClass().getResource("/no_dependencies-0123456789").toURI();
        final Flow executable = new Flow(null, null, null, "no_dependencies-0123456789", "empty_flow",
                null, null, null, new HashSet<String>(), SYSTEM_PROPERTY_DEPENDENCIES);
        when(slangCompiler.preCompileSource(any(SlangSource.class)))
                .thenReturn(new ExecutableModellingResult(executable, new ArrayList<RuntimeException>()));
        when(scoreCompiler.compile(executable, new HashSet<Executable>()))
                .thenReturn(EMPTY_COMPILATION_ARTIFACT);
        SlangBuildResults buildResults = slangBuilder.buildSlangContent(resource.getPath(), resource.getPath(),
                null, null, false, false, ALL_SEQUENTIAL, buildMode, changedFiles);
        int numberOfCompiledSlangFiles = buildResults.getNumberOfCompiledSources();
        assertEquals("Did not compile all Slang files. Expected to compile: 1, but compiled: " +
                numberOfCompiledSlangFiles, numberOfCompiledSlangFiles, 1);
    }

    @Test
    public void testValidFlowNamespaceCaseInsensitive() throws Exception {
        final URI resource = getClass().getResource("/no_dependencies").toURI();
        final Flow executable = new Flow(null, null, null, "No_Dependencies", "empty_flow", null, null, null,
                new HashSet<String>(), SYSTEM_PROPERTY_DEPENDENCIES);
        when(slangCompiler.preCompileSource(any(SlangSource.class)))
                .thenReturn(new ExecutableModellingResult(executable, new ArrayList<RuntimeException>()));
        when(scoreCompiler.compile(executable, new HashSet<Executable>()))
                .thenReturn(EMPTY_COMPILATION_ARTIFACT);
        when(metadataExtractor.extractMetadataModellingResult(any(SlangSource.class), eq(false)))
                .thenReturn(new MetadataModellingResult(EMPTY_METADATA, new ArrayList<RuntimeException>()));
        SlangBuildResults buildResults = slangBuilder.buildSlangContent(resource.getPath(),
                resource.getPath(), null, null, false, false, ALL_SEQUENTIAL, buildMode, changedFiles);
        int numberOfCompiledSlangFiles = buildResults.getNumberOfCompiledSources();
        assertEquals("Did not compile all Slang files. Expected to compile: 1, but compiled: " +
                numberOfCompiledSlangFiles, 1, numberOfCompiledSlangFiles);
    }

    @Test
    public void testNamespaceWithInvalidCharsFlow() throws Exception {
        final URI resource = getClass().getResource("/invalid-chars$").toURI();
        final Flow newExecutable = new Flow(null, null, null, "invalid-chars$", "empty_flow", null, null,
                null, new HashSet<String>(), SYSTEM_PROPERTY_DEPENDENCIES);
        when(slangCompiler.preCompileSource(any(SlangSource.class)))
                .thenReturn(new ExecutableModellingResult(newExecutable, new ArrayList<RuntimeException>()));
        when(metadataExtractor.extractMetadataModellingResult(any(SlangSource.class), eq(false)))
                .thenReturn(new MetadataModellingResult(EMPTY_METADATA, new ArrayList<RuntimeException>()));
        doCallRealMethod().when(staticValidator)
                .validateSlangFile(any(File.class), eq(newExecutable), eq(EMPTY_METADATA),
                        eq(false), any(Queue.class));
        exception.expect(RuntimeException.class);
        exception.expectMessage("invalid-chars$");
        exception.expectMessage("alphanumeric");
        SlangBuildResults slangBuildResults = slangBuilder.buildSlangContent(resource.getPath(),
                resource.getPath(), null, null, false, false, ALL_SEQUENTIAL, buildMode, changedFiles);
        assertNotNull(slangBuildResults.getCompilationExceptions());
        assertTrue(slangBuildResults.getCompilationExceptions().size() > 0);
        throw slangBuildResults.getCompilationExceptions().get(0);
    }

    @Test
    public void testCompileSlangFileAndRunTests() throws Exception {
        final URI contentResource = getClass().getResource("/no_dependencies").toURI();
        final URI testResource = getClass().getResource("/test/valid").toURI();
        when(slangCompiler.preCompileSource(any(SlangSource.class)))
                .thenReturn(new ExecutableModellingResult(EMPTY_EXECUTABLE, new ArrayList<RuntimeException>()));
        when(metadataExtractor.extractMetadataModellingResult(any(SlangSource.class), eq(false)))
                .thenReturn(new MetadataModellingResult(null, new ArrayList<RuntimeException>()));
        when(scoreCompiler.compile(EMPTY_EXECUTABLE, new HashSet<Executable>()))
                .thenReturn(EMPTY_COMPILATION_ARTIFACT);

        doAnswer(new Answer() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] arguments = invocationOnMock.getArguments();
                RunTestsResults runTestsResultsInner = (RunTestsResults) arguments[arguments.length - 1];
                runTestsResultsInner.addFailedTest("test1", new TestRun(new SlangTestCase("test1", "", null, null,
                        null, null, null, null, null), "message"));
                return null;
            }
        }).when(slangTestRunner)
                .runTestsSequential((any(String.class)), anyMap(), anyMap(), any(RunTestsResults.class));
        SlangBuildResults buildResults = slangBuilder
                .buildSlangContent(contentResource.getPath(), contentResource.getPath(), testResource.getPath(),
                        null, false, false, ALL_SEQUENTIAL, buildMode, changedFiles);
        int numberOfCompiledSlangFiles = buildResults.getNumberOfCompiledSources();
        IRunTestResults actualRunTestsResults = buildResults.getRunTestsResults();
        assertEquals("Did not compile all Slang files. Expected to compile: 1, but compiled: " +
                numberOfCompiledSlangFiles, numberOfCompiledSlangFiles, 1);
        assertEquals("1 test case should fail", 1, actualRunTestsResults.getFailedTests().size());
    }

    @Test
    public void testTestCaseWithIncorrectTestFlowReference() throws Exception {
        final URI contentResource = getClass().getResource("/no_dependencies").toURI();
        final URI testResource = getClass().getResource("/test/valid").toURI();
        when(slangCompiler.preCompileSource(any(SlangSource.class)))
                .thenReturn(new ExecutableModellingResult(EMPTY_EXECUTABLE, new ArrayList<RuntimeException>()));
        when(metadataExtractor.extractMetadataModellingResult(any(SlangSource.class), eq(false)))
                .thenReturn(new MetadataModellingResult(null, new ArrayList<RuntimeException>()));
        when(scoreCompiler.compile(EMPTY_EXECUTABLE, new HashSet<Executable>()))
                .thenReturn(EMPTY_COMPILATION_ARTIFACT);

        doNothing().when(slangTestRunner).runTestsSequential(
                any(String.class),
                anyMapOf(String.class, SlangTestCase.class),
                anyMapOf(String.class, CompilationArtifact.class),
                any(ThreadSafeRunTestResults.class));

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

        SlangBuildResults buildResults = slangBuilder
                .buildSlangContent(contentResource.getPath(), contentResource.getPath(), testResource.getPath(),
                        null, false, false, ALL_SEQUENTIAL, buildMode, changedFiles);

        // test case: test flow path points to non existing executable
        // validate execution does not return when detects this situation and coverage data is added to results
        assertEquals(1, buildResults.getRunTestsResults().getUncoveredExecutables().size());
    }

    @Test
    public void testProcessRunTestsParallel() {
        final Map<String, SlangTestCase> testCases = new LinkedHashMap<>();
        final SlangTestCase testCase1 = new SlangTestCase("test1", "testFlowPath", "desc",
                asList("abc", "new"), "mock", null, null, false, "SUCCESS");
        final SlangTestCase testCase2 = new SlangTestCase("test2", "testFlowPath", "desc",
                asList("efg", "new"), "mock", null, null, false, "SUCCESS");
        final SlangTestCase testCase3 = new SlangTestCase("test3", "testFlowPath", "desc",
                asList("new", "new2"), "mock", null, null, false, "SUCCESS");
        final SlangTestCase testCase4 = new SlangTestCase("test4", "testFlowPath", "desc",
                asList("jjj", "new2"), "mock", null, null, false, "SUCCESS");
        final SlangTestCase testCase5 = new SlangTestCase("test5", "testFlowPath", "desc",
                asList("hhh", "jjj", "abc"), "mock", null, null, false, "SUCCESS");

        testCases.put("test1", testCase1);
        testCases.put("test2", testCase2);
        testCases.put("test3", testCase3);
        testCases.put("test4", testCase4);
        testCases.put("test5", testCase5);

        final List<String> testSuites = newArrayList("abc");
        final Map<String, CompilationArtifact> compiledFlows = new HashMap<>();
        final String projectPath = "aaa";

        final AtomicReference<IRunTestResults> capturedArgument = new AtomicReference<>();
        doAnswer(getAnswer(capturedArgument)).when(slangTestRunner).splitTestCasesByRunState(any(BulkRunMode.class),
                anyMap(), anyList(), any(IRunTestResults.class), any(BuildModeConfig.class));
        doNothing().when(slangTestRunner)
                .runTestsParallel(anyString(), anyMap(), anyMap(), any(ThreadSafeRunTestResults.class));

        // Tested call
        slangBuilder.processRunTests(projectPath, testSuites, ALL_PARALLEL, compiledFlows, testCases, buildModeConfig);

        InOrder inOrder = Mockito.inOrder(slangTestRunner);
        inOrder.verify(slangTestRunner).splitTestCasesByRunState(eq(ALL_PARALLEL), eq(testCases), eq(testSuites),
                isA(ThreadSafeRunTestResults.class), any(BuildModeConfig.class));
        inOrder.verify(slangTestRunner).runTestsParallel(eq(projectPath), anyMap(), eq(compiledFlows),
                eq((ThreadSafeRunTestResults) capturedArgument.get()));
        verifyNoMoreInteractions(slangTestRunner);
        verify(slangTestRunner, never())
                .runTestsSequential(anyString(), anyMap(), anyMap(), any(IRunTestResults.class));
    }

    @Test
    public void testProcessRunTestsSequential() {
        final Map<String, SlangTestCase> testCases = new LinkedHashMap<>();
        final SlangTestCase testCase1 = new SlangTestCase("test1", "testFlowPath", "desc",
                asList("abc", "new"), "mock", null, null, false, "SUCCESS");
        final SlangTestCase testCase2 = new SlangTestCase("test2", "testFlowPath", "desc",
                asList("efg", "new"), "mock", null, null, false, "SUCCESS");
        final SlangTestCase testCase3 = new SlangTestCase("test3", "testFlowPath", "desc",
                asList("new", "new2"), "mock", null, null, false, "SUCCESS");

        testCases.put("test1", testCase1);
        testCases.put("test2", testCase2);
        testCases.put("test3", testCase3);

        final List<String> testSuites = newArrayList("abc");
        final Map<String, CompilationArtifact> compiledFlows = new HashMap<>();
        final String projectPath = "aaa";

        final AtomicReference<IRunTestResults> theCapturedArgument = new AtomicReference<>();
        doAnswer(getAnswer(theCapturedArgument)).when(slangTestRunner).splitTestCasesByRunState(any(BulkRunMode.class),
                anyMap(), anyList(), any(IRunTestResults.class), any(BuildModeConfig.class));
        doNothing().when(slangTestRunner).runTestsSequential(anyString(),
                anyMap(), anyMap(), any(IRunTestResults.class));

        BuildModeConfig basic = BuildModeConfig.createBasicBuildModeConfig();
        // Tested call
        slangBuilder.processRunTests(projectPath, testSuites, ALL_SEQUENTIAL, compiledFlows, testCases, basic);

        InOrder inOrder = Mockito.inOrder(slangTestRunner);
        inOrder.verify(slangTestRunner).splitTestCasesByRunState(eq(ALL_SEQUENTIAL),
                eq(testCases), eq(testSuites), isA(RunTestsResults.class), eq(basic));
        inOrder.verify(slangTestRunner).runTestsSequential(eq(projectPath), anyMap(),
                eq(compiledFlows), eq((RunTestsResults) theCapturedArgument.get()));
        inOrder.verify(slangTestRunner, never()).runTestsParallel(anyString(), anyMap(),
                anyMap(), any(ThreadSafeRunTestResults.class));
        verifyNoMoreInteractions(slangTestRunner);
    }

    @Test
    public void testProcessRunTestsMixed() {
        final Map<String, SlangTestCase> testCases = new LinkedHashMap<>();
        final SlangTestCase testCase1 = new SlangTestCase("test1", "testFlowPath", "desc",
                asList("abc", "new"), "mock", null, null, false, "SUCCESS");
        final SlangTestCase testCase2 = new SlangTestCase("test2", "testFlowPath", "desc",
                asList("efg", "new"), "mock", null, null, false, "SUCCESS");
        final SlangTestCase testCase3 = new SlangTestCase("test3", "testFlowPath", "desc",
                asList("new", "new2"), "mock", null, null, false, "SUCCESS");
        final SlangTestCase testCase4 = new SlangTestCase("test4", "testFlowPath", "desc",
                asList("new", "new2"), "mock", null, null, false, "SUCCESS");

        testCases.put("test1", testCase1);
        testCases.put("test2", testCase2);
        testCases.put("test3", testCase3);
        testCases.put("test4", testCase4);

        final List<String> testSuites = newArrayList("new");
        final Map<String, CompilationArtifact> compiledFlows = new HashMap<>();
        final String projectPath = "aaa";

        final AtomicReference<ThreadSafeRunTestResults> theCapturedArgument = new AtomicReference<>();
        final AtomicReference<Map<String, SlangTestCase>> capturedTestsSeq = new AtomicReference<>();
        final AtomicReference<Map<String, SlangTestCase>> capturedTestsPar = new AtomicReference<>();

        doCallRealMethod().when(slangTestRunner).isTestCaseInActiveSuite(any(SlangTestCase.class), anyList());
        doReturn(SlangBuildMain.TestCaseRunMode.SEQUENTIAL)
                .doReturn(SlangBuildMain.TestCaseRunMode.PARALLEL)
                .doReturn(SlangBuildMain.TestCaseRunMode.PARALLEL)
                .doReturn(SlangBuildMain.TestCaseRunMode.SEQUENTIAL)
                .when(testRunInfoService).getRunModeForTestCase(any(SlangTestCase.class),
                any(ConflictResolutionStrategy.class), any(DefaultResolutionStrategy.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] arguments = invocationOnMock.getArguments();
                Object argument = arguments[arguments.length - 2];
                theCapturedArgument.set((ThreadSafeRunTestResults) argument);

                return invocationOnMock.callRealMethod();
            }
        }).when(slangTestRunner).splitTestCasesByRunState(any(BulkRunMode.class), anyMap(), anyList(),
                any(IRunTestResults.class), eq(buildModeConfig));

        doAnswer(new Answer() {
            @Override
            @SuppressWarnings("unchecked")
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] arguments = invocationOnMock.getArguments();
                capturedTestsSeq.set((Map<String, SlangTestCase>) arguments[1]);

                return null;
            }
        }).when(slangTestRunner).runTestsSequential(anyString(), anyMap(), anyMap(), any(IRunTestResults.class));
        doAnswer(new Answer() {
            @Override
            @SuppressWarnings("unchecked")
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] arguments = invocationOnMock.getArguments();
                capturedTestsPar.set((Map<String, SlangTestCase>) arguments[1]);

                return null;
            }
        }).when(slangTestRunner).runTestsParallel(anyString(), anyMap(), anyMap(),
                any(ThreadSafeRunTestResults.class));

        // Tested call
        slangBuilder.processRunTests(projectPath, testSuites, POSSIBLY_MIXED,
                compiledFlows, testCases, buildModeConfig);

        InOrder inOrder = inOrder(slangTestRunner);
        inOrder.verify(slangTestRunner).splitTestCasesByRunState(eq(POSSIBLY_MIXED),
                eq(testCases), eq(testSuites), isA(ThreadSafeRunTestResults.class), eq(buildModeConfig));
        inOrder.verify(slangTestRunner).runTestsSequential(eq(projectPath), anyMap(),
                eq(compiledFlows), eq(theCapturedArgument.get()));
        inOrder.verify(slangTestRunner).runTestsParallel(eq(projectPath), anyMap(),
                eq(compiledFlows), eq(theCapturedArgument.get()));

        final List<SlangTestCase> listSeq = newArrayList(capturedTestsSeq.get().values());
        final List<SlangTestCase> listPar = newArrayList(capturedTestsPar.get().values());
        assertEquals(0, ListUtils.intersection(listSeq, listPar).size()); // assures that a test is run only once
        assertEquals(newHashSet(testCases.values()), newHashSet(ListUtils.union(listSeq, listPar)));
    }

    private Answer getAnswer(final AtomicReference<IRunTestResults> theCapturedArgument) {
        return new Answer() {
            @Override
            public Map<TestCaseRunState, Map<String, SlangTestCase>> answer(InvocationOnMock invocationOnMock)
                    throws Throwable {
                Object[] arguments = invocationOnMock.getArguments();
                Object argument = arguments[arguments.length - 2];
                if (argument instanceof ThreadSafeRunTestResults) {
                    theCapturedArgument.set((ThreadSafeRunTestResults) argument);
                } else if (argument instanceof RunTestsResults) {
                    theCapturedArgument.set((RunTestsResults) argument);
                }

                return new LinkedHashMap<>();
            }
        };
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

        @Bean
        public TestRunInfoService test() {
            return spy(TestRunInfoServiceImpl.class);
        }

        @Bean
        public LoggingService loggingService() {
            return new LoggingServiceImpl();
        }

        @Bean
        public SlangCompilationService slangCompilationService() {
            return new SlangCompilationServiceImpl();
        }

        @Bean
        public LoggingSlangTestCaseEventListener loggingSlangTestCaseEventListener() {
            return new LoggingSlangTestCaseEventListener();
        }

        ////////////////////// Context for DependenciesHelper ////////////////////////////
        @Bean
        public DependenciesHelper dependenciesHelper() {
            return mock(DependenciesHelper.class);
        }

        @Bean
        public PublishTransformer publishTransformer() {
            return mock(PublishTransformer.class);
        }

        @Bean
        public TransformersHandler transformersHandler() {
            return mock(TransformersHandler.class);
        }

        @Bean
        public PreCompileValidator preCompileValidator() {
            return new PreCompileValidatorImpl();
        }

        @Bean
        public ResultsTransformer resultsTransformer() {
            return mock(ResultsTransformer.class);
        }

        @Bean
        public ExecutableValidator executableValidator() {
            return new ExecutableValidatorImpl();
        }

        @Bean
        public SystemPropertyValidator systemPropertyValidator() {
            return new SystemPropertyValidatorImpl();
        }

    }
}
