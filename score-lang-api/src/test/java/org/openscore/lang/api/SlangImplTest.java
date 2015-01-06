package org.openscore.lang.api;
/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/


import ch.lambdaj.function.matcher.Predicate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openscore.api.ExecutionPlan;
import org.openscore.api.Score;
import org.openscore.api.TriggeringProperties;
import org.openscore.events.EventBus;
import org.openscore.events.EventConstants;
import org.openscore.events.ScoreEvent;
import org.openscore.events.ScoreEventListener;
import org.openscore.lang.compiler.SlangCompiler;
import org.openscore.lang.compiler.SlangSource;
import org.openscore.lang.entities.CompilationArtifact;
import org.openscore.lang.entities.ScoreLangConstants;
import org.openscore.lang.entities.bindings.Input;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: stoneo
 * Date: 04/12/2014
 * Time: 11:00
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangImplTest.Config.class)
public class SlangImplTest {

    @Autowired
    private Slang slang;

    @Autowired
    private SlangCompiler compiler;

    @Autowired
    private Score score;

    @Autowired
    private EventBus eventBus;
    private static final int ALL_EVENTS_SIZE = 16;

    private CompilationArtifact emptyCompilationArtifact = new CompilationArtifact(new ExecutionPlan(), new HashMap<String, ExecutionPlan>(), new ArrayList<Input>());

    @Before
    public void init(){
        Mockito.reset(score, compiler);
    }

    // Tests for the compileOperation() method

    @Test
    public void testCompileOperation() throws IOException {
        SlangSource tempFile = createTempFile();
        Mockito.when(compiler.compile(any(SlangSource.class), anyString(), anySetOf(SlangSource.class))).thenReturn(emptyCompilationArtifact);
        CompilationArtifact compilationArtifact = slang.compileOperation(tempFile, "op", new HashSet<SlangSource>());
        Assert.assertNotNull(compilationArtifact);
        Mockito.verify(compiler).compile(tempFile, "op", new HashSet<SlangSource>());
   }

