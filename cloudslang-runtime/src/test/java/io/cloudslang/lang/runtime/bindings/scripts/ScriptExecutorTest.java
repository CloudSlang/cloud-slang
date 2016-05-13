package io.cloudslang.lang.runtime.bindings.scripts;

import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyStringMap;
import org.python.util.PythonInterpreter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ScriptExecutorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @InjectMocks
    private ScriptExecutor scriptExecutor = new ScriptExecutor();

    @Mock
    private PythonInterpreter pythonInterpreter;

    @Test
    public void testExecuteScript() throws Exception {
        String script = "pass";
        Map<String, Serializable> scriptInputValues = new HashMap<>();
        scriptInputValues.put("input1", "value1");
        scriptInputValues.put("input2", "value2");
        Map<Object, PyObject> scriptOutputValues = new HashMap<>();
        scriptOutputValues.put("output1", new PyString("value1"));
        scriptOutputValues.put("output2", new PyString("value2"));
        when(pythonInterpreter.getLocals()).thenReturn(new PyStringMap(scriptOutputValues));
        when(pythonInterpreter.get(eq("output1"))).thenReturn(new PyString("value1"));
        when(pythonInterpreter.get(eq("output2"))).thenReturn(new PyString("value2"));
        Map<String, Serializable> expectedScriptOutputs = new HashMap<>();
        expectedScriptOutputs.put("output1", "value1");
        expectedScriptOutputs.put("output2", "value2");

        Map<String, Serializable> outputs = scriptExecutor.executeScript(script, scriptInputValues);

        verify(pythonInterpreter).set("input1", "value1");
        verify(pythonInterpreter).set("input2", "value2");
        verify(pythonInterpreter).exec(script);
        Assert.assertEquals(expectedScriptOutputs, outputs);
    }

    @Test
    public void testExecuteScriptError() throws Exception {
        String script = "pass";
        doThrow(new RuntimeException("error from interpreter")).when(pythonInterpreter).exec(eq(script));

        exception.expect(RuntimeException.class);
        exception.expectMessage("error from interpreter");
        exception.expectMessage("Error executing python script");

        scriptExecutor.executeScript(script, new HashMap<String, Serializable>());
    }

}
