package io.cloudslang.lang.runtime.bindings.scripts;

import io.cloudslang.dependency.api.services.DependencyService;
import io.cloudslang.dependency.api.services.MavenConfig;
import io.cloudslang.dependency.impl.services.DependencyServiceImpl;
import io.cloudslang.dependency.impl.services.MavenConfigImpl;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.ScriptFunction;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.runtime.api.python.PythonRuntimeService;
import io.cloudslang.runtime.impl.python.PythonExecutionCachedEngine;
import io.cloudslang.runtime.impl.python.PythonExecutionEngine;
import io.cloudslang.runtime.impl.python.PythonExecutor;
import io.cloudslang.runtime.impl.python.PythonRuntimeServiceImpl;
import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.python.core.PyStringMap;
import org.python.google.common.collect.Sets;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ScriptEvaluatorTest.Config.class)
public class ScriptEvaluatorTest {

    private static String LINE_SEPARATOR = System.lineSeparator();
    private static final String GET_FUNCTION_DEFINITION =
            "def get(key, default_value=None):" + LINE_SEPARATOR +
                    "  value = globals().get(key)" + LINE_SEPARATOR +
                    "  return default_value if value is None else value";
    private static final String GET_SP_FUNCTION_DEFINITION =
            "def get_sp(key, default_value=None):" + LINE_SEPARATOR +
                    "  property_value = __sys_prop__.get(key)" + LINE_SEPARATOR +
                    "  return default_value if property_value is None else property_value";
    private static final String CHECK_EMPTY_FUNCTION_DEFINITION =
            "def check_empty(value_to_check, default_value=None):" + LINE_SEPARATOR +
                    "  return default_value if value_to_check is None else value_to_check";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    private ScriptEvaluator scriptEvaluator;

    @Autowired
    private PythonInterpreter pythonInterpreter;

    @Test
    public void testEvalExpr() throws Exception {
        reset(pythonInterpreter);
        scriptEvaluator.evalExpr("", new HashMap<String, Value>(), new HashSet<SystemProperty>());
        verify(pythonInterpreter).eval(eq(""));
        verify(pythonInterpreter).set("true", Boolean.TRUE);
        verify(pythonInterpreter).set("false", Boolean.FALSE);
    }

    @Test
    public void testEvalExprError() throws Exception {
        reset(pythonInterpreter);
        when(pythonInterpreter.eval(anyString())).thenThrow(new RuntimeException("error from interpreter"));
        exception.expect(RuntimeException.class);
        exception.expectMessage("input_expression");
        exception.expectMessage("error from interpreter");
        scriptEvaluator.evalExpr("input_expression", new HashMap<String, Value>(), new HashSet<SystemProperty>());
    }

    @Test
    public void testEvalFunctions() throws Exception {
        reset(pythonInterpreter);
        Set<SystemProperty> props = new HashSet<>();
        SystemProperty systemProperty = new SystemProperty("a.b", "c.key", "value");
        props.add(systemProperty);
        Set<ScriptFunction> functionDependencies = Sets.newHashSet(
                ScriptFunction.GET,
                ScriptFunction.GET_SYSTEM_PROPERTY,
                ScriptFunction.CHECK_EMPTY
        );
        ArgumentCaptor<String> scriptCaptor = ArgumentCaptor.forClass(String.class);

        when(pythonInterpreter.getLocals()).thenReturn(new PyStringMap());

        scriptEvaluator.evalExpr("", new HashMap<String, Value>(), props, functionDependencies);

        verify(pythonInterpreter).eval(eq(""));
        verify(pythonInterpreter, atLeastOnce()).set(eq("__sys_prop__"), anyMap());

        verify(pythonInterpreter).exec(scriptCaptor.capture());
        String actualScript = scriptCaptor.getValue();
        String[] actualFunctionsArray = actualScript.split(LINE_SEPARATOR + LINE_SEPARATOR);
        Set<String> actualFunctions = new HashSet<>();
        Collections.addAll(actualFunctions, actualFunctionsArray);
        Set<String> expectedFunctions = Sets.newHashSet(
                GET_FUNCTION_DEFINITION,
                GET_SP_FUNCTION_DEFINITION,
                CHECK_EMPTY_FUNCTION_DEFINITION
        );
        Assert.assertEquals(expectedFunctions, actualFunctions);
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
        public PythonInterpreter pythonInterpreter() {
            return mock(PythonInterpreter.class);
        }

        @Bean
        public PythonExecutionEngine pythonExecutionEngine(){
            return new PythonExecutionCachedEngine() {
                protected PythonExecutor createNewExecutor(Set<String> filePaths) {
                    return new PythonExecutor(filePaths) {
                        protected PythonInterpreter initInterpreter(Set<String> dependencies) {
                            return  pythonInterpreter();
                        }
                    };
                }
            };
        }
    }
}
