package io.cloudslang.lang.runtime.bindings;

import io.cloudslang.lang.entities.ListForLoopStatement;
import io.cloudslang.lang.entities.LoopStatement;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptEvaluator;
import io.cloudslang.lang.runtime.env.Context;
import io.cloudslang.lang.runtime.env.ForLoopCondition;
import io.cloudslang.lang.runtime.env.LoopCondition;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.python.google.common.collect.Lists;

import java.io.Serializable;
import java.util.*;

import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LoopsBindingTest {

    @SuppressWarnings("unchecked")
    private static final Set<SystemProperty> EMPTY_SET = Collections.EMPTY_SET;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @InjectMocks
    private LoopsBinding loopsBinding = new LoopsBinding();

    @Mock
    private ScriptEvaluator scriptEvaluator;

    private LoopStatement createBasicForStatement() {
        return new ListForLoopStatement("x", "[1]");
    }

    @Test
    public void whenValueIsNotThereItWillBeCreated() throws Exception {
        Context context = mock(Context.class);
        when(scriptEvaluator.evalExpr(
                        anyString(),
                        anyMapOf(String.class, Serializable.class),
                        anySetOf(SystemProperty.class))
        ).thenReturn(Lists.newArrayList(1));
        when(context.getImmutableViewOfLanguageVariables()).thenReturn(Collections.<String, Serializable>emptyMap());
        loopsBinding.getOrCreateLoopCondition(createBasicForStatement(), context, EMPTY_SET, "node");
        verify(context).putLanguageVariable(eq(LoopCondition.LOOP_CONDITION_KEY), isA(ForLoopCondition.class));
    }

    @Test
    public void whenExpressionIsEmptyThrowsException() throws Exception {
        Context context = mock(Context.class);
        when(scriptEvaluator.evalExpr(anyString(), anyMapOf(String.class, Serializable.class), eq(EMPTY_SET)))
                .thenReturn(Lists.newArrayList());
        Map<String, Serializable> langVars = Collections.emptyMap();
        when(context.getImmutableViewOfLanguageVariables()).thenReturn(langVars);

        exception.expectMessage("expression is empty");
        exception.expect(RuntimeException.class);

        loopsBinding.getOrCreateLoopCondition(createBasicForStatement(), context, EMPTY_SET, "node");
    }

    @Test(expected = RuntimeException.class)
    public void passingNullLoopStatementThrowsException() throws Exception {
        loopsBinding.getOrCreateLoopCondition(null, mock(Context.class), EMPTY_SET, "aa");
    }

    @Test(expected = RuntimeException.class)
    public void passingNullContextThrowsException() throws Exception {
        loopsBinding.getOrCreateLoopCondition(createBasicForStatement(), null, EMPTY_SET, "aa");
    }

    @Test(expected = RuntimeException.class)
    public void passingNullNodeNameThrowsException() throws Exception {
        loopsBinding.getOrCreateLoopCondition(createBasicForStatement(), mock(Context.class), EMPTY_SET, null);    }

    @Test
    public void whenValueIsThereItWillBeReturned() throws Exception {
        Context context = mock(Context.class);
        when(scriptEvaluator.evalExpr(anyString(), anyMapOf(String.class, Serializable.class), eq(EMPTY_SET)))
                .thenReturn(new ArrayList<>());
        Map<String, Serializable> langVars = new HashMap<>();
        ForLoopCondition forLoopCondition = mock(ForLoopCondition.class);
        langVars.put(LoopCondition.LOOP_CONDITION_KEY, forLoopCondition);
        when(context.getImmutableViewOfLanguageVariables()).thenReturn(Collections.unmodifiableMap(langVars));
        loopsBinding.getOrCreateLoopCondition(createBasicForStatement(), context, EMPTY_SET, "node");
        Assert.assertEquals(true, context.getImmutableViewOfLanguageVariables().containsKey(LoopCondition.LOOP_CONDITION_KEY));
        Assert.assertEquals(forLoopCondition, context.getImmutableViewOfLanguageVariables().get(LoopCondition.LOOP_CONDITION_KEY));
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