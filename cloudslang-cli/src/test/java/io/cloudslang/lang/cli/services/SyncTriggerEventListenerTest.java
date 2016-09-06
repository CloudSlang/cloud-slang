/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

package io.cloudslang.lang.cli.services;

import io.cloudslang.lang.runtime.events.LanguageEventData;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Date: 2/26/2015
 *
 * @author lesant
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SyncTriggerEventListenerTest.Config.class)
public class SyncTriggerEventListenerTest {

    public static final String EXEC_START_PATH = "0";
    public static final String FIRST_STEP_PATH = "0/1";

    public static final String RETURN_RESULT = "returnResult";
    public static final String ERROR_MESSAGE = "errorMessage";
    public static final String RESULT = "result";
    public static final String LONG_RESULT = "result that is too long will be abbreviated so that it does not affect CLI readability result that is too long will be abbreviated so that it does not affect CLI readability";
    public static final String ABBREVIATED_RESULT = "result that is too long will be abbreviated so that it does not affect CLI readability result tha...";

    LanguageEventData data;
    Map<String, Serializable> outputs, expectedFilteredOutputs, actualFilteredOutputs;

    @Before
    public void before() throws Exception {
        data = new LanguageEventData();
        outputs = new HashMap<>();
    }


    @Test
    public void testExtractOutputs() throws InterruptedException {
        outputs.put(RETURN_RESULT, RESULT);
        outputs.put(ERROR_MESSAGE, StringUtils.EMPTY);
        data.put(LanguageEventData.OUTPUTS, (Serializable) outputs);
        data.put(LanguageEventData.PATH, EXEC_START_PATH);

        expectedFilteredOutputs = new HashMap<>();
        expectedFilteredOutputs.put(RETURN_RESULT, RESULT);

        actualFilteredOutputs = SyncTriggerEventListener.extractNotEmptyOutputs(data);

        Assert.assertEquals("outputs different than expected", expectedFilteredOutputs, actualFilteredOutputs);
    }

    @Test
    public void testExtractStepOutputs() throws InterruptedException {
        outputs.put(RETURN_RESULT, RESULT);
        outputs.put(ERROR_MESSAGE, StringUtils.EMPTY);
        data.put(LanguageEventData.OUTPUTS, (Serializable) outputs);
        data.put(LanguageEventData.STEP_TYPE, LanguageEventData.StepType.STEP);

        expectedFilteredOutputs = new HashMap<>();
        expectedFilteredOutputs.put(RETURN_RESULT, RESULT);

        actualFilteredOutputs = SyncTriggerEventListener.extractNotEmptyOutputs(data);

        Assert.assertEquals("outputs different than expected", expectedFilteredOutputs, actualFilteredOutputs);
    }

    @Test
    public void testExtractStepOutputsEmpty() throws InterruptedException {
        outputs.put(ERROR_MESSAGE, StringUtils.EMPTY);
        data.put(LanguageEventData.OUTPUTS, (Serializable) outputs);
        data.put(LanguageEventData.STEP_TYPE, LanguageEventData.StepType.STEP);

        actualFilteredOutputs = SyncTriggerEventListener.extractNotEmptyOutputs(data);

        Assert.assertTrue("outputs different than expected", MapUtils.isEmpty(actualFilteredOutputs));
    }

    @Test
    public void testExtractOutputsAbbreviated() throws InterruptedException {
        outputs.put(RETURN_RESULT, LONG_RESULT);
        data.put(LanguageEventData.OUTPUTS, (Serializable) outputs);
        data.put(LanguageEventData.PATH, EXEC_START_PATH);

        expectedFilteredOutputs = new HashMap<>();
        expectedFilteredOutputs.put(RETURN_RESULT, ABBREVIATED_RESULT);

        actualFilteredOutputs = SyncTriggerEventListener.extractNotEmptyOutputs(data);

        Assert.assertEquals("outputs different than expected", expectedFilteredOutputs, actualFilteredOutputs);
    }

    @Test
    public void testExtractOutputsEmpty() throws InterruptedException {
        outputs.put(ERROR_MESSAGE, StringUtils.EMPTY);
        data.put(LanguageEventData.OUTPUTS, (Serializable) outputs);
        data.put(LanguageEventData.PATH, EXEC_START_PATH);

        actualFilteredOutputs = SyncTriggerEventListener.extractNotEmptyOutputs(data);

        Assert.assertTrue("outputs different than expected", MapUtils.isEmpty(actualFilteredOutputs));
    }

    @Test
    public void testExtractNotEmptyOutputs() throws InterruptedException {
        outputs.put(ERROR_MESSAGE, StringUtils.EMPTY);
        data.put(LanguageEventData.OUTPUTS, (Serializable) outputs);
        data.put(LanguageEventData.PATH, EXEC_START_PATH);

        actualFilteredOutputs = SyncTriggerEventListener.extractNotEmptyOutputs(data);

        Assert.assertTrue("outputs different than expected", MapUtils.isEmpty(actualFilteredOutputs));
    }

    @Configuration
    static class Config {

        @Bean
        public SyncTriggerEventListener scoreEventListener() {
            return new SyncTriggerEventListener();
        }

    }
}
