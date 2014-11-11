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
package com.hp.score.lang.runtime.systemtests;

import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.Score;
import com.hp.score.api.TriggeringProperties;
import com.hp.score.events.EventBus;
import com.hp.score.events.EventConstants;
import com.hp.score.events.ScoreEvent;
import com.hp.score.events.ScoreEventListener;
import com.hp.score.lang.runtime.env.RunEnvironment;
import com.hp.score.lang.entities.ScoreLangConstants;
import com.hp.score.lang.runtime.systemtests.builders.POCExecutionPlanActionsBuilder;
import com.hp.score.lang.runtime.systemtests.builders.POCParentExecutionPlanActionsBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import static com.hp.score.lang.entities.ScoreLangConstants.*;

/**
 * User: stoneo
 * Date: 06/10/2014
 * Time: 08:36
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:META-INF/spring/systemTestsContext.xml")
public class RuntimeTest {

    @Autowired
    private Score score;

    @Autowired
    private EventBus eventBus;

    LinkedBlockingQueue<ScoreEvent> queue = new LinkedBlockingQueue<>();

    public static final String USER_INPUTS = "userInputs";

    @Test
    public void testFlow() throws InterruptedException {

        //Parse YAML -> flow

        //Compile Flow -> ExecutionPlan
        POCExecutionPlanActionsBuilder builder = new POCExecutionPlanActionsBuilder();
        ExecutionPlan executionPlan = builder.getExecutionPlan();
        //Trigger ExecutionPlan
        Map<String, Serializable> executionContext = createExecutionContext();
        addUserInputs(executionContext);

        TriggeringProperties triggeringProperties = TriggeringProperties
                .create(executionPlan)
                .setContext(executionContext);
        registerHandlers();
        score.trigger(triggeringProperties);
        ScoreEvent event;
        do {
            event = queue.take();
            System.out.println("Event recieved: " + event);
        } while(!EventConstants.SCORE_FINISHED_EVENT.equals(event.getEventType()));
        Assert.assertEquals(EventConstants.SCORE_FINISHED_EVENT, event.getEventType());
    }

    @Test
    public void testSubFlow() throws InterruptedException {

        //Parse YAML -> flow

        //Compile Flow -> ExecutionPlan
        POCExecutionPlanActionsBuilder builder = new POCExecutionPlanActionsBuilder();
        ExecutionPlan executionPlan = builder.getExecutionPlan();

        POCParentExecutionPlanActionsBuilder parentBuilder = new POCParentExecutionPlanActionsBuilder();
        ExecutionPlan parentExecutionPlan = parentBuilder.getExecutionPlan();
        //Trigger ExecutionPlan
        Map<String, Serializable> executionContext = createExecutionContext();
        addUserInputs(executionContext);

        Map<String, ExecutionPlan> dependencies = new HashMap<>();
        dependencies.put("childFlow", executionPlan);
        TriggeringProperties triggeringProperties = TriggeringProperties
                .create(parentExecutionPlan)
                .setContext(executionContext)
                .setDependencies(dependencies);
        registerHandlers();
        score.trigger(triggeringProperties);
        ScoreEvent event;
        do {
            event = queue.take();
            System.out.println("Event recieved: " + event);
        } while(!EventConstants.SCORE_FINISHED_EVENT.equals(event.getEventType()));
        Assert.assertEquals(EventConstants.SCORE_FINISHED_EVENT, event.getEventType());
    }

	private static Map<String, Serializable> createExecutionContext() {
		Map<String, Serializable> executionContext = new HashMap<>();
		executionContext.put(ScoreLangConstants.RUN_ENV, new RunEnvironment());
		return executionContext;
	}

	private static void addUserInputs(Map<String, Serializable> executionContext) {
		HashMap<String, Serializable> userInputs = new HashMap<>();
		userInputs.put("name", "orit");
		userInputs.put("id", "123");
		executionContext.put(USER_INPUTS, userInputs);
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
	}

}
