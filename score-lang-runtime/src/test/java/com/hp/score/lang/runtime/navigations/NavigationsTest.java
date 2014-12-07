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
package com.hp.score.lang.runtime.navigations;

import com.hp.score.lang.runtime.env.RunEnvironment;
import junit.framework.Assert;
import org.eclipse.score.events.ScoreEvent;
import org.eclipse.score.lang.ExecutionRuntimeServices;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collection;

import static com.hp.score.lang.entities.ScoreLangConstants.SLANG_EXECUTION_EXCEPTION;

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
