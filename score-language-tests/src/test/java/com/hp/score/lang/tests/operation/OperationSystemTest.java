package com.hp.score.lang.tests.operation;
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

import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.Score;
import com.hp.score.api.TriggeringProperties;
import com.hp.score.events.EventBus;
import com.hp.score.events.EventConstants;
import com.hp.score.events.ScoreEvent;
import com.hp.score.events.ScoreEventListener;
import com.hp.score.lang.compiler.SlangCompiler;
import com.hp.score.lang.entities.ScoreLangConstants;
import com.hp.score.lang.runtime.env.RunEnvironment;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_ACTION_END;
import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_ACTION_ERROR;
import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_ACTION_START;
import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_INPUT_END;
import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_INPUT_START;
import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_OUTPUT_END;
import static com.hp.score.lang.entities.ScoreLangConstants.EVENT_OUTPUT_START;

/**
 * User: stoneo
 * Date: 11/11/2014
 * Time: 11:55
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/META-INF/spring/operationTestContext.xml")
public class OperationSystemTest {

    @Autowired
    private SlangCompiler compiler;

    @Autowired
    private EventBus eventBus;

    @Autowired
    private Score score;

    LinkedBlockingQueue<ScoreEvent> queue = new LinkedBlockingQueue<>();

    @Test
    public void testCompileAndRunOperationBasic() throws Exception {
        URL resource = getClass().getResource("/yaml/operation.yaml");
        ExecutionPlan executionPlan = compiler.compile(new File(resource.toURI()), "test_op", null).getExecutionPlan();
        //Trigger ExecutionPlan
        Map<String, Serializable> userInputs = new HashMap<>();
        ScoreEvent event = triggerOperation(executionPlan, userInputs);
        Assert.assertEquals(EventConstants.SCORE_FINISHED_EVENT, event.getEventType());
    }

    private ScoreEvent triggerOperation(ExecutionPlan executionPlan, Map<String, Serializable> userInputs) throws InterruptedException {
        Map<String, Serializable> executionContext = createExecutionContext(userInputs);

        TriggeringProperties triggeringProperties = TriggeringProperties
                .create(executionPlan)
                .setContext(executionContext);

        registerHandlers();
        score.trigger(triggeringProperties);
        ScoreEvent event;
        do {
            event = queue.take();
            System.out.println("Event received: " + event.getEventType() + " Data is: " + event.getData());
        } while(!EventConstants.SCORE_FINISHED_EVENT.equals(event.getEventType()));
        return event;
    }

    @Test
    public void testCompileAndRunOperationWithData() throws Exception {
        URL resource = getClass().getResource("/yaml/operation_with_data.yaml");
        ExecutionPlan executionPlan = compiler.compile(new File(resource.toURI()), "test_op_2", null).getExecutionPlan();
        //Trigger ExecutionPlan
        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("input1", "value1");
        userInputs.put("input2", "value2");
        userInputs.put("input4", "value4");
        userInputs.put("input5", "value5");
        //not supposed to be supplied
        userInputs.put("input6", "value6");
        ScoreEvent event = triggerOperation(executionPlan, userInputs);
        Assert.assertEquals(EventConstants.SCORE_FINISHED_EVENT, event.getEventType());
    }

    private static Map<String, Serializable> createExecutionContext(Map<String, Serializable> userInputs) {
        Map<String, Serializable> executionContext = new HashMap<>();
        executionContext.put(ScoreLangConstants.RUN_ENV, new RunEnvironment());
        executionContext.put(ScoreLangConstants.USER_INPUTS_KEY, (Serializable) userInputs);
        return executionContext;
    }

    private void registerHandlers() {
        Set<String> handlerTypes = new HashSet<>();
        handlerTypes.add(EventConstants.SCORE_FINISHED_EVENT);
        handlerTypes.add(EventConstants.SCORE_ERROR_EVENT);
        handlerTypes.add(EventConstants.SCORE_FAILURE_EVENT);
        handlerTypes.add(EVENT_ACTION_START);
        handlerTypes.add(EVENT_ACTION_END);
        handlerTypes.add(EVENT_ACTION_ERROR);
        handlerTypes.add(EVENT_INPUT_START);
        handlerTypes.add(EVENT_INPUT_END);
        handlerTypes.add(EVENT_OUTPUT_START);
        handlerTypes.add(EVENT_OUTPUT_END);
        eventBus.subscribe(new ScoreEventListener() {

            @Override
            public void onEvent(ScoreEvent event) {
                try {
                    queue.put(event);
                } catch(InterruptedException ignore) {}
            }
        }, handlerTypes);
    }}
