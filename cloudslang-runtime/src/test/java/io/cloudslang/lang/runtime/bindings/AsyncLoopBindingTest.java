package io.cloudslang.lang.runtime.bindings;

import io.cloudslang.lang.entities.AsyncLoopStatement;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptEvaluator;
import io.cloudslang.lang.runtime.env.Context;
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

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AsyncLoopBindingTest {

    @SuppressWarnings("unchecked")
    private static final Set<SystemProperty> EMPTY_SET = Collections.EMPTY_SET;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @InjectMocks
    private AsyncLoopBinding asyncLoopBinding = new AsyncLoopBinding();

    @Mock
    private ScriptEvaluator scriptEvaluator;

    @Test
    public void passingNullAsyncLoopStatementThrowsException() throws Exception {
        exception.expect(RuntimeException.class);
        asyncLoopBinding.bindAsyncLoopList(null, new Context(new HashMap<String, Serializable>()), EMPTY_SET, "nodeName");
    }

    @Test
    public void passingNullContextThrowsException() throws Exception {
        exception.expect(RuntimeException.class);
        asyncLoopBinding.bindAsyncLoopList(createBasicSyncLoopStatement(), null, EMPTY_SET, "nodeName");
    }

    @Test
    public void passingNullNodeNameThrowsException() throws Exception {
        exception.expect(RuntimeException.class);
        asyncLoopBinding.bindAsyncLoopList(createBasicSyncLoopStatement(), new Context(new HashMap<String, Serializable>()), EMPTY_SET, null);
    }

    @Test
    public void testAsyncLoopListIsReturned() throws Exception {
        Map<String, Serializable> variables = new HashMap<>();
        variables.put("key1", "value1");
        variables.put("key2", "value2");
        Context context = new Context(variables);
        List<Serializable> expectedList = Lists.newArrayList((Serializable) 1, 2, 3);

        when(scriptEvaluator.evalExpr(eq("expression"), eq(variables), eq(EMPTY_SET))).thenReturn((Serializable) expectedList);

        List<Serializable> actualList = asyncLoopBinding.bindAsyncLoopList(createBasicSyncLoopStatement(), context, EMPTY_SET, "nodeName");

        verify(scriptEvaluator).evalExpr(eq("expression"), eq(variables), eq(EMPTY_SET));
        assertEquals("returned async loop list not as expected", expectedList, actualList);
    }

    @Test
    public void testEmptyExpressionThrowsException() throws Exception {
        Map<String, Serializable> variables = new HashMap<>();
        variables.put("key1", "value1");
        variables.put("key2", "value2");
        Context context = new Context(variables);

        when(scriptEvaluator.evalExpr(eq("expression"), eq(variables), eq(EMPTY_SET))).thenReturn(Lists.newArrayList());

        exception.expectMessage("expression is empty");
        exception.expect(RuntimeException.class);

        asyncLoopBinding.bindAsyncLoopList(createBasicSyncLoopStatement(), context, EMPTY_SET, "nodeName");
    }

    @Test
    public void testExceptionIsPropagated() throws Exception {
        Map<String, Serializable> variables = new HashMap<>();

        when(scriptEvaluator.evalExpr(eq("expression"), eq(variables), eq(EMPTY_SET))).thenThrow(new RuntimeException("evaluation exception"));
        exception.expectMessage("evaluation exception");
        exception.expectMessage("nodeName");
        exception.expect(RuntimeException.class);

        asyncLoopBinding.bindAsyncLoopList(createBasicSyncLoopStatement(), new Context(variables), EMPTY_SET, "nodeName");
    }

    private AsyncLoopStatement createBasicSyncLoopStatement() {
        return new AsyncLoopStatement("varName", "expression");
    }
}
