/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.events;

import io.cloudslang.lang.entities.ScoreLangConstants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author moradi
 * @version $Id$
 * @since 16/11/2014
 */
public class LanguageEventDataTest {

    LanguageEventData eventData = new LanguageEventData();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link LanguageEventData#getEventType()}.
     */
    @Test
    public void testEventType() {
        String eventType = ScoreLangConstants.SLANG_EXECUTION_EXCEPTION;
        eventData.setEventType(eventType);
        assertEquals(eventType, eventData.getEventType());
        assertEquals(eventType, eventData.get(LanguageEventData.TYPE));
    }

    /**
     * Test method for {@link LanguageEventData#getDescription()}.
     */
    @Test
    public void testDescription() {
        String desc = "My description";
        eventData.setDescription(desc);
        assertEquals(desc, eventData.getDescription());
        assertEquals(desc, eventData.get(LanguageEventData.DESCRIPTION));
    }

    /**
     * Test method for {@link LanguageEventData#getTimeStamp()}.
     */
    @Test
    public void testTimeStamp() {
        Date ts = new Date();
        eventData.setTimeStamp(ts);
        assertEquals(ts, eventData.getTimeStamp());
        assertEquals(ts, eventData.get(LanguageEventData.TIMESTAMP));
    }

    /**
     * Test method for {@link LanguageEventData#getExecutionId()}.
     */
    @Test
    public void testExecutionId() {
        Long exeId = 123L;
        eventData.setExecutionId(exeId);
        assertEquals(exeId, eventData.getExecutionId());
        assertEquals(exeId, eventData.get(LanguageEventData.EXECUTION_ID));
    }

    /**
     * Test method for {@link LanguageEventData#getPath()}.
     */
    @Test
    public void testPath() {
        String exePath = "0/1/2";
        eventData.setPath(exePath);
        assertEquals(exePath, eventData.getPath());
        assertEquals(exePath, eventData.get(LanguageEventData.PATH));
    }

    /**
     * Test method for {@link LanguageEventData#getResult()}.
     */
    @Test
    public void testResult() {
        String message = "Good";
        eventData.setResult(message);
        assertEquals(message, eventData.getResult());
        assertEquals(message, eventData.get(LanguageEventData.RESULT));
    }

    /**
     * Test method for {@link LanguageEventData#getException()}.
     */
    @Test
    public void testException() {
        String message = "My exception";
        Exception ex = new Exception(message);
        eventData.setException(ex.getMessage());
        assertEquals(message, eventData.getException());
        assertEquals(message, eventData.get(LanguageEventData.EXCEPTION));
    }

    /**
     * Test method for {@link LanguageEventData#getInputs()}.
     */
    @Test
    public void testInputs() {
        HashMap<String, Serializable> inputs = new HashMap<String, Serializable>() {

            private static final long serialVersionUID = 161841000262993977L;

            {
                put("input1", "str1");
                put("input2", 123L);
                put("input3", true);
            }
        };
        eventData.setInputs(inputs);
        assertEquals(inputs, eventData.getInputs());
        assertEquals(inputs, eventData.get(LanguageEventData.BOUND_INPUTS));
    }

    /**
     * Test method for {@link LanguageEventData#getArguments()}.
     */
    @Test
    public void testArguments() {
        HashMap<String, Serializable> arguments = new HashMap<String, Serializable>() {

            private static final long serialVersionUID = 161841000262993977L;

            {
                put("argument1", "str1");
                put("argument2", 123L);
                put("argument3", true);
            }
        };
        eventData.setArguments(arguments);
        assertEquals(arguments, eventData.getArguments());
        assertEquals(arguments, eventData.get(LanguageEventData.BOUND_ARGUMENTS));
    }

    /**
     * Test method for {@link LanguageEventData#getOutputs()}.
     */
    @Test
    public void testOutputs() {
        HashMap<String, Serializable> outputs = new HashMap<String, Serializable>() {

            private static final long serialVersionUID = 161841000262993977L;

            {
                put("output1", "str1");
                put("output2", 123L);
                put("output3", false);
            }
        };
        eventData.setOutputs(outputs);
        assertEquals(outputs, eventData.getOutputs());
        assertEquals(outputs, eventData.get(LanguageEventData.OUTPUTS));
    }

    /**
     * Test method for {@link LanguageEventData#getParallelLoopBoundExpression()}.
     */
    @Test
    public void testParallelLoopBoundExpression() {
        List<Serializable> parallelLoopBoundExpression = new ArrayList<>(Arrays.asList((Serializable) "a", "b", "c"));
        eventData.setParallelLoopBoundExpression(parallelLoopBoundExpression);
        assertEquals(parallelLoopBoundExpression, eventData.getParallelLoopBoundExpression());
        assertEquals(parallelLoopBoundExpression, eventData.get(LanguageEventData.BOUND_PARALLEL_LOOP_EXPRESSION));
    }
}
