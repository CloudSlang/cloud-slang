/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.systemtests;

import com.google.common.collect.Sets;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.SensitiveValue;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.entities.properties.EventVerbosityLevel;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.score.events.ScoreEvent;
import org.apache.commons.collections4.MapUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.cloudslang.lang.compiler.SlangSource.fromFile;
import static io.cloudslang.lang.entities.properties.SlangSystemPropertyConstant.CSLANG_RUNTIME_EVENTS_VERBOSITY;
import static org.junit.Assert.fail;

public class EventDataTest extends SystemsTestsParent {
    private static final long DEFAULT_TIMEOUT = 60000;
    private static final Set<SystemProperty> EMPTY_SP_SET = Collections.emptySet();
    private static final String SENSITIVE_VALUE_STRING = "sensitive_value";

    private static final Map<String, Serializable> CONTEXT_USER_INPUTS;
    private static final Map<String, Serializable> CONTEXT_FLOW_01;
    private static final Map<String, Serializable> CONTEXT_FLOW_02;
    private static final Map<String, Serializable> CONTEXT_FLOW_03;
    private static final Map<String, Serializable> CONTEXT_OPERATION_01;
    private static final Map<String, Serializable> CONTEXT_STEP_PUBLISH_01;
    private static final Map<String, Serializable> CONTEXT_BRANCH_END_01;
    private static final Map<String, Serializable> CONTEXT_BRANCH_END_02;
    private static final Map<String, Serializable> CONTEXT_BRANCH_END_03;

    static {
        CONTEXT_USER_INPUTS = new HashMap<>();
        CONTEXT_USER_INPUTS.put("flow01_input01", "xyz");
        CONTEXT_USER_INPUTS.put("flow01_input03", SensitiveValue.SENSITIVE_VALUE_MASK);

        CONTEXT_FLOW_01 = new HashMap<>();
        CONTEXT_FLOW_01.putAll(CONTEXT_USER_INPUTS);
        CONTEXT_FLOW_01.put("flow01_input02", "abc");

        CONTEXT_OPERATION_01 = new HashMap<>();
        CONTEXT_OPERATION_01.put("op01_input_01", "def");
        CONTEXT_OPERATION_01.put("op01_input_03", SensitiveValue.SENSITIVE_VALUE_MASK);
        CONTEXT_OPERATION_01.put("op01_input_02", "abc");

        CONTEXT_FLOW_02 = new HashMap<>();
        CONTEXT_FLOW_02.put("flow01_input01", "xyz");
        CONTEXT_FLOW_02.put("flow01_input02", "abc");
        CONTEXT_FLOW_02.put("flow01_input03", SensitiveValue.SENSITIVE_VALUE_MASK);
        CONTEXT_FLOW_02.put("parallel_loop_output", "efg");
        CONTEXT_FLOW_02.put("step01_publish_01", "def");
        CONTEXT_FLOW_02.put("step01_publish_02", "out02");
        CONTEXT_FLOW_02.put("step01_publish_03", SensitiveValue.SENSITIVE_VALUE_MASK);

        CONTEXT_FLOW_03 = new HashMap<>();
        CONTEXT_FLOW_03.put("flow01_input01", "xyz");
        CONTEXT_FLOW_03.put("flow01_input02", "abc");
        CONTEXT_FLOW_03.put("flow01_input03", SensitiveValue.SENSITIVE_VALUE_MASK);
        CONTEXT_FLOW_03.put("step01_publish_01", "def");
        CONTEXT_FLOW_03.put("step01_publish_02", "out02");
        CONTEXT_FLOW_03.put("step01_publish_03", SensitiveValue.SENSITIVE_VALUE_MASK);

        CONTEXT_STEP_PUBLISH_01 = new HashMap<>();
        CONTEXT_STEP_PUBLISH_01.put("op01_input_01", "def");
        CONTEXT_STEP_PUBLISH_01.put("op01_input_02", "abc");
        CONTEXT_STEP_PUBLISH_01.put("op01_input_03", SensitiveValue.SENSITIVE_VALUE_MASK);
        CONTEXT_STEP_PUBLISH_01.put("op01_output_01", "def");
        CONTEXT_STEP_PUBLISH_01.put("op01_output_02", "out02");
        CONTEXT_STEP_PUBLISH_01.put("op01_output_03", SensitiveValue.SENSITIVE_VALUE_MASK);

        CONTEXT_BRANCH_END_01 = new HashMap<>();
        CONTEXT_BRANCH_END_01.put("flow01_input01", "xyz");
        CONTEXT_BRANCH_END_01.put("flow01_input02", "abc");
        CONTEXT_BRANCH_END_01.put("flow01_input03", SensitiveValue.SENSITIVE_VALUE_MASK);
        CONTEXT_BRANCH_END_01.put("step01_publish_01", "def");
        CONTEXT_BRANCH_END_01.put("step01_publish_02", "out02");
        CONTEXT_BRANCH_END_01.put("step01_publish_03", SensitiveValue.SENSITIVE_VALUE_MASK);
        CONTEXT_BRANCH_END_01.put("op01_output_01", "1");
        CONTEXT_BRANCH_END_01.put("op01_output_02", "out02");
        CONTEXT_BRANCH_END_01.put("op01_output_03", SensitiveValue.SENSITIVE_VALUE_MASK);
        CONTEXT_BRANCH_END_01.put("x", "1");

        CONTEXT_BRANCH_END_02 = new HashMap<>(CONTEXT_BRANCH_END_01);
        CONTEXT_BRANCH_END_02.put("op01_output_01", "2");
        CONTEXT_BRANCH_END_02.put("x", "2");
        CONTEXT_BRANCH_END_03 = new HashMap<>(CONTEXT_BRANCH_END_02);
        CONTEXT_BRANCH_END_03.put("op01_output_01", "3");
        CONTEXT_BRANCH_END_03.put("x", "3");
    }

