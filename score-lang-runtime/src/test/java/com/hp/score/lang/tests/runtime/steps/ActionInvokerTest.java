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
package com.hp.score.lang.tests.runtime.steps;

import com.hp.oo.sdk.content.annotations.Param;
import com.hp.score.lang.ExecutionRuntimeServices;
import com.hp.score.lang.runtime.env.ReturnValues;
import com.hp.score.lang.runtime.env.RunEnvironment;
import com.hp.score.lang.runtime.steps.ActionSteps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.hp.score.lang.entities.ActionType.JAVA;
import static com.hp.score.lang.entities.ActionType.PYTHON;
import static com.hp.score.lang.entities.ScoreLangConstants.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Date: 10/31/2014
 *
 * @author Bonczidai Levente
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ActionInvokerTest.Config.class)
public class ActionInvokerTest {

    private static final long DEFAULT_TIMEOUT = 10000;

    @Autowired
    private ActionSteps actionInvoker;
	ExecutionRuntimeServices executionRuntimeServicesMock = mock(ExecutionRuntimeServices.class);

    @Test(timeout = DEFAULT_TIMEOUT)
    public void doActionJavaTest() {
        //prepare doAction arguments
        RunEnvironment runEnv = new RunEnvironment();
        Map<String, Serializable> initialCallArguments = new HashMap<>();
        initialCallArguments.put("name", "nameTest");
        initialCallArguments.put("role", "roleTest");
        runEnv.putCallArguments(initialCallArguments);

        Map<String, Object> nonSerializableExecutionData = new HashMap<>();

        //invoke doAction
		actionInvoker.doAction(runEnv, nonSerializableExecutionData, JAVA, ActionInvokerTest.class.getName(), "doJavaAction", executionRuntimeServicesMock, null);

        //construct expected outputs
        Map<String, String> expectedOutputs = new HashMap<>();
        expectedOutputs.put("name", "nameTest");
        expectedOutputs.put("role", "roleTest");

        //extract actual outputs
        ReturnValues actualReturnValues = runEnv.removeReturnValues();
        Map<String, String> actualOutputs = actualReturnValues.getOutputs();

        //verify matching
        assertEquals("Java action outputs are not as expected", expectedOutputs, actualOutputs);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void doActionPythonTest() {
        //prepare doAction arguments
        RunEnvironment runEnv = new RunEnvironment();
        Map<String, Serializable> initialCallArguments = new HashMap<>();
        initialCallArguments.put("host", "localhost");
        initialCallArguments.put("port", "8080");
        runEnv.putCallArguments(initialCallArguments);

        Map<String, Object> nonSerializableExecutionData = new HashMap<>();

        String userPythonScript = "import os\n" +
                "print host\n" +
                "print port\n" +
                "os.system(\"ping -c 1 \" + host)\n" +
                "url = 'http://' + host + ':' + str(port)\n" +
                "url2 = url + '/oo'\n" +
                "another = 'just a string'\n" +
//                we can also change..
                "port = 8081\n" +
                "print url";

        //invoke doAction
        actionInvoker.doAction(runEnv, nonSerializableExecutionData, PYTHON, "", "", executionRuntimeServicesMock, userPythonScript);

        //construct expected outputs
        Map<String, String> expectedOutputs = new HashMap<>();
        expectedOutputs.put("host", "localhost");
        expectedOutputs.put("port", "8081");
        expectedOutputs.put("url", "http://localhost:8080");
        expectedOutputs.put("url2", "http://localhost:8080/oo");
        expectedOutputs.put("another", "just a string");

        //extract actual outputs
        ReturnValues actualReturnValues = runEnv.removeReturnValues();
        Map<String, String> actualOutputs = actualReturnValues.getOutputs();

        //verify matching
        assertEquals("Python action outputs are not as expected", expectedOutputs, actualOutputs);
    }

    @SuppressWarnings("unused")
    public Map<String, String> doJavaAction(@Param("name") String name,
                                            @Param("role") String role) {
        Map<String, String> returnValues = new HashMap<>();
        returnValues.put("name", name);
        returnValues.put("role", role);
        return returnValues;
    }

    static class Config {

        @Bean
        public ActionSteps actionSteps() {
            return new ActionSteps();
        }

        @Bean
        public PythonInterpreter pythonInterpreter() {
            return new PythonInterpreter();
        }

    }
}
