package com.hp.score.lang.api;
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

import ch.lambdaj.function.matcher.Predicate;
import com.hp.score.lang.compiler.SlangCompiler;
import com.hp.score.lang.entities.CompilationArtifact;
import com.hp.score.lang.entities.ScoreLangConstants;
import com.hp.score.lang.entities.bindings.Input;
import org.eclipse.score.api.ExecutionPlan;
import org.eclipse.score.api.Score;
import org.eclipse.score.api.TriggeringProperties;
import org.eclipse.score.events.EventBus;
import org.eclipse.score.events.EventConstants;
import org.eclipse.score.events.ScoreEvent;
import org.eclipse.score.events.ScoreEventListener;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
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
        File tempFile = createTempFile();
        Mockito.when(compiler.compile(any(File.class), anyString(), anySetOf(File.class))).thenReturn(emptyCompilationArtifact);
        CompilationArtifact compilationArtifact = slang.compileOperation(tempFile.getPath(), "op", new HashSet<String>());
        Assert.assertNotNull(compilationArtifact);
        Mockito.verify(compiler).compile(tempFile, "op", new HashSet<File>());
   }

    @Test(expected = IllegalArgumentException.class)
    public void testCompileOperationWithNoFilePath(){
        slang.compileOperation(null, "op", new HashSet<String>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCompileOperationWithCorruptedFilePath(){
        slang.compileOperation("/", "op", new HashSet<String>());
    }

    @Test
    public void testCompileOperationWithNullDependencies() throws IOException {
        File tempFile = createTempFile();
        Mockito.when(compiler.compile(any(File.class), anyString(), anySetOf(File.class))).thenReturn(emptyCompilationArtifact);
        CompilationArtifact compilationArtifact = slang.compileOperation(tempFile.getPath(), "op", null);
        Assert.assertNotNull(compilationArtifact);
        Mockito.verify(compiler).compile(tempFile, "op", new HashSet<File>());
    }

    @Test
    public void testCompileOperationWithDependencies() throws IOException {
        File tempFile = createTempFile();
        File tempDependencyFile = File.createTempFile("tempDependency", null);
        tempDependencyFile.deleteOnExit();
        Set<String> dependencies = new HashSet<>();
        dependencies.add(tempDependencyFile.getPath());
        Mockito.when(compiler.compile(any(File.class), anyString(), anySetOf(File.class))).thenReturn(emptyCompilationArtifact);
        CompilationArtifact compilationArtifact = slang.compileOperation(tempFile.getPath(), "op", dependencies);
        Assert.assertNotNull(compilationArtifact);
        Set<File> dependencyFiles = new HashSet<>();
        dependencyFiles.add(tempDependencyFile);
        Mockito.verify(compiler).compile(tempFile, "op", dependencyFiles);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCompileOperationWithCorruptedDependencies() throws IOException {
        File tempFile = createTempFile();
        Set<String> dependencies = new HashSet<>();
        dependencies.add("noFile");
        slang.compileOperation(tempFile.getPath(), "op", dependencies);
    }

    @Test
    public void testCompileOperationWithNoName() throws IOException {
        File tempFile = createTempFile();
        Mockito.when(compiler.compile(any(File.class), anyString(), anySetOf(File.class))).thenReturn(emptyCompilationArtifact);
        CompilationArtifact compilationArtifact = slang.compileOperation(tempFile.getPath(), null, new HashSet<String>());
        Assert.assertNotNull(compilationArtifact);
        Mockito.verify(compiler).compile(tempFile, null, new HashSet<File>());
    }

    @Test(expected = Exception.class)
    public void testCompileOperationWithException() throws IOException {
        Mockito.when(compiler.compile(any(File.class), anyString(), anySetOf(File.class))).thenThrow(Exception.class);
        File tempFile = createTempFile();
        slang.compileOperation(tempFile.getPath(), "op", new HashSet<String>());
    }

    private File createTempFile() throws IOException {
        File tempFile = File.createTempFile("temp", null);
        tempFile.deleteOnExit();
        return tempFile;
    }

    // Tests for the compile() method

    @Test
    public void testCompile() throws IOException {
        File tempFile = createTempFile();
        Mockito.when(compiler.compile(any(File.class), anyString(), anySetOf(File.class))).thenReturn(emptyCompilationArtifact);
        CompilationArtifact compilationArtifact = slang.compile(tempFile.getPath(), new HashSet<String>());
        Assert.assertNotNull(compilationArtifact);
        Mockito.verify(compiler).compile(tempFile, null, new HashSet<File>());
    }

    // tests for run() method

    @Test
    public void testRun(){
        CompilationArtifact compilationArtifact = new CompilationArtifact(new ExecutionPlan(), new HashMap<String, ExecutionPlan>(), new ArrayList<Input>());
        Long executionId = slang.run(compilationArtifact, new HashMap<String, String>());
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
        Long executionId = slang.run(compilationArtifact, null);
        Assert.assertNotNull(executionId);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRunWithNullCompilationArtifact(){
        slang.run(null, new HashMap<String, String>());
    }

    // tests for launchOperation() method

    @Test
    public void testLaunchOperation() throws IOException {
        Slang mockSlang = Mockito.mock(SlangImpl.class);
        File tempFile = createTempFile();
        CompilationArtifact compilationArtifact = new CompilationArtifact(new ExecutionPlan(), new HashMap<String, ExecutionPlan>(), new ArrayList<Input>());
        Mockito.when(mockSlang.compileOperation(tempFile.getPath(), "op", new HashSet<String>())).thenReturn(compilationArtifact);
        when(mockSlang.launchOperation(anyString(), anyString(), anySetOf(String.class), anyMapOf(String.class,String.class))).thenCallRealMethod();
        Long id = mockSlang.launchOperation(tempFile.getPath(), "op", new HashSet<String>(), new HashMap<String, String>());
        Assert.assertNotNull(id);
        Mockito.verify(mockSlang).run(compilationArtifact, new HashMap<String, String>());
    }

    //tests for launch() method

    @Test
    public void testLaunch() throws IOException {
        Slang mockSlang = Mockito.mock(SlangImpl.class);
        File tempFile = createTempFile();

        when(mockSlang.launch(anyString(), anySetOf(String.class), anyMapOf(String.class,String.class))).thenCallRealMethod();
        Long id = mockSlang.launch(tempFile.getPath(), new HashSet<String>(), new HashMap<String, String>());
        Assert.assertNotNull(id);
        Mockito.verify(mockSlang).launchOperation(tempFile.getPath(), null, new HashSet<String>(), new HashMap<String, String>());
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
    public void testSubscribeOnEvents(){
        Slang mockSlang = Mockito.mock(SlangImpl.class);
        Mockito.doCallRealMethod().when(mockSlang).subscribeOnEvents(anySetOf(String.class));
        Set<String> eventTypes = new HashSet<>();
        eventTypes.add(EventConstants.SCORE_ERROR_EVENT);
        mockSlang.subscribeOnEvents(eventTypes);
        Mockito.verify(mockSlang).subscribeOnEvents(any(ScoreEventListener.class), eq(eventTypes));
    }

    @Test
    public void testUnSubscribeOnEvents(){
        ScoreEventListener eventListener = new EventListener();
        slang.unSubscribeOnEvents(eventListener);
        Mockito.verify(eventBus).unsubscribe(eventListener);
    }

    @Test
    public void testSubscribeOnAllEvents(){
        Slang mockSlang = Mockito.mock(SlangImpl.class);
        Mockito.doCallRealMethod().when(mockSlang).subscribeOnAllEvents();
        mockSlang.subscribeOnAllEvents();
        Mockito.verify(mockSlang).subscribeOnEvents(argThat(new Predicate<Set<String>>() {
            @Override
            public boolean apply(Set<String> item) {
                return item.size() == ALL_EVENTS_SIZE;
            }
        }));
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
            Mockito.when(compiler.compileFlow(any(File.class), anySetOf(File.class))).thenReturn(compilationArtifact);
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
