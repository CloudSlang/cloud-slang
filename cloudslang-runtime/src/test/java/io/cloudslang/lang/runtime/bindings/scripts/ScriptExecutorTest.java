package io.cloudslang.lang.runtime.bindings.scripts;

import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.dependency.impl.services.DependencyServiceImpl;
import io.cloudslang.runtime.api.python.PythonRuntimeService;
import io.cloudslang.runtime.impl.python.PythonEvaluator;
import io.cloudslang.runtime.impl.python.PythonExecutor;
import io.cloudslang.runtime.impl.python.PythonRuntimeServiceImpl;
import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyStringMap;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ScriptExecutorTest.Config.class)
public class ScriptExecutorTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    private ScriptExecutor scriptExecutor;

    @Autowired
    private PythonInterpreter execInterpreter;

    @Test
    public void testExecuteScript() throws Exception {
        reset(execInterpreter);
        String script = "pass";
        Map<String, Serializable> scriptInputValues = new HashMap<>();
        scriptInputValues.put("input1", "value1");
        scriptInputValues.put("input2", "value2");
        Map<Object, PyObject> scriptOutputValues = new HashMap<>();
        scriptOutputValues.put("output1", new PyString("value1"));
        scriptOutputValues.put("output2", new PyString("value2"));
        when(execInterpreter.getLocals()).thenReturn(new PyStringMap(scriptOutputValues));
        when(execInterpreter.get(eq("output1"))).thenReturn(new PyString("value1"));
        when(execInterpreter.get(eq("output2"))).thenReturn(new PyString("value2"));
        Map<String, Serializable> expectedScriptOutputs = new HashMap<>();
        expectedScriptOutputs.put("output1", "value1");
        expectedScriptOutputs.put("output2", "value2");

        Map<String, Serializable> outputs = scriptExecutor.executeScript(script, scriptInputValues);

        verify(execInterpreter).set("input1", "value1");
        verify(execInterpreter).set("input2", "value2");
        verify(execInterpreter).exec(script);
        Assert.assertEquals(expectedScriptOutputs, outputs);
    }

    @Test
    public void testExecuteScriptError() throws Exception {
        reset(execInterpreter);
        String script = "pass";
        doThrow(new RuntimeException("error from interpreter")).when(execInterpreter).exec(eq(script));

        exception.expect(RuntimeException.class);
        exception.expectMessage("error from interpreter");
        exception.expectMessage("Error executing python script");

        scriptExecutor.executeScript(script, new HashMap<String, Serializable>());
    }

    @Configuration
    static class Config {
        @Bean
        public ScriptExecutor scriptExecutor() {
            return new ScriptExecutor();
        }

        @Bean
        public ScriptEvaluator scriptEvaluator() {
            return new ScriptEvaluator();
        }

        @Bean
        public PythonEvaluator pythonEvaluator(){
            return new PythonEvaluator();
        }

        @Bean
        public PythonExecutor pythonExecutor(){
            return new PythonExecutor();
        }

        @Bean
        public PythonInterpreter execInterpreter(){
            return mock(PythonInterpreter.class);
        }

        @Bean
        public PythonInterpreter evalInterpreter(){
            return mock(PythonInterpreter.class);
        }

        @Bean
        public DependencyService mavenRepositoryService() {
            return new DependencyServiceImpl();
        }

        @Bean
        public PythonRuntimeService pythonRuntimeService() {
            return new PythonRuntimeServiceImpl();
        }
    }
}
