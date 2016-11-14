/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.api;

import com.google.common.collect.Sets;
import io.cloudslang.lang.compiler.MetadataExtractor;
import io.cloudslang.lang.compiler.PrecompileStrategy;
import io.cloudslang.lang.compiler.SlangCompiler;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.modeller.model.Metadata;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.api.Score;
import io.cloudslang.score.api.TriggeringProperties;
import io.cloudslang.score.events.EventBus;
import io.cloudslang.score.events.EventConstants;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.events.ScoreEventListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.eq;

/**
 * User: stoneo
 * Date: 04/12/2014
 * Time: 11:00
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangImplTest.Config.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SlangImplTest {

    static final CompilationArtifact emptyCompilationArtifact = new CompilationArtifact(new ExecutionPlan(),
            new HashMap<String, ExecutionPlan>(), new ArrayList<Input>(), new HashSet<String>());
    private static final int ALL_EVENTS_SIZE = 24;

    @Autowired
    private Slang slang;

    @Autowired
    private SlangCompiler compiler;

    @Autowired
    private MetadataExtractor metadataExtractor;

    @Autowired
    private Score score;

    @Autowired
    private EventBus eventBus;

    @Before
    public void init() {
        Mockito.reset(score, compiler);
    }

    @Test
    public void testExtractMetadata() throws IOException {
        SlangSource tempFile = createTempFile();
        Mockito.when(metadataExtractor.extractMetadata(any(SlangSource.class))).thenReturn(new Metadata());
        Metadata metadata = slang.extractMetadata(tempFile);
        Assert.assertNotNull(metadata);
        Mockito.verify(metadataExtractor).extractMetadata(tempFile);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExtractMetadataNoFilePath() {
        slang.extractMetadata(null);
    }

    @Test
    public void testCompile() throws IOException {
        SlangSource tempFile = createTempFile();
        Mockito.when(
                compiler.compile(any(SlangSource.class),
                        anySetOf(SlangSource.class),
                        any(PrecompileStrategy.class))
        ).thenReturn(emptyCompilationArtifact);
        CompilationArtifact compilationArtifact = slang.compile(tempFile, new HashSet<SlangSource>());
        Assert.assertNotNull(compilationArtifact);
        Mockito.verify(compiler)
                .compile(eq(tempFile), eq(new HashSet<SlangSource>()), eq(PrecompileStrategy.WITHOUT_CACHE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCompileWithNoFilePath() {
        slang.compile(null, new HashSet<SlangSource>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCompileWithCorruptedFilePath() {
        slang.compile(SlangSource.fromFile(new File("")), new HashSet<SlangSource>());
    }

    @Test
    public void testCompileWithNullDependencies() throws IOException {
        SlangSource tempFile = createTempFile();
        Mockito.when(
                compiler.compile(any(SlangSource.class),
                        anySetOf(SlangSource.class),
                        any(PrecompileStrategy.class))
        ).thenReturn(emptyCompilationArtifact);
        CompilationArtifact compilationArtifact = slang.compile(tempFile, null);
        Assert.assertNotNull(compilationArtifact);
        Mockito.verify(compiler)
                .compile(eq(tempFile), eq(new HashSet<SlangSource>()), eq(PrecompileStrategy.WITHOUT_CACHE));
    }

    @Test
    public void testCompileWithDependencies() throws IOException {
        final SlangSource tempFile = createTempFile();
        File tempDependencyFile = File.createTempFile("tempDependency", null);
        tempDependencyFile.deleteOnExit();
        Set<SlangSource> dependencies = new HashSet<>();
        dependencies.add(SlangSource.fromFile(tempDependencyFile));
        Mockito.when(
                compiler.compile(any(SlangSource.class), anySetOf(SlangSource.class), any(PrecompileStrategy.class)))
                .thenReturn(emptyCompilationArtifact);
        CompilationArtifact compilationArtifact = slang.compile(tempFile, dependencies);
        Assert.assertNotNull(compilationArtifact);
        Set<SlangSource> dependencyFiles = new HashSet<>();
        dependencyFiles.add(SlangSource.fromFile(tempDependencyFile));
        Mockito.verify(compiler).compile(eq(tempFile), eq(dependencyFiles), eq(PrecompileStrategy.WITHOUT_CACHE));
    }

    @Test(expected = Exception.class)
    public void testCompileOperationWithException() throws IOException {
        Mockito.when(
                compiler.compile(any(SlangSource.class),
                        anySetOf(SlangSource.class),
                        any(PrecompileStrategy.class))
        ).thenThrow(Exception.class);
        SlangSource tempFile = createTempFile();
        slang.compile(tempFile, new HashSet<SlangSource>());
    }

    // tests for run() method

    @Test
    public void testRun() {
        SystemProperty expectedSystemProperty = new SystemProperty("docker.sys", "props.port", "22");
        Long executionId = slang.run(
                emptyCompilationArtifact,
                new HashMap<String, Value>(),
                Sets.newHashSet(expectedSystemProperty)
        );
        Assert.assertNotNull(executionId);

        ArgumentCaptor<TriggeringProperties> argumentCaptor = ArgumentCaptor.forClass(TriggeringProperties.class);
        Mockito.verify(score).trigger(argumentCaptor.capture());

        TriggeringProperties triggeringProperties = argumentCaptor.getValue();
        RunEnvironment runEnv = (RunEnvironment) triggeringProperties.getContext().get(ScoreLangConstants.RUN_ENV);
        Assert.assertNotNull(runEnv);
        Assert.assertTrue(triggeringProperties.getContext().containsKey(ScoreLangConstants.USER_INPUTS_KEY));
        Assert.assertTrue(runEnv.getSystemProperties().contains(expectedSystemProperty));
    }

    @Test
    public void testRunWithNullInputs() {
        Long executionId = slang.run(emptyCompilationArtifact, null, new HashSet<SystemProperty>());
        Assert.assertNotNull(executionId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRunWithNullCompilationArtifact() {
        slang.run(null, new HashMap<String, Value>(), null);
    }

    // tests for compileAndRun() method

    @Test
    public void testLaunchOperation() throws IOException {
        Slang mockSlang = Mockito.mock(SlangImpl.class);
        SlangSource tempFile = createTempFile();
        Mockito.when(mockSlang.compile(tempFile, new HashSet<SlangSource>())).thenReturn(emptyCompilationArtifact);
        Mockito.when(mockSlang.compileAndRun(any(SlangSource.class),
                anySetOf(SlangSource.class), anyMapOf(String.class, Value.class), anySetOf(SystemProperty.class)))
                .thenCallRealMethod();
        Long id = mockSlang
                .compileAndRun(tempFile, new HashSet<SlangSource>(),
                        new HashMap<String, Value>(), new HashSet<SystemProperty>());
        Assert.assertNotNull(id);
        Mockito.verify(mockSlang).run(emptyCompilationArtifact,
                new HashMap<String, Value>(), new HashSet<SystemProperty>());
    }

    @Test
    public void testLaunch() throws IOException {
        Slang mockSlang = Mockito.mock(SlangImpl.class);
        SlangSource tempFile = createTempFile();

        Mockito.when(mockSlang.compileAndRun(any(SlangSource.class), anySetOf(SlangSource.class),
                anyMapOf(String.class, Value.class), anySetOf(SystemProperty.class))).thenCallRealMethod();
        Long id = mockSlang
                .compileAndRun(tempFile, new HashSet<SlangSource>(),
                        new HashMap<String, Value>(), new HashSet<SystemProperty>());
        Assert.assertNotNull(id);
        Mockito.verify(mockSlang)
                .compileAndRun(tempFile, new HashSet<SlangSource>(),
                        new HashMap<String, Value>(), new HashSet<SystemProperty>());
    }

    // tests for subscribeOnEvents() method

    @Test
    public void testSubscribeOnEventsWithListener() {
        ScoreEventListener eventListener = new EventListener();
        Set<String> eventTypes = new HashSet<>();
        eventTypes.add(EventConstants.SCORE_ERROR_EVENT);
        slang.subscribeOnEvents(eventListener, eventTypes);
        Mockito.verify(eventBus).subscribe(eventListener, eventTypes);
    }

    @Test
    public void testUnSubscribeOnEvents() {
        ScoreEventListener eventListener = new EventListener();
        slang.unSubscribeOnEvents(eventListener);
        Mockito.verify(eventBus).unsubscribe(eventListener);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSubscribeOnAllEventsWithListener() {
        Slang mockSlang = Mockito.mock(SlangImpl.class);
        ScoreEventListener eventListener = new EventListener();
        Mockito.doCallRealMethod().when(mockSlang).subscribeOnAllEvents(any(ScoreEventListener.class));
        ArgumentCaptor<Set> eventsCaptor = ArgumentCaptor.forClass(Set.class);

        mockSlang.subscribeOnAllEvents(eventListener);

        Mockito.verify(mockSlang).subscribeOnEvents(eq(eventListener), eventsCaptor.capture());
        Set allEvents = eventsCaptor.getValue();

        Assert.assertNotNull(allEvents);
        Assert.assertEquals("Events size not as expected", ALL_EVENTS_SIZE, allEvents.size());
    }

    private static class EventListener implements ScoreEventListener {

        @Override
        public synchronized void onEvent(ScoreEvent event) throws InterruptedException {

        }
    }

    private SlangSource createTempFile() throws IOException {
        File tempFile = File.createTempFile("temp", null);
        tempFile.deleteOnExit();
        return SlangSource.fromFile(tempFile);
    }

    @Configuration
    static class Config {

        @Bean
        public SlangImpl slang() {
            return new SlangImpl();
        }

        @Bean
        public MetadataExtractor metadataExtractor() {
            return Mockito.mock(MetadataExtractor.class);
        }

        @Bean
        public SlangCompiler compiler() {
            SlangCompiler compiler = Mockito.mock(SlangCompiler.class);
            Mockito.when(compiler.compile(any(SlangSource.class), anySetOf(SlangSource.class)))
                    .thenReturn(emptyCompilationArtifact);
            return compiler;
        }

        @Bean
        public Score score() {
            return Mockito.mock(Score.class);
        }

        @Bean
        public EventBus eventBus() {
            return Mockito.mock(EventBus.class);
        }
    }

}
