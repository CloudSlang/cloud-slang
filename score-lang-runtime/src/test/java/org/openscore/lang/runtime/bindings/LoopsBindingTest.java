package org.openscore.lang.runtime.bindings;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openscore.lang.entities.LoopStatement;
import org.openscore.lang.runtime.env.Context;
import org.openscore.lang.runtime.env.LoopCondition;

import javax.script.ScriptEngine;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LoopsBindingTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @InjectMocks
    private LoopsBinding loopsBinding = new LoopsBinding();

    @Mock
    private ScriptEvaluator scriptEvaluator;

    @Mock
    private ScriptEngine scriptEngine;

    private LoopStatement createBasicForStatement() {
        return new LoopStatement("x", "[1]", LoopStatement.Type.FOR);
    }

    @Test
    public void whenValueIsNotThereItWillBeCreated() throws Exception {
        Context context = mock(Context.class);
        when(scriptEvaluator.evalExpr(anyString(), anyMapOf(String.class, Serializable.class)))
                .thenReturn(new ArrayList<>());
        HashMap<String, Serializable> langVars = new HashMap<>();
        when(context.getLangVariables()).thenReturn(langVars);
        loopsBinding.getOrCreateLoopCondition(createBasicForStatement(), context, "node");
//        Assert.assertEquals(true, true);
    }

    @Test(expected = RuntimeException.class)
    public void passingNullLoopStatementThrowsException() throws Exception {
        loopsBinding.getOrCreateLoopCondition(null, mock(Context.class), "aa");
    }

    @Test(expected = RuntimeException.class)
    public void passingNullContextThrowsException() throws Exception {
        loopsBinding.getOrCreateLoopCondition(createBasicForStatement(), null, "aa");
    }

    @Test(expected = RuntimeException.class)
    public void passingNullNodeNameThrowsException() throws Exception {
        loopsBinding.getOrCreateLoopCondition(createBasicForStatement(), mock(Context.class), null);    }

    @Test
    public void whenValueIsThereItWillBeReturned() throws Exception {

    }

    @Test
    public void testIncrementForLoop() throws Exception {

    }
}