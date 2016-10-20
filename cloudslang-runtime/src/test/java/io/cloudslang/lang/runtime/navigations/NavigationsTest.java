/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.navigations;

import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.lang.ExecutionRuntimeServices;

import java.util.Collection;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * User: stoneo
 * Date: 17/11/2014
 * Time: 10:48
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = NavigationsTest.Config.class)
public class NavigationsTest {

    @Autowired
    private Navigations navigations;


    @Test
    public void simpleNavigateTest() throws Exception {

        RunEnvironment runEnv = new RunEnvironment();
        Long nextStepId = 2L;
        runEnv.putNextStepPosition(nextStepId);
        Long nextPosition = navigations.navigate(runEnv, new ExecutionRuntimeServices());

        Assert.assertEquals(nextStepId, nextPosition);
    }

    @Test
    public void errorNavigationTest() throws Exception {

        RunEnvironment runEnv = new RunEnvironment();
        Long nextStepId = 2L;
        runEnv.putNextStepPosition(nextStepId);
        ExecutionRuntimeServices runtimeServices = new ExecutionRuntimeServices();
        runtimeServices.setStepErrorKey("Error");
        try {
            navigations.navigate(runEnv, runtimeServices);

        } catch (RuntimeException e) {
            Collection<ScoreEvent> events = runtimeServices.getEvents();

            Assert.assertFalse(events.isEmpty());
            ScoreEvent stepErrorEvent = null;
            for (ScoreEvent event : events) {
                if (event.getEventType().equals(ScoreLangConstants.SLANG_EXECUTION_EXCEPTION)) {
                    stepErrorEvent = event;
                }
            }
            Assert.assertNotNull(stepErrorEvent);
        }

    }

    @Configuration
    static class Config {

        @Bean
        public Navigations navigations() {
            return new Navigations();
        }

    }
}
