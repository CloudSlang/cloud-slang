/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.lang.runtime.steps;

import com.hp.oo.sdk.content.plugin.GlobalSessionObject;
import com.hp.oo.sdk.content.plugin.SerializableSessionObject;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptExecutor;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.lang.runtime.env.ReturnValues;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import org.junit.Assert;
import io.cloudslang.score.api.execution.ExecutionParametersConsts;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static io.cloudslang.lang.entities.ActionType.JAVA;
import static io.cloudslang.lang.entities.ActionType.PYTHON;
import static org.mockito.Mockito.mock;

/**
 * Date: 10/31/2014
 *
 * @author Bonczidai Levente
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ActionStepsTest.Config.class)
public class ActionStepsTest {

    private static final long DEFAULT_TIMEOUT = 10000;
    private static final String NON_SERIALIZABLE_VARIABLE_NAME = "current_time";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    private ActionSteps actionSteps;

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
		actionSteps.doAction(runEnv, nonSerializableExecutionData, JAVA, ContentTestActions.class.getName(), "doJavaSampleAction", executionRuntimeServicesMock, null, 2L);

        //construct expected outputs
        Map<String, String> expectedOutputs = new HashMap<>();
        expectedOutputs.put("name", "nameTest");
        expectedOutputs.put("role", "roleTest");

        //extract actual outputs
        ReturnValues actualReturnValues = runEnv.removeReturnValues();
        Map<String, Serializable> actualOutputs = actualReturnValues.getOutputs();

        //verify matching
        Assert.assertEquals("Java action outputs are not as expected", expectedOutputs, actualOutputs);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void doActionSetNextPositionTest() {
        //prepare doAction arguments
        RunEnvironment runEnv = new RunEnvironment();
        Map<String, Serializable> initialCallArguments = new HashMap<>();
        initialCallArguments.put("name", "nameTest");
        initialCallArguments.put("role", "roleTest");
        runEnv.putCallArguments(initialCallArguments);

        Long nextStepPosition = 2L;

        //invoke doAction
        actionSteps.doAction(runEnv, new HashMap<String, Object>(), JAVA, ContentTestActions.class.getName(), "doJavaSampleAction", executionRuntimeServicesMock, null, nextStepPosition);

        //verify matching
        Assert.assertEquals(nextStepPosition, runEnv.removeNextStepPosition());
    }

    @Test(expected = RuntimeException.class, timeout = DEFAULT_TIMEOUT)
    public void doJavaActionWrongClassTest() {
        //prepare doAction arguments
        RunEnvironment runEnv = new RunEnvironment();
        ExecutionRuntimeServices runtimeServices = new ExecutionRuntimeServices();

        //invoke doAction
        actionSteps.doAction(runEnv, new HashMap<String, Object>(), JAVA, "MissingClassName", "doJavaSampleAction", runtimeServices, null, 2L);

        Collection<ScoreEvent> events = runtimeServices.getEvents();

        Assert.assertFalse(events.isEmpty());
        ScoreEvent actionErrorEvent = null;
        ScoreEvent actionEndEvent = null;
        for(ScoreEvent event:events){
            if(event.getEventType().equals(ScoreLangConstants.EVENT_ACTION_ERROR)){
                actionErrorEvent = event;
            } else if(event.getEventType().equals(ScoreLangConstants.EVENT_ACTION_END)){
                actionEndEvent = event;
            }
        }
        Assert.assertNotNull(actionErrorEvent);
        Assert.assertNotNull(actionEndEvent);
    }

    @Test
    public void doActionPythonActionCheckCallArgumentsOnEvent() {
        //prepare doAction arguments
        RunEnvironment runEnv = new RunEnvironment();
        ExecutionRuntimeServices runtimeServices = new ExecutionRuntimeServices();

        Map<String, Serializable> callArguments = new HashMap<>();
        callArguments.put("index", 1);
        runEnv.putCallArguments(callArguments);

        String userPythonScript = "var= \"hello\"";

        //invoke doAction
        actionSteps.doAction(runEnv,new HashMap<String, Object>(), PYTHON, "", "", runtimeServices, userPythonScript, 2L);

        Collection<ScoreEvent> events = runtimeServices.getEvents();

        Assert.assertFalse(events.isEmpty());
        ScoreEvent eventActionStart = null;
        for(ScoreEvent event:events){
            if(event.getEventType().equals(ScoreLangConstants.EVENT_ACTION_START)){
                eventActionStart = event;
                break;
            }
        }

        Assert.assertNotNull(eventActionStart);
        LanguageEventData data = (LanguageEventData)eventActionStart.getData();
        Map<String, Serializable> actualCallArguments = data.getCallArguments();
        Assert.assertEquals("Python action call arguments are not as expected", callArguments, actualCallArguments);
    }

    @Test
    public void doActionJavaActionCheckCallArgumentsOnEvent() {
        //prepare doAction arguments
        RunEnvironment runEnv = new RunEnvironment();
        ExecutionRuntimeServices runtimeServices = new ExecutionRuntimeServices();

        Map<String, Serializable> callArguments = new HashMap<>();
        callArguments.put("index", 1);
        runEnv.putCallArguments(callArguments);

        //invoke doAction
        actionSteps.doAction(runEnv, new HashMap<String, Object>(), JAVA, ContentTestActions.class.getName(), "doJavaSampleAction", runtimeServices, null, 2L);

        Collection<ScoreEvent> events = runtimeServices.getEvents();

        Assert.assertFalse(events.isEmpty());
        ScoreEvent eventActionStart = null;
        for(ScoreEvent event:events){
            if(event.getEventType().equals(ScoreLangConstants.EVENT_ACTION_START)){
                eventActionStart = event;
                break;
            }
        }

        Assert.assertNotNull(eventActionStart);
        LanguageEventData data = (LanguageEventData)eventActionStart.getData();
        Map<String, Serializable> actualCallArguments = data.getCallArguments();
        Assert.assertEquals("Java action call arguments are not as expected", callArguments, actualCallArguments);
    }

    @Test(expected = RuntimeException.class, timeout = DEFAULT_TIMEOUT)
    public void doJavaActionWrongMethodTest() {
        //prepare doAction arguments
        RunEnvironment runEnv = new RunEnvironment();
        ExecutionRuntimeServices runtimeServices = new ExecutionRuntimeServices();

        //invoke doAction
        actionSteps.doAction(runEnv, new HashMap<String, Object>(), JAVA, ContentTestActions.class.getName(), "wrongMethodName", runtimeServices, null, 2L);

        Collection<ScoreEvent> events = runtimeServices.getEvents();

        Assert.assertFalse(events.isEmpty());
        ScoreEvent actionErrorEvent = null;
        ScoreEvent actionEndEvent = null;
        for(ScoreEvent event:events){
            if(event.getEventType().equals(ScoreLangConstants.EVENT_ACTION_ERROR)){
                actionErrorEvent = event;
            } else if(event.getEventType().equals(ScoreLangConstants.EVENT_ACTION_END)){
                actionEndEvent = event;
            }
        }
        Assert.assertNotNull(actionErrorEvent);
        Assert.assertNotNull(actionEndEvent);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void doJavaActionWithExceptionTest() {
        //prepare doAction arguments
        RunEnvironment runEnv = new RunEnvironment();
        ExecutionRuntimeServices runtimeServices = new ExecutionRuntimeServices();

        boolean exceptionThrown = false;
        try {
            //invoke doAction
            actionSteps.doAction(runEnv, new HashMap<String, Object>(), JAVA, ContentTestActions.class.getName(), "doJavaActionExceptionMethod", runtimeServices, null, 2L);
        } catch (RuntimeException ex){
            exceptionThrown = true;
        }

        Assert.assertTrue(exceptionThrown);

        Collection<ScoreEvent> events = runtimeServices.getEvents();

        Assert.assertFalse(events.isEmpty());
        ScoreEvent actionErrorEvent = null;
        ScoreEvent actionEndEvent = null;
        for(ScoreEvent event:events){
            if(event.getEventType().equals(ScoreLangConstants.EVENT_ACTION_ERROR)){
                actionErrorEvent = event;
            } else if(event.getEventType().equals(ScoreLangConstants.EVENT_ACTION_END)){
                actionEndEvent = event;
            }
        }
        Assert.assertNotNull(actionErrorEvent);
        Assert.assertNull(actionEndEvent);
    }

    @Test(expected = RuntimeException.class, timeout = DEFAULT_TIMEOUT)
    public void doJavaActionWrongReturnTypeTest() {
        //prepare doAction arguments
        RunEnvironment runEnv = new RunEnvironment();
        ExecutionRuntimeServices runtimeServices = new ExecutionRuntimeServices();

        //invoke doAction
        actionSteps.doAction(runEnv, new HashMap<String, Object>(), JAVA, ContentTestActions.class.getName(), "doJavaActionWrongReturnType", runtimeServices, null, 2L);

        Collection<ScoreEvent> events = runtimeServices.getEvents();

        Assert.assertFalse(events.isEmpty());
        ScoreEvent actionErrorEvent = null;
        ScoreEvent actionEndEvent = null;
        for(ScoreEvent event:events){
            if(event.getEventType().equals(ScoreLangConstants.EVENT_ACTION_ERROR)){
                actionErrorEvent = event;
            } else if(event.getEventType().equals(ScoreLangConstants.EVENT_ACTION_END)){
                actionEndEvent = event;
            }
        }
        Assert.assertNotNull(actionErrorEvent);
        Assert.assertNotNull(actionEndEvent);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void doActionJavaMissingInputTest() {
        //prepare doAction arguments
        RunEnvironment runEnv = new RunEnvironment();
        Map<String, Serializable> initialCallArguments = new HashMap<>();
        initialCallArguments.put("name", "nameTest");
        // missing role
        runEnv.putCallArguments(initialCallArguments);

        Map<String, Object> nonSerializableExecutionData = new HashMap<>();

        //invoke doAction
        actionSteps.doAction(runEnv, nonSerializableExecutionData, JAVA, ContentTestActions.class.getName(), "doJavaSampleAction", executionRuntimeServicesMock, null, 2L);

        //construct expected outputs
        Map<String, String> expectedOutputs = new HashMap<>();
        expectedOutputs.put("name", "nameTest");
        expectedOutputs.put("role", null);

        //extract actual outputs
        ReturnValues actualReturnValues = runEnv.removeReturnValues();
        Map<String, Serializable> actualOutputs = actualReturnValues.getOutputs();

        //verify matching
        Assert.assertEquals("Java action outputs are not as expected", expectedOutputs, actualOutputs);
    }

    @Test(expected = RuntimeException.class, timeout = DEFAULT_TIMEOUT)
    public void doJavaActionParameterTypeMismatch() {
        //prepare doAction arguments
        RunEnvironment runEnv = new RunEnvironment();
        Map<String, Serializable> initialCallArguments = new HashMap<>();
        initialCallArguments.put("port", 5); //should be string
        runEnv.putCallArguments(initialCallArguments);

        Map<String, Object> nonSerializableExecutionData = new HashMap<>();

        //invoke doAction
        actionSteps.doAction(runEnv, nonSerializableExecutionData, JAVA, ContentTestActions.class.getName(), "doJavaNumberAsString", executionRuntimeServicesMock, null, 2L);
    }

    @Test
    public void doJavaActionParameterAndReturnTypeInteger() {
        //prepare doAction arguments
        RunEnvironment runEnv = new RunEnvironment();
        Map<String, Serializable> initialCallArguments = new HashMap<>();
        initialCallArguments.put("port", 5);
        runEnv.putCallArguments(initialCallArguments);

        Map<String, Object> nonSerializableExecutionData = new HashMap<>();

        //invoke doAction
        actionSteps.doAction(runEnv, nonSerializableExecutionData, JAVA, ContentTestActions.class.getName(), "doJavaNumbersAction", executionRuntimeServicesMock, null, 2L);
        ReturnValues returnValues = runEnv.removeReturnValues();
        Assert.assertEquals(5, returnValues.getOutputs()
                .get("port"));
    }

    @Test(expected = RuntimeException.class, timeout = DEFAULT_TIMEOUT)
    public void doActionJavaMissingActionTest() {
        //prepare doAction arguments
        RunEnvironment runEnv = new RunEnvironment();
        Map<String, Serializable> initialCallArguments = new HashMap<>();
        initialCallArguments.put("name", "nameTest");
        initialCallArguments.put("role", "roleTest");
        runEnv.putCallArguments(initialCallArguments);

        Map<String, Object> nonSerializableExecutionData = new HashMap<>();

        //invoke doAction
        actionSteps.doAction(runEnv, nonSerializableExecutionData, JAVA, ContentTestActions.class.getName(), "doJavaSampleAction_NOT_FOUND", executionRuntimeServicesMock, null, 2L);

        //construct expected outputs
        Map<String, String> expectedOutputs = new HashMap<>();

        //extract actual outputs
        ReturnValues actualReturnValues = runEnv.removeReturnValues();
        Map<String, Serializable> actualOutputs = actualReturnValues.getOutputs();

        //verify matching
        Assert.assertEquals("Java action output should be empty map", expectedOutputs, actualOutputs);
    }

    @Test(expected = RuntimeException.class, timeout = DEFAULT_TIMEOUT)
    public void doActionJavaMissingActionAnnotationTest() {
        //prepare doAction arguments
        RunEnvironment runEnv = new RunEnvironment();
        Map<String, Serializable> initialCallArguments = new HashMap<>();
        initialCallArguments.put("name", "nameTest");
        initialCallArguments.put("role", "roleTest");
        runEnv.putCallArguments(initialCallArguments);

        Map<String, Object> nonSerializableExecutionData = new HashMap<>();

        //invoke doAction
        actionSteps.doAction(runEnv, nonSerializableExecutionData, JAVA, ContentTestActions.class.getName(), "doJavaActionMissingAnnotation", executionRuntimeServicesMock, null, 2L);

        //construct expected outputs
        Map<String, String> expectedOutputs = new HashMap<>();

        //extract actual outputs
        ReturnValues actualReturnValues = runEnv.removeReturnValues();
        Map<String, Serializable> actualOutputs = actualReturnValues.getOutputs();

        //verify matching
        Assert.assertEquals("Java action output should be empty map", expectedOutputs, actualOutputs);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void doJavaActionGetKeyFromNonSerializableSessionTest() {
        //prepare doAction arguments
        RunEnvironment runEnv = new RunEnvironment();
        HashMap<String, Object> nonSerializableExecutionData = new HashMap<>();
        GlobalSessionObject<ContentTestActions.NonSerializableObject> sessionObject = new GlobalSessionObject<>();
        ContentTestActions.NonSerializableObject employee = new ContentTestActions.NonSerializableObject("John");
        sessionObject.setResource(new ContentTestActions.NonSerializableSessionResource(employee));
        nonSerializableExecutionData.put("name", sessionObject);

        //invoke doAction
        actionSteps.doAction(runEnv, nonSerializableExecutionData, JAVA, ContentTestActions.class.getName(),
                "getNameFromNonSerializableSession", executionRuntimeServicesMock, null, 2L);

        Map<String, Serializable> outputs = runEnv.removeReturnValues().getOutputs();
        Assert.assertTrue(outputs.containsKey("name"));
        Assert.assertEquals("John", outputs.get("name"));
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void doJavaActionSetKeyOnNonSerializableSessionTest() {
        //prepare doAction arguments
        RunEnvironment runEnv = new RunEnvironment();
        HashMap<String, Object> nonSerializableExecutionData = new HashMap<>();
        GlobalSessionObject<ContentTestActions.NonSerializableObject> sessionObject = new GlobalSessionObject<>();
        ContentTestActions.NonSerializableObject employee = new ContentTestActions.NonSerializableObject("John");
        sessionObject.setResource(new ContentTestActions.NonSerializableSessionResource(employee));
        nonSerializableExecutionData.put("name", sessionObject);
        Map<String, Serializable> initialCallArguments = new HashMap<>();
        initialCallArguments.put("value", "David");
        runEnv.putCallArguments(initialCallArguments);

        //invoke doAction
        actionSteps.doAction(runEnv, nonSerializableExecutionData, JAVA, ContentTestActions.class.getName(),
                "setNameOnNonSerializableSession", executionRuntimeServicesMock, null, 2L);

        Map<String, Serializable> outputs = runEnv.removeReturnValues().getOutputs();
        Assert.assertTrue(outputs.containsKey("name"));
        Assert.assertEquals("David", outputs.get("name"));
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void doJavaActionGetNonExistingKeyFromNonSerializableSessionTest() {
        //prepare doAction arguments
        RunEnvironment runEnv = new RunEnvironment();
        HashMap<String, Object> nonSerializableExecutionData = new HashMap<>();

        //invoke doAction
        actionSteps.doAction(runEnv, nonSerializableExecutionData, JAVA, ContentTestActions.class.getName(),
                "getNameFromNonSerializableSession", executionRuntimeServicesMock, null, 2L);

        Map<String, Serializable> outputs = runEnv.removeReturnValues().getOutputs();
        Assert.assertTrue(outputs.containsKey("name"));
        Assert.assertNull(outputs.get("name"));
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void doJavaActionGetKeyFromSerializableSessionTest() {
        //prepare doAction arguments
        RunEnvironment runEnv = new RunEnvironment();
        Map<String, Serializable> initialCallArguments = new HashMap<>();
        HashMap<String, SerializableSessionObject> serializableExecutionData = new HashMap<>();
        SerializableSessionObject sessionObject = new SerializableSessionObject();
        sessionObject.setName("John");
        serializableExecutionData.put("name", sessionObject);
        runEnv.getSerializableDataMap().putAll(serializableExecutionData);
        runEnv.putCallArguments(initialCallArguments);

        //invoke doAction
        actionSteps.doAction(runEnv, new HashMap<String, Object>(), JAVA, ContentTestActions.class.getName(),
                "getNameFromSerializableSession", executionRuntimeServicesMock, null, 2L);

        Map<String, Serializable> outputs = runEnv.removeReturnValues().getOutputs();
        Assert.assertTrue(outputs.containsKey("name"));
        Assert.assertEquals("John", outputs.get("name"));
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void doJavaActionGetNonExistingKeyFromSerializableSessionTest() {
        //prepare doAction arguments
        RunEnvironment runEnv = new RunEnvironment();
        Map<String, Serializable> initialCallArguments = new HashMap<>();
        HashMap<String, Object> serializableExecutionData = new HashMap<>();
        initialCallArguments.put(ExecutionParametersConsts.SERIALIZABLE_SESSION_CONTEXT, serializableExecutionData);
        runEnv.putCallArguments(initialCallArguments);

        //invoke doAction
        actionSteps.doAction(runEnv, new HashMap<String, Object>(), JAVA, ContentTestActions.class.getName(),
                "getNameFromSerializableSession", executionRuntimeServicesMock, null, 2L);

        Map<String, Serializable> outputs = runEnv.removeReturnValues().getOutputs();
        Assert.assertTrue(outputs.containsKey("name"));
        Assert.assertNull(outputs.get("name"));
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void doJavaActionGetNonExistingKeyFromNonExistingSerializableSessionTest() {
        //prepare doAction arguments
        RunEnvironment runEnv = new RunEnvironment();
        Map<String, Serializable> initialCallArguments = new HashMap<>();
        runEnv.putCallArguments(initialCallArguments);

        //invoke doAction
        actionSteps.doAction(runEnv, new HashMap<String, Object>(), JAVA, ContentTestActions.class.getName(),
                "getNameFromSerializableSession", executionRuntimeServicesMock, null, 2L);

        Map<String, SerializableSessionObject> serializableSessionMap = runEnv.getSerializableDataMap();
        Assert.assertTrue(serializableSessionMap.containsKey("name"));
    }

    @Test
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
                "condition = 1==1\n" +
                "print url";

        //invoke doAction
        actionSteps.doAction(runEnv, nonSerializableExecutionData, PYTHON, "", "", executionRuntimeServicesMock, userPythonScript, 2L);

        //construct expected outputs
        Map<String, Serializable> expectedOutputs = new HashMap<>();
        expectedOutputs.put("host", "localhost");
        expectedOutputs.put("port", 8081);
        expectedOutputs.put("url", "http://localhost:8080");
        expectedOutputs.put("url2", "http://localhost:8080/oo");
        expectedOutputs.put("another", "just a string");
        expectedOutputs.put("condition", true);

        //extract actual outputs
        ReturnValues actualReturnValues = runEnv.removeReturnValues();
        Map<String, Serializable> actualOutputs = actualReturnValues.getOutputs();

        //verify matching
        Assert.assertEquals("Python action outputs are not as expected", expectedOutputs, actualOutputs);
    }

    @Test
    public void doActionInvalidReturnTypes() throws IOException {
        //prepare doAction arguments
        RunEnvironment runEnv = new RunEnvironment();
        File file = folder.newFile();

        String fileAbsolutePathEscaped = file.getAbsolutePath().replace("\\", "\\\\");

        String userPythonScript =
                "valid = 1\n" +
                "with open('" + fileAbsolutePathEscaped + "', 'r') as f:\n" +
                "  f.close()\n\n" +
                "import sys\n" +
                "import io\n" +
                "def a():\n" +
                "  print 'a'\n\n";

        //invoke doAction
        actionSteps.doAction(runEnv, new HashMap<String, Object>(), PYTHON, "", "", executionRuntimeServicesMock, userPythonScript, 2L);

        //extract actual outputs
        ReturnValues actualReturnValues = runEnv.removeReturnValues();
        Map<String, Serializable> actualOutputs = actualReturnValues.getOutputs();

        //verify matching
        Assert.assertEquals("Invalid types passed exclusion",
                1, actualOutputs.size());
    }

    @Test (expected = RuntimeException.class, timeout = DEFAULT_TIMEOUT)
    public void doActionPythonMissingInputsTest() {
        //prepare doAction arguments
        RunEnvironment runEnv = new RunEnvironment();
        //missing inputs
        Map<String, Serializable> initialCallArguments = new HashMap<>();
        runEnv.putCallArguments(initialCallArguments);

        Map<String, Object> nonSerializableExecutionData = new HashMap<>();

        String userPythonScript = "import os\n" +
                "print host\n" +
                "print port\n" +
                "os.system(\"ping -c 1 \" + host)\n" +
                "url = 'http://' + host + ':' + str(port)\n" +
                "url2 = url + '/oo'\n" +
                "another = 'just a string'\n" +
                "port = 8081\n" +
                "print url";

        //invoke doAction
        actionSteps.doAction(runEnv, nonSerializableExecutionData, PYTHON, "", "", executionRuntimeServicesMock, userPythonScript, 2L);
    }


    @Test
    public void doActionPythonImportRightIOPackage() {
        //prepare doAction arguments
        RunEnvironment runEnv = new RunEnvironment();

        String userPythonScript =
                "import io\n" +
                "if 'StringIO' not in dir(io):\n" +
                "  raise Exception('cant find StringIO')";

        //invoke doAction
        actionSteps.doAction(
                runEnv, new HashMap<String, Object>(), PYTHON, "", "",
                executionRuntimeServicesMock, userPythonScript, 2L);
    }

    @Test (expected = RuntimeException.class, timeout = DEFAULT_TIMEOUT)
    public void doActionPythonInputTypeMismatchTest() {
        //prepare doAction arguments
        RunEnvironment runEnv = new RunEnvironment();
        //missing inputs
        Map<String, Serializable> initialCallArguments = new HashMap<>();
        initialCallArguments.put("port", 8080); //should be string
        runEnv.putCallArguments(initialCallArguments);

        Map<String, Object> nonSerializableExecutionData = new HashMap<>();

        //expects port as string
        String userPythonScript = "print('localhost:' + port)";

        //invoke doAction
        actionSteps.doAction(runEnv, nonSerializableExecutionData, PYTHON, "", "", executionRuntimeServicesMock, userPythonScript, 2L);
    }

    @Test (expected = RuntimeException.class, timeout = DEFAULT_TIMEOUT)
    public void doActionPythonEmptyScript() {
        //prepare doAction arguments
        RunEnvironment runEnv = new RunEnvironment();
        Map<String, Serializable> initialCallArguments = new HashMap<>();
        initialCallArguments.put("host", "localhost");
        initialCallArguments.put("port", "8080");
        runEnv.putCallArguments(initialCallArguments);

        Map<String, Object> nonSerializableExecutionData = new HashMap<>();

        //invoke doAction
        actionSteps.doAction(runEnv, nonSerializableExecutionData, PYTHON, "", "", executionRuntimeServicesMock, "", 2L);
    }

    @Test (expected = RuntimeException.class, timeout = DEFAULT_TIMEOUT)
    public void doActionPythonMissingScript() {
        //prepare doAction arguments
        RunEnvironment runEnv = new RunEnvironment();
        Map<String, Serializable> initialCallArguments = new HashMap<>();
        initialCallArguments.put("host", "localhost");
        initialCallArguments.put("port", "8080");
        runEnv.putCallArguments(initialCallArguments);

        Map<String, Object> nonSerializableExecutionData = new HashMap<>();

        //invoke doAction
        actionSteps.doAction(runEnv, nonSerializableExecutionData, PYTHON, "", "", executionRuntimeServicesMock, null, 2L);
    }

    @Test
    public void testPythonOutputSerializationErrorMessage() {
        RunEnvironment runEnv = new RunEnvironment();
        runEnv.putCallArguments(new HashMap<String, Serializable>());
        Map<String, Object> nonSerializableExecutionData = new HashMap<>();
        String userPythonScript =
                "from datetime import datetime\n" +
                NON_SERIALIZABLE_VARIABLE_NAME + " = datetime.utcnow()";

        exception.expect(RuntimeException.class);
        exception.expectMessage(NON_SERIALIZABLE_VARIABLE_NAME);
        exception.expectMessage("serializable");

        actionSteps.doAction(runEnv, nonSerializableExecutionData, PYTHON, "", "", executionRuntimeServicesMock, userPythonScript, 2L);
    }

    @Configuration
    static class Config {

        @Bean
        public ActionSteps actionSteps() {
            return new ActionSteps();
        }

        @Bean
        public ScriptExecutor scriptExecutor() {
            return new ScriptExecutor();
        }

        @Bean
        public PythonInterpreter execInterpreter() {
            return new PythonInterpreter();
        }

    }
}
