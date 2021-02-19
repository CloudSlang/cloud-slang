/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.bindings;

import io.cloudslang.lang.entities.ListLoopStatement;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.ScriptFunction;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptEvaluator;
import io.cloudslang.lang.runtime.env.Context;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.python.google.common.collect.Lists.newArrayList;

@RunWith(MockitoJUnitRunner.class)
public class ParallelLoopBindingTest {
    static {
        System.setProperty("use.jython.expressions", "true");
    }

    @SuppressWarnings("unchecked")
    private static final Set<SystemProperty> EMPTY_SET = Collections.EMPTY_SET;
    @SuppressWarnings("unchecked")
    private static final Set<ScriptFunction> EMPTY_FUNCTION_SET = Collections.EMPTY_SET;
    @SuppressWarnings("unchecked")
    private static final Set<String> EMPTY_PROPERTY_SET = Collections.EMPTY_SET;

    @InjectMocks
    private ParallelLoopBinding parallelLoopBinding = new ParallelLoopBinding();

    @Mock
    private ScriptEvaluator scriptEvaluator;

    @Test
    public void passingNullParallelLoopStatementThrowsException() throws Exception {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                parallelLoopBinding
                        .bindParallelLoopList(null, new Context(
                        new HashMap<String, Value>(),
                        Collections.<String, Value>emptyMap()), EMPTY_SET, "nodeName"));
        Assert.assertEquals("parallel loop statement cannot be null", exception.getMessage());

    }

    @Test
    public void passingNullContextThrowsException() throws Exception {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                parallelLoopBinding.bindParallelLoopList(createBasicSyncLoopStatement(),
                        null, EMPTY_SET, "nodeName"));
        Assert.assertEquals("flow context cannot be null", exception.getMessage());
    }

    @Test
    public void passingNullNodeNameThrowsException() throws Exception {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                parallelLoopBinding.bindParallelLoopList(
                    createBasicSyncLoopStatement(), new Context(
                            new HashMap<String, Value>(),
                            Collections.<String, Value>emptyMap()), EMPTY_SET, null));
        Assert.assertEquals("node name cannot be null", exception.getMessage());
    }

    @Test
    public void testParallelLoopListIsReturned() throws Exception {
        Map<String, Value> variables = new HashMap<>();
        variables.put("key1", ValueFactory.create("value1"));
        variables.put("key2", ValueFactory.create("value2"));
        final Context context = new Context(variables,Collections.<String, Value>emptyMap());
        List<Value> expectedList = newArrayList(ValueFactory.create(1), ValueFactory.create(2), ValueFactory.create(3));

        when(scriptEvaluator.evalExpr(eq("expression"), eq(variables), eq(EMPTY_SET), eq(EMPTY_FUNCTION_SET)))
                .thenReturn(ValueFactory.create((Serializable) expectedList));

        List<Value> actualList = parallelLoopBinding
                .bindParallelLoopList(createBasicSyncLoopStatement(), context, EMPTY_SET, "nodeName");

        verify(scriptEvaluator).evalExpr(eq("expression"), eq(variables), eq(EMPTY_SET), eq(EMPTY_FUNCTION_SET));
        assertEquals("returned parallel loop list not as expected", expectedList, actualList);
    }

    @Test
    public void testEmptyExpressionThrowsException() throws Exception {
        Map<String, Value> variables = new HashMap<>();
        variables.put("key1", ValueFactory.create("value1"));
        variables.put("key2", ValueFactory.create("value2"));
        final Context context = new Context(variables,Collections.<String, Value>emptyMap());

        when(scriptEvaluator.evalExpr(eq("expression"), eq(variables), eq(EMPTY_SET), eq(EMPTY_FUNCTION_SET)))
                .thenReturn(ValueFactory.create(newArrayList()));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                parallelLoopBinding.bindParallelLoopList(createBasicSyncLoopStatement(),
                        context, EMPTY_SET, "nodeName"));
        Assert.assertEquals("Error evaluating parallel loop expression in step 'nodeName', error is: \n" +
                "expression is empty", exception.getMessage());
    }

    @Test
    public void testExceptionIsPropagated() throws Exception {
        Map<String, Value> variables = new HashMap<>();

        when(scriptEvaluator.evalExpr(eq("expression"), eq(variables), eq(EMPTY_SET), eq(EMPTY_FUNCTION_SET)))
                .thenThrow(new RuntimeException("evaluation exception"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                parallelLoopBinding
                .bindParallelLoopList(createBasicSyncLoopStatement(), new Context(
                        variables,
                        Collections.<String, Value>emptyMap()), EMPTY_SET, "nodeName"));
        Assert.assertEquals("Error evaluating parallel loop expression in step 'nodeName', error is: \n" +
                "evaluation exception", exception.getMessage());
    }

    private ListLoopStatement createBasicSyncLoopStatement() {
        return new ListLoopStatement("varName", "expression", EMPTY_FUNCTION_SET, EMPTY_PROPERTY_SET, true);
    }
}
