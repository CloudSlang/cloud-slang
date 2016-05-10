package io.cloudslang.lang.runtime.bindings.scripts;

import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.python.util.PythonInterpreter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        Map<String, Value> scriptInputValues = new HashMap<>();
        Value value1 = ValueFactory.create("value1");
        Value value2 = ValueFactory.create("value2");
        scriptInputValues.put("input1", value1);
        scriptInputValues.put("input2", value2);
        Map<Object, PyObject> scriptOutputValues = new HashMap<>();
        PyObject PyObjectValue1 = (PyObject)ValueFactory.createPyObjectValue("value1", false);
        PyObject PyObjectValue2 = (PyObject)ValueFactory.createPyObjectValue("value2", false);
        scriptOutputValues.put("output1", PyObjectValue1);
        scriptOutputValues.put("output2", PyObjectValue2);
        when(pythonInterpreter.getLocals()).thenReturn(new PyStringMap(scriptOutputValues));
        when(pythonInterpreter.get(eq("output1"))).thenReturn(PyObjectValue1);
        when(pythonInterpreter.get(eq("output2"))).thenReturn(PyObjectValue2);
        Map<String, Serializable> expectedScriptOutputs = new HashMap<>();
        expectedScriptOutputs.put("output1", value1);
        expectedScriptOutputs.put("output2", value2);

        Map<String, Value> outputs = scriptExecutor.executeScript(script, scriptInputValues);

        verify(pythonInterpreter).set(eq("input1"), eq((Value)PyObjectValue1));
        verify(pythonInterpreter).set(eq("input2"), eq((Value)PyObjectValue2));
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

        scriptExecutor.executeScript(script, new HashMap<String, Value>());
    }

}
