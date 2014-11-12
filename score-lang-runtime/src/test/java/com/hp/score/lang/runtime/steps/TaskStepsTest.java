package com.hp.score.lang.runtime.steps;

import com.google.common.collect.Lists;
import com.hp.score.events.ScoreEvent;
import com.hp.score.lang.ExecutionRuntimeServices;
import com.hp.score.lang.entities.bindings.Input;
import com.hp.score.lang.runtime.bindings.InputsBinding;
import com.hp.score.lang.runtime.bindings.OutputsBinding;
import com.hp.score.lang.runtime.bindings.ScriptEvaluator;
import com.hp.score.lang.runtime.env.RunEnvironment;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.script.ScriptEngine;
import java.io.Serializable;
import java.util.*;

import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_INPUT_END;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TaskStepsTest.Config.class)
public class TaskStepsTest {

    @Autowired
    private TaskSteps taskSteps;

    @Autowired
    private InputsBinding inputsBinding;

    @Autowired
    private OutputsBinding outputsBinding;

    @Test
    public void testBeginTaskEmptyInputs() throws Exception {
        RunEnvironment runEnv = new RunEnvironment();
        taskSteps.beginTask(new ArrayList<Input>(),runEnv,new ExecutionRuntimeServices());
        Map<String,Serializable> callArgs = runEnv.removeCallArguments();
        Assert.assertTrue(callArgs.isEmpty());
    }

    @Test
    public void testBeginTaskInputsEvents() throws Exception {
        RunEnvironment runEnv = new RunEnvironment();
        ExecutionRuntimeServices runtimeServices = new ExecutionRuntimeServices();
        List<Input> inputs = Lists.newArrayList(new Input("input1","input1"),new Input("input2","input2"));
        Map<String,Serializable> resultMap = new HashMap<>();
        resultMap.put("input1",5);
        resultMap.put("input2",3);

        when(inputsBinding.bindInputs(anyMap(),eq(inputs))).thenReturn(resultMap);

        taskSteps.beginTask(inputs,runEnv,runtimeServices);
        Map<String,Serializable> callArgs = runEnv.removeCallArguments();
        Assert.assertFalse(callArgs.isEmpty());
        Assert.assertEquals(5, callArgs.get("input1"));
        Assert.assertEquals(3,callArgs.get("input2"));

        Collection<ScoreEvent> events = runtimeServices.getEvents();
        Assert.assertEquals(1,events.size());
        ScoreEvent inputEvent = events.iterator().next();
        Assert.assertEquals(EVENT_INPUT_END,inputEvent.getEventType());
    }


    /*@Test
    public void testEndTask() throws Exception {

    }*/

    @Configuration
    static class Config{

        @Bean
        public InputsBinding inputsBinding(){
            return mock(InputsBinding.class);
        }

        @Bean
        public OutputsBinding outputsBinding() {
            return mock(OutputsBinding.class);
        }

        @Bean
        public ScriptEvaluator scriptEvaluator(){
            return mock(ScriptEvaluator.class);
        }

        @Bean
        public ScriptEngine scriptEngine(){
            return mock(ScriptEngine.class);
        }

        @Bean
        public TaskSteps taskSteps(){
            return new TaskSteps();
        }

    }
}