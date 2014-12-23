/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.lang.runtime.navigations;

import org.openscore.lang.runtime.env.RunEnvironment;
import junit.framework.Assert;
import org.openscore.events.ScoreEvent;
import org.openscore.lang.ExecutionRuntimeServices;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collection;

import static org.openscore.lang.entities.ScoreLangConstants.SLANG_EXECUTION_EXCEPTION;

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
        Long nextPosition = navigations.navigate(runEnv , new ExecutionRuntimeServices());

        Assert.assertEquals(nextStepId, nextPosition);
    }

    @Test
    public void errorNavigationTest() throws Exception {

        RunEnvironment runEnv = new RunEnvironment();
        Long nextStepId = 2L;
        runEnv.putNextStepPosition(nextStepId);
        ExecutionRuntimeServices runtimeServices = new ExecutionRuntimeServices();
        runtimeServices.setStepErrorKey("Error");
        Long nextPosition = navigations.navigate(runEnv , runtimeServices);

        Collection<ScoreEvent> events = runtimeServices.getEvents();

        Assert.assertNull(nextPosition);

        Assert.assertFalse(events.isEmpty());
        ScoreEvent stepErrorEvent = null;
        for(ScoreEvent event:events){
            if(event.getEventType().equals(SLANG_EXECUTION_EXCEPTION)){
                stepErrorEvent = event;
            }
        }
        Assert.assertNotNull(stepErrorEvent);
    }

    @Configuration
    static class Config{

        @Bean
        public Navigations navigations(){
            return new Navigations();
        }

    }
}