    @After
    public void tearDown() throws Exception {
        // do not store context in events
        System.setProperty(CSLANG_RUNTIME_EVENTS_VERBOSITY.getValue(), EventVerbosityLevel.DEFAULT.getValue());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testEventDataNoContext() throws Exception {
        // do not store context in events
        System.setProperty(CSLANG_RUNTIME_EVENTS_VERBOSITY.getValue(), EventVerbosityLevel.DEFAULT.getValue());

        Map<String, Value> inputs = new HashMap<>();
        inputs.put("flow01_input01", ValueFactory.create("xyz"));
        inputs.put("flow01_input03", ValueFactory.create(SENSITIVE_VALUE_STRING, true));

        List<ScoreEvent> events = compileAndRunExecutable(inputs, EMPTY_SP_SET);

        for (ScoreEvent scoreEvent : events) {
            LanguageEventData eventDataAsMap = getData(scoreEvent);
            if (eventDataAsMap.keySet().contains(LanguageEventData.CONTEXT)) {
                fail("Context key should not be in event data");
            }
        }
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testEventDataWithContext() throws Exception {
        // store context in events
        System.setProperty(CSLANG_RUNTIME_EVENTS_VERBOSITY.getValue(), EventVerbosityLevel.ALL.getValue());

        Map<String, Value> inputs = new HashMap<>();
        inputs.put("flow01_input01", ValueFactory.create("xyz"));
        inputs.put("flow01_input03", ValueFactory.create(SENSITIVE_VALUE_STRING, true));

        List<ScoreEvent> events = compileAndRunExecutable(inputs, EMPTY_SP_SET);
        Map<String, List<LanguageEventData>> eventDataByPath = groupByPath(events);

        validateEventData(eventDataByPath,"0", ScoreLangConstants.EVENT_INPUT_START, CONTEXT_USER_INPUTS);
        validateEventData(eventDataByPath,"0", ScoreLangConstants.EVENT_INPUT_END, CONTEXT_USER_INPUTS);
        validateEventData(eventDataByPath,"0", ScoreLangConstants.EVENT_OUTPUT_END, CONTEXT_FLOW_02);
        validateEventData(eventDataByPath,"0", ScoreLangConstants.EVENT_EXECUTION_FINISHED, CONTEXT_FLOW_02);

        validateEventData(eventDataByPath,"0.0", ScoreLangConstants.EVENT_STEP_START, CONTEXT_FLOW_01);
        validateEventData(eventDataByPath,"0.0", ScoreLangConstants.EVENT_ARGUMENT_START, CONTEXT_FLOW_01);
        validateEventData(eventDataByPath,"0.0", ScoreLangConstants.EVENT_ARGUMENT_END, CONTEXT_FLOW_01);
        validateEventData(eventDataByPath,"0.0", ScoreLangConstants.EVENT_ACTION_START, CONTEXT_OPERATION_01);
        validateEventData(eventDataByPath,"0.0", ScoreLangConstants.EVENT_ACTION_END, CONTEXT_OPERATION_01);
        validateEventData(eventDataByPath,"0.0", ScoreLangConstants.EVENT_OUTPUT_START, CONTEXT_OPERATION_01);
        // subflow outputs validation
        validateEventData(eventDataByPath,"0.0", ScoreLangConstants.EVENT_OUTPUT_END, CONTEXT_OPERATION_01);
        // step publish validation
        validateEventData(eventDataByPath,"0.0", ScoreLangConstants.EVENT_OUTPUT_END, CONTEXT_STEP_PUBLISH_01, 1);

        // parallel loop
        validateEventData(eventDataByPath,"0.1", ScoreLangConstants.EVENT_SPLIT_BRANCHES, CONTEXT_FLOW_03);
        validateEventData(eventDataByPath,"0.1.0", ScoreLangConstants.EVENT_BRANCH_START, CONTEXT_FLOW_03);
        validateEventData(eventDataByPath,"0.1.0", ScoreLangConstants.EVENT_BRANCH_END, CONTEXT_BRANCH_END_01);
        validateEventData(eventDataByPath,"0.1.1", ScoreLangConstants.EVENT_BRANCH_END, CONTEXT_BRANCH_END_02);
        validateEventData(eventDataByPath,"0.1.2", ScoreLangConstants.EVENT_BRANCH_END, CONTEXT_BRANCH_END_03);
        validateEventData(
            eventDataByPath,
            "0.1",
            ScoreLangConstants.EVENT_JOIN_BRANCHES_START,
            Collections.<String, Serializable>emptyMap()
        );
        validateEventData(
            eventDataByPath,
            "0.1",
            ScoreLangConstants.EVENT_JOIN_BRANCHES_END,
            Collections.<String, Serializable>emptyMap()
        );

        validateSensitiveDataNotReveiledInContext(events);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testEventDataWithException() throws Exception {
        // store context in events
        System.setProperty(CSLANG_RUNTIME_EVENTS_VERBOSITY.getValue(), EventVerbosityLevel.ALL.getValue());

        Map<String, Value> inputs = new HashMap<>();
        inputs.put("flow01_input01", ValueFactory.create("xyz"));
        inputs.put("flow01_input03", ValueFactory.create(SENSITIVE_VALUE_STRING, true));

        List<ScoreEvent> events = compileAndRunExecutableWithException(inputs, EMPTY_SP_SET);
        Map<String, List<LanguageEventData>> eventDataByPath = groupByPath(events);

        validateEventData(eventDataByPath,"0.0", ScoreLangConstants.EVENT_ACTION_ERROR, CONTEXT_OPERATION_01);
        validateEventData(
            eventDataByPath,
            "0.0.0",
            ScoreLangConstants.SLANG_EXECUTION_EXCEPTION, CONTEXT_OPERATION_01
        );

        validateSensitiveDataNotReveiledInContext(events);
    }

    private void validateSensitiveDataNotReveiledInContext(List<ScoreEvent> events) {
        for (ScoreEvent scoreEvent : events) {
            LanguageEventData eventDataAsMap = getData(scoreEvent);
            Map<String, Serializable> context = eventDataAsMap.getContext();
            if (MapUtils.isNotEmpty(context)) {
                for (Serializable value : context.values()) {
                    if (value.toString().contains(SENSITIVE_VALUE_STRING)) {
                        fail(SENSITIVE_VALUE_STRING + " string should not be in event data");
                    }
                }
            }
        }
    }

    private void validateEventData(
        Map<String, List<LanguageEventData>> eventDataByPath,
        String path,
        String eventType,
        Map<String, Serializable> expectedContext,
        int skipEventsCounter) {
        List<LanguageEventData> eventsForPath = eventDataByPath.get(path);
        // select first event with type - in general we should not rely on event order
        // but in this case the group by path is a good enough exception factor
        LanguageEventData actualEventData = null;
        for (LanguageEventData data : eventsForPath) {
            if (eventType.equals(data.getEventType())) {
                if (skipEventsCounter-- <= 0) {
                    actualEventData = data;
                    break;
                }
            }
        }
        Assert.assertNotNull("Event data not found", actualEventData);
        Assert.assertEquals("Event data not as expected", expectedContext, actualEventData.getContext());
    }

    private void validateEventData(
        Map<String, List<LanguageEventData>> eventDataByPath,
        String path,
        String eventType,
        Map<String, Serializable> expectedContext) {
        validateEventData(eventDataByPath, path, eventType, expectedContext, 0);
    }

    private Map<String, List<LanguageEventData>> groupByPath(List<ScoreEvent> events) {
        Map<String, List<LanguageEventData>> result =  new HashMap<>();
        for (ScoreEvent scoreEvent : events) {
            LanguageEventData eventData = getData(scoreEvent);
            String path = eventData.getPath();
            List<LanguageEventData> eventsForPath = result.get(path);
            if (eventsForPath == null) {
                eventsForPath = new ArrayList<>();
            }
            eventsForPath.add(eventData);
            result.put(path, eventsForPath);
        }
        return result;
    }

    private LanguageEventData getData(ScoreEvent scoreEvent) {
        Serializable eventData = scoreEvent.getData();
        @SuppressWarnings("unchecked")
       LanguageEventData eventDataAsMap = (LanguageEventData) eventData;
        return eventDataAsMap;
    }

    private List<ScoreEvent> compileAndRunExecutable(
        Map<String, Value> inputs,
        Set<SystemProperty> systemProperties) throws Exception {
        URI flow = getClass().getResource("/yaml/events/flow01.sl").toURI();
        URI operations1 = getClass().getResource("/yaml/events/op01.sl").toURI();
        Set<SlangSource> dependencies = Sets.newHashSet(fromFile(operations1));
        CompilationArtifact compilationArtifact = slang.compile(fromFile(flow), dependencies);

        List<ScoreEvent> events = runAndCollectAllEvents(compilationArtifact, inputs, systemProperties);

        Assert.assertFalse(CollectionUtils.isEmpty(events));
        for (ScoreEvent scoreEvent : events) {
            if (ScoreLangConstants.EVENT_EXECUTION_FINISHED.equals(scoreEvent.getEventType())) {
                return events;
            }
        }
        fail("Finished event not received.");

        return events;
    }

    private List<ScoreEvent> compileAndRunExecutableWithException(
        Map<String, Value> inputs,
        Set<SystemProperty> systemProperties) throws Exception {
        URI flow = getClass().getResource("/yaml/events/flow02.sl").toURI();
        URI operations1 = getClass().getResource("/yaml/events/op02.sl").toURI();
        Set<SlangSource> dependencies = Sets.newHashSet(fromFile(operations1));
        CompilationArtifact compilationArtifact = slang.compile(fromFile(flow), dependencies);

        List<ScoreEvent> events = runAndCollectAllEvents(compilationArtifact, inputs, systemProperties);

        Assert.assertFalse(CollectionUtils.isEmpty(events));

        for (ScoreEvent scoreEvent : events) {
            if (ScoreLangConstants.SLANG_EXECUTION_EXCEPTION.equals(scoreEvent.getEventType())) {
                return events;
            }
        }
        fail("Exception event not received.");
        return events;
    }

}
