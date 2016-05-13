package io.cloudslang.lang.runtime.bindings.scripts;

import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.ScriptFunction;
import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.python.core.PyStringMap;
import org.python.google.common.collect.Sets;
import org.python.util.PythonInterpreter;

import java.io.Serializable;
import java.util.*;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
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

    @InjectMocks
    private ScriptEvaluator scriptEvaluator = new ScriptEvaluator();

    @Mock
    private PythonInterpreter pythonInterpreter;

    @Test
    public void testEvalExpr() throws Exception {
        scriptEvaluator.evalExpr("", new HashMap<String, Serializable>(), new HashSet<SystemProperty>());
        verify(pythonInterpreter).eval(eq(""));
        verify(pythonInterpreter).set("true", Boolean.TRUE);
        verify(pythonInterpreter).set("false", Boolean.FALSE);
    }

    @Test
    public void testEvalExprError() throws Exception {
        when(pythonInterpreter.eval(anyString())).thenThrow(new RuntimeException("error from interpreter"));
        exception.expect(RuntimeException.class);
        exception.expectMessage("input_expression");
        exception.expectMessage("error from interpreter");
        scriptEvaluator.evalExpr("input_expression", new HashMap<String, Serializable>(), new HashSet<SystemProperty>());
    }

    @Test
    public void testEvalFunctions() throws Exception {
        Set<SystemProperty> props = new HashSet<>();
        SystemProperty systemProperty = new SystemProperty("a.b", "c.key", "value");
        props.add(systemProperty);
        Map<String, String> propsAsMap = new HashMap<>();
        propsAsMap.put(systemProperty.getFullyQualifiedName(), systemProperty.getValue());
        Set<ScriptFunction> functionDependencies = Sets.newHashSet(
                ScriptFunction.GET,
                ScriptFunction.GET_SYSTEM_PROPERTY,
                ScriptFunction.CHECK_EMPTY
        );
        ArgumentCaptor<String> scriptCaptor = ArgumentCaptor.forClass(String.class);

        when(pythonInterpreter.getLocals()).thenReturn(new PyStringMap());

        scriptEvaluator.evalExpr("", new HashMap<String, Serializable>(), props, functionDependencies);

        verify(pythonInterpreter).eval(eq(""));
        verify(pythonInterpreter, atLeastOnce()).set("__sys_prop__", propsAsMap);

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

}
