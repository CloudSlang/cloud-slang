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
import io.cloudslang.runtime.api.python.PythonRuntimeService;
import io.cloudslang.runtime.impl.python.PythonExecutionCachedEngine;
import io.cloudslang.runtime.impl.python.PythonExecutionEngine;
import io.cloudslang.runtime.impl.python.PythonExecutor;
import io.cloudslang.runtime.impl.python.PythonRuntimeServiceImpl;
import io.cloudslang.score.events.EventBus;
import io.cloudslang.score.events.EventBusImpl;
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
        final String script = "pass";
        Map<String, Value> scriptInputValues = new HashMap<>();
        Value value1 = ValueFactory.create("value1");
        Value value2 = ValueFactory.create("value2");
        scriptInputValues.put("input1", value1);
        scriptInputValues.put("input2", value2);
        Map<Object, PyObject> scriptOutputValues = new HashMap<>();
        PyObject pyObjectValue1 = (PyObject) ValueFactory.createPyObjectValue("value1", false);
        PyObject pyObjectValue2 = (PyObject) ValueFactory.createPyObjectValue("value2", false);
        scriptOutputValues.put("output1", pyObjectValue1);
        scriptOutputValues.put("output2", pyObjectValue2);
        when(execInterpreter.getLocals()).thenReturn(new PyStringMap(scriptOutputValues));
        when(execInterpreter.get(eq("output1"))).thenReturn(pyObjectValue1);
        when(execInterpreter.get(eq("output2"))).thenReturn(pyObjectValue2);
        Map<String, Serializable> expectedScriptOutputs = new HashMap<>();
        expectedScriptOutputs.put("output1", value1);
        expectedScriptOutputs.put("output2", value2);

        final Map<String, Value> outputs = scriptExecutor.executeScript(script, scriptInputValues);

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

        @Bean
        public EventBus eventBus() {
            return new EventBusImpl();
        }
    }
}
