/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.lang.tests.operation;

import org.openscore.lang.api.Slang;
import org.openscore.lang.entities.CompilationArtifact;
import org.openscore.lang.entities.ScoreLangConstants;
import org.openscore.lang.runtime.events.LanguageEventData;
import org.openscore.events.EventConstants;
import org.openscore.events.ScoreEvent;
import org.openscore.events.ScoreEventListener;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/*
 * Created by orius123 on 12/11/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/systemTestContext.xml")
public class SystemsTestsParent {

    @Autowired
    protected Slang slang;

    private LinkedBlockingQueue<ScoreEvent> queue = new LinkedBlockingQueue<>();
    private List<ScoreEvent> taskEvents;

    protected ScoreEvent trigger(CompilationArtifact compilationArtifact, Map<String, Serializable> userInputs) throws InterruptedException {
        registerHandlers();
        slang.run(compilationArtifact, userInputs);
        ScoreEvent event;
        do {
            event = queue.take();
            Assert.assertNotSame("Error event has been thrown during execution with data: " + event.getData(), ScoreLangConstants.SLANG_EXECUTION_EXCEPTION, event.getEventType());
            System.out.println("Event received: " + event.getEventType() + " Data is: " + event.getData());
        } while (!EventConstants.SCORE_FINISHED_EVENT.equals(event.getEventType()));
        return event;
    }

    protected void startTaskMonitoring() {
        taskEvents = new ArrayList<>();

        Set<String> handlerTypes = new HashSet<>();
        handlerTypes.add(ScoreLangConstants.EVENT_INPUT_END);
        slang.subscribeOnEvents(new ScoreEventListener() {
            @Override
            public void onEvent(ScoreEvent event) {
                LanguageEventData eventData = (LanguageEventData) event.getData();
                String taskKey = LanguageEventData.levelName.TASK_NAME.name();
                if (eventData.containsKey(taskKey)) {
                    // register only tasks, not operations
                    taskEvents.add(event);
                }
            }
        }, handlerTypes);
    }

    protected void verifyTaskOrder(List<String> expectedTasks) {
        List<String> actualTasks = extractTasks();
        Assert.assertEquals("task order not as expected", expectedTasks, actualTasks);
    }
    protected void verifyResults(List<String> expectedResults) {
        List<String> actualTasks = extractResults();
        Assert.assertEquals("results not as expected", expectedResults, actualTasks);
    }

    private List<String> extractTasks() {
        List<String> taskList = new ArrayList<>();
        for (ScoreEvent event: taskEvents) {
            LanguageEventData eventData = (LanguageEventData) event.getData();
            String taskKey = LanguageEventData.levelName.TASK_NAME.name();
            String taskName = (String) eventData.get(taskKey);
            taskList.add(taskName);
        }
        return taskList;
    }

    private void registerHandlers() {
        Set<String> handlerTypes = new HashSet<>();
        handlerTypes.add(EventConstants.SCORE_FINISHED_EVENT);
        handlerTypes.add(EventConstants.SCORE_ERROR_EVENT);
        handlerTypes.add(EventConstants.SCORE_FAILURE_EVENT);
        handlerTypes.add(ScoreLangConstants.EVENT_ACTION_START);
        handlerTypes.add(ScoreLangConstants.EVENT_ACTION_END);
        handlerTypes.add(ScoreLangConstants.EVENT_ACTION_ERROR);
        handlerTypes.add(ScoreLangConstants.EVENT_INPUT_START);
        handlerTypes.add(ScoreLangConstants.EVENT_INPUT_END);
        handlerTypes.add(ScoreLangConstants.EVENT_OUTPUT_START);
        handlerTypes.add(ScoreLangConstants.EVENT_OUTPUT_END);
        handlerTypes.add(ScoreLangConstants.SLANG_EXECUTION_EXCEPTION);
        handlerTypes.add(ScoreLangConstants.EVENT_EXECUTION_FINISHED);
        slang.subscribeOnEvents(new ScoreEventListener() {

            @Override
            public void onEvent(ScoreEvent event) {
                try {
                    queue.put(event);
                } catch (InterruptedException ignore) {}
            }
        }, handlerTypes);
    }
    protected void startOperationMonitoring() {
        taskEvents = new ArrayList<>();

        Set<String> handlerTypes = new HashSet<>();
        handlerTypes.add(ScoreLangConstants.EVENT_OUTPUT_END);
        slang.subscribeOnEvents(new ScoreEventListener() {
            @Override
            public void onEvent(ScoreEvent event) {
                LanguageEventData eventData = (LanguageEventData) event.getData();
                String taskKey = LanguageEventData.levelName.TASK_NAME.name();
                if (!eventData.containsKey(taskKey)) {
                    // register only tasks, not operations
                    taskEvents.add(event);
                }
            }
        }, handlerTypes);
    }
    private List<String> extractResults() {
        List<String> results = new ArrayList<>();
        for (ScoreEvent event:taskEvents) {
            LanguageEventData eventData = (LanguageEventData) event.getData();
            String resultKey = LanguageEventData.RESULT;
            String result = (String) eventData.get(resultKey);
            results.add(result);
        }
        return results;
    }
}
