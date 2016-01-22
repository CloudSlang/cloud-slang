package io.cloudslang.lang.runtime.bindings.scripts;

import io.cloudslang.lang.entities.bindings.ScriptFunction;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.python.core.PyStringMap;
import org.python.google.common.collect.Sets;
import org.python.util.PythonInterpreter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ScriptEvaluatorTest {

    private static String LINE_SEPARATOR = System.lineSeparator();
    private static final String GET_FUNCTION_DEFINITION =
            "def get(key, default_value):" + LINE_SEPARATOR +
                    "  value = globals().get(key)" + LINE_SEPARATOR +
                    "  return default_value if value is None else value";
    private static final String GET_SP_FUNCTION_DEFINITION =
            "def get_sp(key, default_value=None):" + LINE_SEPARATOR +
                    "  property_value = __sys_prop__.get(key)" + LINE_SEPARATOR +
                    "  return default_value if property_value is None else property_value";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @InjectMocks
    private ScriptEvaluator scriptEvaluator = new ScriptEvaluator();

    @Mock
    private PythonInterpreter pythonInterpreter;

    @Test
    public void testEvalExpr() throws Exception {
        scriptEvaluator.evalExpr("", new HashMap<String, Serializable>(), new HashMap<String, String>());
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
        scriptEvaluator.evalExpr("input_expression", new HashMap<String, Serializable>(), new HashMap<String, String>());
    }

    @Test
    public void testEvalFunctions() throws Exception {
        Map<String, String> props = new HashMap<>();
        props.put("a.b.c.key", "value");
        Set<ScriptFunction> functionDependencies = Sets.newHashSet(ScriptFunction.GET, ScriptFunction.GET_SYSTEM_PROPERTY);
        when(pythonInterpreter.getLocals()).thenReturn(new PyStringMap());

        scriptEvaluator.evalExpr("", new HashMap<String, Serializable>(), props, functionDependencies);

        verify(pythonInterpreter).eval(eq(""));
        verify(pythonInterpreter, atLeastOnce()).set("__sys_prop__", props);
        verify(pythonInterpreter).exec(
                GET_FUNCTION_DEFINITION +
                        LINE_SEPARATOR +
                        LINE_SEPARATOR +
                        GET_SP_FUNCTION_DEFINITION
        );
    }

}