    @Test(expected = IllegalArgumentException.class)
    public void testCompileOperationWithNoFilePath(){
        slang.compileOperation(null, "op", new HashSet<SlangSource>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCompileOperationWithCorruptedFilePath(){
        slang.compileOperation(SlangSource.fromFile(new File("/")), "op", new HashSet<SlangSource>());
    }

    @Test
    public void testCompileOperationWithNullDependencies() throws IOException {
        SlangSource tempFile = createTempFile();
        Mockito.when(compiler.compile(any(SlangSource.class), anyString(), anySetOf(SlangSource.class))).thenReturn(emptyCompilationArtifact);
        CompilationArtifact compilationArtifact = slang.compileOperation(tempFile, "op", null);
        Assert.assertNotNull(compilationArtifact);
        Mockito.verify(compiler).compile(tempFile, "op", new HashSet<SlangSource>());
    }

    @Test
    public void testCompileOperationWithDependencies() throws IOException {
        SlangSource tempFile = createTempFile();
        File tempDependencyFile = File.createTempFile("tempDependency", null);
        tempDependencyFile.deleteOnExit();
        Set<SlangSource> dependencies = new HashSet<>();
        dependencies.add(SlangSource.fromFile(tempDependencyFile));
        Mockito.when(compiler.compile(any(SlangSource.class), anyString(), anySetOf(SlangSource.class))).thenReturn(emptyCompilationArtifact);
        CompilationArtifact compilationArtifact = slang.compileOperation(tempFile, "op", dependencies);
        Assert.assertNotNull(compilationArtifact);
        Set<SlangSource> dependencyFiles = new HashSet<>();
        dependencyFiles.add(SlangSource.fromFile(tempDependencyFile));
        Mockito.verify(compiler).compile(tempFile, "op", dependencyFiles);
    }

    @Test
    public void testCompileOperationWithNoName() throws IOException {
        SlangSource tempFile = createTempFile();
        Mockito.when(compiler.compile(any(SlangSource.class), anyString(), anySetOf(SlangSource.class))).thenReturn(emptyCompilationArtifact);
        CompilationArtifact compilationArtifact = slang.compileOperation(tempFile, null, new HashSet<SlangSource>());
        Assert.assertNotNull(compilationArtifact);
        Mockito.verify(compiler).compile(tempFile, null, new HashSet<SlangSource>());
    }

    @Test(expected = Exception.class)
    public void testCompileOperationWithException() throws IOException {
        Mockito.when(compiler.compile(any(SlangSource.class), anyString(), anySetOf(SlangSource.class))).thenThrow(Exception.class);
        SlangSource tempFile = createTempFile();
        slang.compileOperation(tempFile, "op", new HashSet<SlangSource>());
    }

    private SlangSource createTempFile() throws IOException {
        File tempFile = File.createTempFile("temp", null);
        tempFile.deleteOnExit();
        return SlangSource.fromFile(tempFile);
    }

    // Tests for the compile() method

    @Test
    public void testCompile() throws IOException {
        SlangSource tempFile = createTempFile();
        Mockito.when(compiler.compile(any(SlangSource.class), anyString(), anySetOf(SlangSource.class))).thenReturn(emptyCompilationArtifact);
        CompilationArtifact compilationArtifact = slang.compile(tempFile, new HashSet<SlangSource>());
        Assert.assertNotNull(compilationArtifact);
        Mockito.verify(compiler).compile(tempFile, null, new HashSet<SlangSource>());
    }

    // tests for run() method

    @Test
    public void testRun(){
        CompilationArtifact compilationArtifact = new CompilationArtifact(new ExecutionPlan(), new HashMap<String, ExecutionPlan>(), new ArrayList<Input>());
        Long executionId = slang.run(compilationArtifact, new HashMap<String, Serializable>(), null);
        Assert.assertNotNull(executionId);

        ArgumentCaptor<TriggeringProperties> argumentCaptor = ArgumentCaptor.forClass(TriggeringProperties.class);
        Mockito.verify(score).trigger(argumentCaptor.capture());

        TriggeringProperties triggeringProperties = argumentCaptor.getValue();
        Assert.assertTrue(triggeringProperties.getContext().containsKey(ScoreLangConstants.RUN_ENV));
        Assert.assertTrue(triggeringProperties.getContext().containsKey(ScoreLangConstants.USER_INPUTS_KEY));
    }

    @Test
    public void testRunWithNullInputs(){
        CompilationArtifact compilationArtifact = new CompilationArtifact(new ExecutionPlan(), new HashMap<String, ExecutionPlan>(), new ArrayList<Input>());
        Long executionId = slang.run(compilationArtifact, null, null);
        Assert.assertNotNull(executionId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRunWithNullCompilationArtifact(){
        slang.run(null, new HashMap<String, Serializable>(), null);
    }

    // tests for compileAndRunOperation() method

    @Test
    public void testLaunchOperation() throws IOException {
        Slang mockSlang = Mockito.mock(SlangImpl.class);
        SlangSource tempFile = createTempFile();
        CompilationArtifact compilationArtifact = new CompilationArtifact(new ExecutionPlan(), new HashMap<String, ExecutionPlan>(), new ArrayList<Input>());
        Mockito.when(mockSlang.compileOperation(tempFile, "op", new HashSet<SlangSource>())).thenReturn(compilationArtifact);
        when(mockSlang.compileAndRunOperation(any(SlangSource.class), anyString(), anySetOf(SlangSource.class), anyMapOf(String.class, Serializable.class))).thenCallRealMethod();
        Long id = mockSlang.compileAndRunOperation(tempFile, "op", new HashSet<SlangSource>(), new HashMap<String, Serializable>());
        Assert.assertNotNull(id);
        Mockito.verify(mockSlang).run(compilationArtifact, new HashMap<String, Serializable>(), null);
    }

    //tests for compileAndRun() method

    @Test
    public void testLaunch() throws IOException {
        Slang mockSlang = Mockito.mock(SlangImpl.class);
        SlangSource tempFile = createTempFile();

        when(mockSlang.compileAndRun(any(SlangSource.class), anySetOf(SlangSource.class), anyMapOf(String.class, Serializable.class))).thenCallRealMethod();
        Long id = mockSlang.compileAndRun(tempFile, new HashSet<SlangSource>(), new HashMap<String, Serializable>());
        Assert.assertNotNull(id);
        Mockito.verify(mockSlang).compileAndRunOperation(tempFile, null, new HashSet<SlangSource>(), new HashMap<String, Serializable>());
    }

    // tests for subscribeOnEvents() method

    @Test
    public void testSubscribeOnEventsWithListener(){
        ScoreEventListener eventListener = new EventListener();
        Set<String> eventTypes = new HashSet<>();
        eventTypes.add(EventConstants.SCORE_ERROR_EVENT);
        slang.subscribeOnEvents(eventListener, eventTypes);
        Mockito.verify(eventBus).subscribe(eventListener, eventTypes);
    }

    @Test
    public void testUnSubscribeOnEvents(){
        ScoreEventListener eventListener = new EventListener();
        slang.unSubscribeOnEvents(eventListener);
        Mockito.verify(eventBus).unsubscribe(eventListener);
    }

    @Test
    public void testSubscribeOnAllEventsWithListener(){
        Slang mockSlang = Mockito.mock(SlangImpl.class);
        ScoreEventListener eventListener = new EventListener();
        Mockito.doCallRealMethod().when(mockSlang).subscribeOnAllEvents(any(ScoreEventListener.class));
        mockSlang.subscribeOnAllEvents(eventListener);
        Mockito.verify(mockSlang).subscribeOnEvents(eq(eventListener), argThat(new Predicate<Set<String>>() {
            @Override
            public boolean apply(Set<String> item) {
                return item.size() == ALL_EVENTS_SIZE;
            }
        }));
    }

    private class EventListener implements ScoreEventListener{

        @Override
        public void onEvent(ScoreEvent event) throws InterruptedException {

        }
    }


    @Configuration
    static class Config {

        @Bean
        public SlangImpl slang(){
            return new SlangImpl();
        }

        @Bean
        public SlangCompiler compiler() {
            SlangCompiler compiler = mock(SlangCompiler.class);
            CompilationArtifact compilationArtifact = new CompilationArtifact(new ExecutionPlan(), new HashMap<String, ExecutionPlan>(), new ArrayList<Input>());
            Mockito.when(compiler.compileFlow(any(SlangSource.class), anySetOf(SlangSource.class))).thenReturn(compilationArtifact);
            return compiler;
        }

        @Bean
        public Score score(){
            return mock(Score.class);
        }

        @Bean
        public EventBus eventBus(){
            return mock(EventBus.class);
        }
    }
}
