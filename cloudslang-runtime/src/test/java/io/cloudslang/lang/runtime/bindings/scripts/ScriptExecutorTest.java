/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.bindings.scripts;

import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.dependency.api.services.MavenConfig;
import io.cloudslang.dependency.impl.services.DependencyServiceImpl;
import io.cloudslang.dependency.impl.services.MavenConfigImpl;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.runtime.api.python.PythonExecutionResult;
import io.cloudslang.runtime.api.python.PythonRuntimeService;
import io.cloudslang.runtime.impl.python.PythonExecutionCachedEngine;
import io.cloudslang.runtime.impl.python.PythonExecutionEngine;
import io.cloudslang.runtime.impl.python.PythonExecutor;
import io.cloudslang.runtime.impl.python.PythonRuntimeServiceImpl;
import io.cloudslang.runtime.impl.python.external.ExternalPythonExecutionEngine;
import io.cloudslang.runtime.impl.python.external.ExternalPythonRuntimeServiceImpl;
import io.cloudslang.score.events.EventBus;
import io.cloudslang.score.events.EventBusImpl;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Matchers.any;
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

    @Resource(name = "externalPythonRuntimeService")
    private PythonRuntimeService externalPyhonRuntimeService;

    @Test
    public void testExecuteScript() throws Exception {
        reset(execInterpreter);
        final String script = "pass";
        Map<String, Value> scriptInputValues = new HashMap<>();
        Value value1 = ValueFactory.create("value1");
        Value value2 = ValueFactory.create("value2");
        scriptInputValues.put("input1", value1);
        scriptInputValues.put("input2", value2);
        Map<Object, PyObject> scriptOutputValues = new HashMap<>();
        PyObject pyObjectValue1 = (PyObject) ValueFactory.createPyObjectValue("value1", false, false);
        PyObject pyObjectValue2 = (PyObject) ValueFactory.createPyObjectValue("value2", false, false);
        scriptOutputValues.put("output1", pyObjectValue1);
        scriptOutputValues.put("output2", pyObjectValue2);
        when(execInterpreter.getLocals()).thenReturn(new PyStringMap(scriptOutputValues));
        when(execInterpreter.get(eq("output1"))).thenReturn(pyObjectValue1);
        when(execInterpreter.get(eq("output2"))).thenReturn(pyObjectValue2);
        Map<String, Serializable> expectedScriptOutputs = new HashMap<>();
        expectedScriptOutputs.put("output1", value1);
        expectedScriptOutputs.put("output2", value2);

        final Map<String, Value> outputs = scriptExecutor.executeScript(script, scriptInputValues, true);

        verify(execInterpreter).set(eq("input1"), eq((Value) pyObjectValue1));
        verify(execInterpreter).set(eq("input2"), eq((Value) pyObjectValue2));
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

        scriptExecutor.executeScript(script, new HashMap<String, Value>(), true);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExternalPythonValid() {
        Map<String, Value> scriptInputValues = new HashMap<>();
        Value value1 = ValueFactory.create("value1");
        Value value2 = ValueFactory.create("value2");
        scriptInputValues.put("input1", value1);
        scriptInputValues.put("input2", value2);
        String script = PythonScriptGeneratorUtils.generateScript(scriptInputValues.keySet());
        ArgumentCaptor<Map> callArgCaptor = ArgumentCaptor.forClass(Map.class);
        when(externalPyhonRuntimeService.exec(any(), eq(script), callArgCaptor.capture()))
                .thenReturn(new PythonExecutionResult(new HashMap<>()));

        scriptExecutor.executeScript(script, scriptInputValues, false);

        Map<String, Serializable> captured = callArgCaptor.getValue();
        Assert.assertArrayEquals(scriptInputValues.keySet().toArray(), captured.keySet().toArray());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExternalPythonValidExtraInputs() {
        Map<String, Value> scriptInputValues = new HashMap<>();
        Value value1 = ValueFactory.create("value1");
        Value value2 = ValueFactory.create("value2");
        Value value3 = ValueFactory.create("value3");
        scriptInputValues.put("input1", value1);
        scriptInputValues.put("input2", value2);
        scriptInputValues.put("input3", value3);
        String script = PythonScriptGeneratorUtils.generateScript(Arrays.asList("input1", "input2"));
        ArgumentCaptor<Map> callArgCaptor = ArgumentCaptor.forClass(Map.class);
        when(externalPyhonRuntimeService.exec(any(), eq(script), callArgCaptor.capture()))
                .thenReturn(new PythonExecutionResult(new HashMap<>()));

        scriptExecutor.executeScript(script, scriptInputValues, false);

        List<String> expectedArgs = Arrays.asList("input1", "input2");
        Map<String, Serializable> captured = callArgCaptor.getValue();
        Set<String> actualArgs = captured.keySet();
        Assert.assertTrue(expectedArgs.size() == actualArgs.size() && actualArgs.containsAll(expectedArgs));
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

        @Bean(name = "jythonRuntimeService")
        public PythonRuntimeService pythonRuntimeService() {
            return new PythonRuntimeServiceImpl();
        }

        @Bean(name = "jythonExecutionEngine")
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

        @Bean(name = "externalPythonRuntimeService")
        public PythonRuntimeService externalPythonRuntimeService() {
            return mock(ExternalPythonRuntimeServiceImpl.class);
        }

        @Bean(name = "externalPythonExecutionEngine")
        public PythonExecutionEngine externalPythonExecutionEngine() {
            return new ExternalPythonExecutionEngine();
        }

        @Bean
        public EventBus eventBus() {
            return new EventBusImpl();
        }
    }
}
