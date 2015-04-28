package io.cloudslang.lang.runtime.bindings;

import org.apache.commons.lang3.tuple.Pair;
import io.cloudslang.lang.entities.ListForLoopStatement;
import io.cloudslang.lang.runtime.env.LoopCondition;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.cloudslang.lang.entities.LoopStatement;
import io.cloudslang.lang.runtime.env.Context;
import io.cloudslang.lang.runtime.env.ForLoopCondition;
import org.python.google.common.collect.Lists;

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
        return new ListForLoopStatement("x", "[1]");
    }

    @Test
    public void whenValueIsNotThereItWillBeCreated() throws Exception {
        Context context = mock(Context.class);
        when(scriptEvaluator.evalExpr(anyString(), anyMapOf(String.class, Serializable.class)))
                .thenReturn(Lists.newArrayList(1));
        HashMap<String, Serializable> langVars = new HashMap<>();
        when(context.getLangVariables()).thenReturn(langVars);
        loopsBinding.getOrCreateLoopCondition(createBasicForStatement(), context, "node");
        Assert.assertEquals(true, context.getLangVariables().containsKey(LoopCondition.LOOP_CONDITION_KEY));
    }

    @Test
    public void whenExpressionIsEmptyThrowsException() throws Exception {
        Context context = mock(Context.class);
        when(scriptEvaluator.evalExpr(anyString(), anyMapOf(String.class, Serializable.class)))
                .thenReturn(Lists.newArrayList());
        HashMap<String, Serializable> langVars = new HashMap<>();
        when(context.getLangVariables()).thenReturn(langVars);

        exception.expectMessage("expression is empty");
        exception.expect(RuntimeException.class);

        loopsBinding.getOrCreateLoopCondition(createBasicForStatement(), context, "node");
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
        Context context = mock(Context.class);
        when(scriptEvaluator.evalExpr(anyString(), anyMapOf(String.class, Serializable.class)))
                .thenReturn(new ArrayList<>());
        HashMap<String, Serializable> langVars = new HashMap<>();
        ForLoopCondition forLoopCondition = mock(ForLoopCondition.class);
        langVars.put(LoopCondition.LOOP_CONDITION_KEY, forLoopCondition);
        when(context.getLangVariables()).thenReturn(langVars);
        loopsBinding.getOrCreateLoopCondition(createBasicForStatement(), context, "node");
        Assert.assertEquals(true, context.getLangVariables().containsKey(LoopCondition.LOOP_CONDITION_KEY));
        Assert.assertEquals(forLoopCondition, context.getLangVariables().get(LoopCondition.LOOP_CONDITION_KEY));
    }

    @Test
    public void testIncrementListForLoop() throws Exception {
        Serializable nextValue = "1";
        Context context = mock(Context.class);
        ForLoopCondition forLoopCondition = mock(ForLoopCondition.class);
        when(forLoopCondition.next()).thenReturn(nextValue);

        loopsBinding.incrementListForLoop("varName", context, forLoopCondition);

        verify(forLoopCondition).next();
        verify(context).putVariable("varName", nextValue);
    }

    @Test
    public void testIncrementMapForLoop() throws Exception {
        Context context = mock(Context.class);
        ForLoopCondition forLoopCondition = mock(ForLoopCondition.class);
        when(forLoopCondition.next()).thenReturn(Pair.of("john", 1));

        loopsBinding.incrementMapForLoop("k", "v", context, forLoopCondition);

        verify(forLoopCondition).next();
        verify(context).putVariable("k", "john");
        verify(context).putVariable("v", 1);
    }

}