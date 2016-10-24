/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.cli.services;

import io.cloudslang.lang.api.Slang;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.score.events.EventConstants;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.events.ScoreEventListener;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyMapOf;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Date: 24/2/2015
 *
 * @author Bonczidai Levente
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ScoreServicesImplTest.Config.class)
public class ScoreServicesImplTest {

    private static final long DEFAULT_THREAD_SLEEP_TIME = 100;
    private static final long DEFAULT_TIMEOUT = 5000;
    private static final long DEFAULT_EXECUTION_ID = 1;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    private ScoreServicesImpl scoreServicesImpl;

    @Autowired
    private Slang slang;

    @Before
    public void setUp() {
        reset(slang);
        when(
                slang.run(
                        any(CompilationArtifact.class),
                        anyMapOf(String.class, Value.class),
                        anySetOf(SystemProperty.class)))
                .thenReturn(DEFAULT_EXECUTION_ID);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testSubscribe() throws Exception {
        ScoreEventListener eventHandler = mock(ScoreEventListener.class);
        Set<String> eventTypes = newHashSet("a", "b");

        scoreServicesImpl.subscribe(eventHandler, eventTypes);

        verify(slang).subscribeOnEvents(eventHandler, eventTypes);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testTrigger() throws Exception {
        final CompilationArtifact compilationArtifact = mock(CompilationArtifact.class);
        Map<String, Value> inputs = new HashMap<>();
        inputs.put("a", ValueFactory.create(1));
        Set<SystemProperty> systemProperties = newHashSet(
                new SystemProperty("ns", "b", "c")
        );

        long executionId = scoreServicesImpl.trigger(compilationArtifact, inputs, systemProperties);

        verify(slang).run(compilationArtifact, inputs, systemProperties);
        assertEquals(DEFAULT_EXECUTION_ID, executionId);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testTriggerSyncSuccess() throws Exception {
        //prepare method args
        final CompilationArtifact compilationArtifact = mock(CompilationArtifact.class);
        Map<String, Value> inputs = new HashMap<>();
        inputs.put("a", ValueFactory.create(1));
        Set<SystemProperty> systemProperties = newHashSet(
                new SystemProperty("ns", "b", "c")
        );

        /* stubbing subscribeEvents method - mocking the behaviour of the EventBus
         * After a specific amount of time a onCompilationFinish event needs to be sent
         * to the ScoreEventListener.onEvent() method in order to onCompilationFinish the execution
         * of the triggerSync method.
         */
        doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                final ScoreEventListener scoreEventListener = (ScoreEventListener) invocation.getArguments()[0];
                Thread eventDispatcherThread = new Thread() {
                    public void run() {
                        try {
                            Thread.sleep(DEFAULT_THREAD_SLEEP_TIME);
                            ScoreEvent scoreFinishedEvent =
                                    new ScoreEvent(EventConstants.SCORE_FINISHED_EVENT, new HashMap<>());
                            scoreEventListener.onEvent(scoreFinishedEvent);
                        } catch (InterruptedException ignore) {
                        }
                    }
                };
                eventDispatcherThread.start();
                return null;
            }
        }).when(slang).subscribeOnEvents(any(ScoreEventListener.class), anySetOf(String.class));

        // invoke method
        final long executionId = scoreServicesImpl
                .triggerSync(compilationArtifact, inputs, systemProperties, false, false);

        // verify constraints
        ArgumentCaptor<ScoreEventListener> scoreEventListenerArg = ArgumentCaptor.forClass(ScoreEventListener.class);
        verify(slang).subscribeOnEvents(scoreEventListenerArg.capture(), anySetOf(String.class));
        verify(slang).run(compilationArtifact, inputs, systemProperties);
        verify(slang).unSubscribeOnEvents(scoreEventListenerArg.getValue());
        assertEquals("execution ID not as expected", DEFAULT_EXECUTION_ID, executionId);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testTriggerSyncException() throws Exception {
        //prepare method args
        final CompilationArtifact compilationArtifact = mock(CompilationArtifact.class);
        Map<String, Value> inputs = new HashMap<>();
        inputs.put("a", ValueFactory.create(1));
        final Set<SystemProperty> systemProperties = newHashSet(new SystemProperty("ns", "b", "c"));

        /* stubbing subscribeEvents method - mocking the behaviour of the EventBus
         * After a specific amount of time a erroneous event followed by a onCompilationFinish event
         * need to be sent to the ScoreEventListener.onEvent() method in order to
         * test the error case of the triggerSync method.
         */
        doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                final ScoreEventListener scoreEventListener = (ScoreEventListener) invocation.getArguments()[0];
                Thread eventDispatcherThread = new Thread() {
                    public void run() {
                        try {
                            Thread.sleep(DEFAULT_THREAD_SLEEP_TIME);

                            Map<String, Serializable> slangExecutionExceptionEventData = new HashMap<>();
                            slangExecutionExceptionEventData.put(LanguageEventData.EXCEPTION, "exception message");
                            ScoreEvent slangExecutionExceptionEvent = new ScoreEvent(
                                    ScoreLangConstants.SLANG_EXECUTION_EXCEPTION,
                                    (Serializable) slangExecutionExceptionEventData);
                            ScoreEvent scoreFinishedEvent =
                                    new ScoreEvent(EventConstants.SCORE_FINISHED_EVENT, new HashMap<>());

                            scoreEventListener.onEvent(slangExecutionExceptionEvent);
                            scoreEventListener.onEvent(scoreFinishedEvent);
                        } catch (InterruptedException ignore) {
                        }
                    }
                };
                eventDispatcherThread.start();
                return null;
            }
        }).when(slang).subscribeOnEvents(any(ScoreEventListener.class), anySetOf(String.class));

        exception.expect(RuntimeException.class);
        exception.expectMessage("exception message");

        // invoke method
        scoreServicesImpl.triggerSync(compilationArtifact, inputs, systemProperties, false, false);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testTriggerSyncSpException() throws Exception {
        //prepare method args
        final CompilationArtifact compilationArtifact = mock(CompilationArtifact.class);
        final Map<String, Value> inputs = new HashMap<>();
        ValueFactory.create(1);
        final Set<SystemProperty> systemProperties = newHashSet(new SystemProperty("ns", "b", "c"));

        doAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                final ScoreEventListener scoreEventListener = (ScoreEventListener) invocation.getArguments()[0];
                Thread eventDispatcherThread = new Thread() {
                    public void run() {
                        try {
                            Thread.sleep(DEFAULT_THREAD_SLEEP_TIME);

                            Map<String, Serializable> slangExecutionExceptionEventData = new HashMap<>();
                            slangExecutionExceptionEventData
                                    .put(LanguageEventData.EXCEPTION,
                                            "This value can also be supplied using a system property");
                            ScoreEvent slangExecutionExceptionEvent = new ScoreEvent(
                                    ScoreLangConstants.SLANG_EXECUTION_EXCEPTION,
                                    (Serializable) slangExecutionExceptionEventData);
                            ScoreEvent scoreFinishedEvent =
                                    new ScoreEvent(EventConstants.SCORE_FINISHED_EVENT, new HashMap<>());

                            scoreEventListener.onEvent(slangExecutionExceptionEvent);
                            scoreEventListener.onEvent(scoreFinishedEvent);
                        } catch (InterruptedException ignore) {
                        }
                    }
                };
                eventDispatcherThread.start();
                return null;
            }
        }).when(slang).subscribeOnEvents(any(ScoreEventListener.class), anySetOf(String.class));

        exception.expect(RuntimeException.class);
        exception.expectMessage("This value can also be supplied using a system property");
        exception.expectMessage("A system property file can be included using --spf <path_to_file>");


        // invoke method
        scoreServicesImpl.triggerSync(compilationArtifact, inputs, systemProperties, false, false);
    }

    @Configuration
    static class Config {

        @Bean
        public ScoreServicesImpl scoreServicesImpl() {
            return new ScoreServicesImpl();
        }

        @Bean
        public Slang slang() {
            return mock(Slang.class);
        }

        @Bean
        public ConsolePrinter consolePrinter() {
            return new ConsolePrinterImpl();
        }

    }
}
