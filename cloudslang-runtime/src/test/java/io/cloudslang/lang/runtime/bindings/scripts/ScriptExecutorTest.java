package io.cloudslang.lang.runtime.bindings.scripts;

import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.dependency.api.services.MavenConfig;
import io.cloudslang.dependency.impl.services.DependencyServiceImpl;
import io.cloudslang.dependency.impl.services.MavenConfigImpl;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.runtime.api.python.PythonRuntimeService;
import io.cloudslang.runtime.impl.python.PythonExecutionCachedEngine;
import io.cloudslang.runtime.impl.python.PythonExecutionEngine;
import io.cloudslang.runtime.impl.python.PythonExecutor;
import io.cloudslang.runtime.impl.python.PythonRuntimeServiceImpl;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ScriptExecutorTest.Config.class)
public class ScriptExecutorTest {
    private static PythonInterpreter execInterpreter = mock(PythonInterpreter.class);

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    private ScriptExecutor scriptExecutor;

    @Test
    public void testExecuteScript() throws Exception {
        reset(execInterpreter);
        String script = "pass";
        Map<String, Value> scriptInputValues = new HashMap<>();
        Value value1 = ValueFactory.create("value1");
        Value value2 = ValueFactory.create("value2");
        scriptInputValues.put("input1", value1);
        scriptInputValues.put("input2", value2);
        Map<Object, PyObject> scriptOutputValues = new HashMap<>();
        PyObject PyObjectValue1 = (PyObject) ValueFactory.createPyObjectValue("value1", false);
        PyObject PyObjectValue2 = (PyObject) ValueFactory.createPyObjectValue("value2", false);
        scriptOutputValues.put("output1", PyObjectValue1);
        scriptOutputValues.put("output2", PyObjectValue2);
        when(execInterpreter.getLocals()).thenReturn(new PyStringMap(scriptOutputValues));
        when(execInterpreter.get(eq("output1"))).thenReturn(PyObjectValue1);
        when(execInterpreter.get(eq("output2"))).thenReturn(PyObjectValue2);
        Map<String, Serializable> expectedScriptOutputs = new HashMap<>();
        expectedScriptOutputs.put("output1", value1);
        expectedScriptOutputs.put("output2", value2);

        Map<String, Value> outputs = scriptExecutor.executeScript(script, scriptInputValues);

        verify(execInterpreter).set(eq("input1"), eq((Value) PyObjectValue1));
        verify(execInterpreter).set(eq("input2"), eq((Value) PyObjectValue2));
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

        scriptExecutor.executeScript(script, new HashMap<String, Value>());
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
        public DependencyService mavenRepositoryService() {
            return new DependencyServiceImpl();
        }

        @Bean
        public MavenConfig mavenConfig() {
            return new MavenConfigImpl();
        }

        @Bean
        public PythonRuntimeService pythonRuntimeService() {
            return new PythonRuntimeServiceImpl();
        }

        @Bean
        public PythonExecutionEngine pythonExecutionEngine() {
            return new PythonExecutionCachedEngine() {
                protected PythonExecutor createNewExecutor(Set<String> filePaths) {
                    return new PythonExecutor(filePaths) {
                        protected PythonInterpreter initInterpreter(Set<String> dependencies) {
                            return execInterpreter;
                        }
                    };
                }
            };
        }
    }
}
