package io.cloudslang.lang.runtime.bindings;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ScriptEvaluatorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @InjectMocks
    private ScriptEvaluator scriptEvaluator = new ScriptEvaluator();

    @Mock
    private ScriptEngine scriptEngine;

    @Test
    public void testEvalExpr() throws Exception {
        Map<String, Serializable> context = new HashMap<>();
        scriptEvaluator.evalExpr("", context);
        ArgumentCaptor<ScriptContext> argument = ArgumentCaptor.forClass(ScriptContext.class);
        verify(scriptEngine).eval(eq(""), argument.capture());
        Bindings bindings = argument.getValue()
                                    .getBindings(ScriptContext.ENGINE_SCOPE);
        assertEquals(true, bindings.get("true"));
        assertEquals(false, bindings.get("false"));
    }

    @Test
    public void testEvalExprError() throws Exception {
        when(scriptEngine.eval(anyString(), any(ScriptContext.class))).thenThrow(new ScriptException("error1"));
        exception.expect(RuntimeException.class);
        exception.expectMessage("input1");
        exception.expectMessage("error1");
        scriptEvaluator.evalExpr("input1", new HashMap<String, Serializable>());
    }
}