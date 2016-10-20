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
import io.cloudslang.lang.entities.LoopStatement;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.ScriptFunction;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptEvaluator;
import io.cloudslang.lang.runtime.env.Context;
import io.cloudslang.lang.runtime.env.ForLoopCondition;
import io.cloudslang.lang.runtime.env.LoopCondition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.anySetOf;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LoopsBindingTest {

    @SuppressWarnings("unchecked")
    private static final Set<SystemProperty> EMPTY_SET = Collections.EMPTY_SET;
    @SuppressWarnings("unchecked")
    private static final Set<ScriptFunction> EMPTY_FUNCTION_SET = Collections.EMPTY_SET;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @InjectMocks
    private LoopsBinding loopsBinding = new LoopsBinding();

    @Mock
    private ScriptEvaluator scriptEvaluator;

    private LoopStatement createBasicForStatement() {
        return new ListLoopStatement("x", "[1]", new HashSet<ScriptFunction>(), new HashSet<String>(), false);
    }

    @Test
    public void whenValueIsNotThereItWillBeCreated() throws Exception {
        Context context = mock(Context.class);
        ArrayList<Value> result = Lists.newArrayList(ValueFactory.create(1));
        when(scriptEvaluator.evalExpr(
                anyString(),
                anyMapOf(String.class, Value.class),
                anySetOf(SystemProperty.class),
                anySetOf(ScriptFunction.class))
        ).thenReturn(ValueFactory.create(result));
        Value loopCondition = ValueFactory.create(new ForLoopCondition(result));
        when(context.getLanguageVariable(LoopCondition.LOOP_CONDITION_KEY)).thenReturn(null);
        context.putLanguageVariable(LoopCondition.LOOP_CONDITION_KEY, loopCondition);
        when(context.getLanguageVariable(LoopCondition.LOOP_CONDITION_KEY)).thenReturn(loopCondition);
        loopsBinding.getOrCreateLoopCondition(createBasicForStatement(), context, EMPTY_SET, "node");
        verify(context).putLanguageVariable(LoopCondition.LOOP_CONDITION_KEY, loopCondition);
    }

    @Test
    public void whenExpressionIsEmptyThrowsException() throws Exception {
        Context context = mock(Context.class);
        when(scriptEvaluator.evalExpr(anyString(), anyMapOf(String.class, Value.class),
                eq(EMPTY_SET), eq(EMPTY_FUNCTION_SET)))
                .thenReturn(ValueFactory.create(Lists.newArrayList()));
        Map<String, Value> langVars = Collections.emptyMap();
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
        loopsBinding.getOrCreateLoopCondition(createBasicForStatement(), mock(Context.class), EMPTY_SET, null);
    }

    @Test
    public void whenValueIsThereItWillBeReturned() throws Exception {
        Context context = mock(Context.class);
        ArrayList<Value> result = Lists.newArrayList(ValueFactory.create(1));
        when(scriptEvaluator.evalExpr(anyString(), anyMapOf(String.class, Value.class),
                eq(EMPTY_SET), eq(EMPTY_FUNCTION_SET)))
                .thenReturn(ValueFactory.create(result));
        Map<String, Value> langVars = new HashMap<>();
        ForLoopCondition forLoopCondition = new ForLoopCondition(result);
        langVars.put(LoopCondition.LOOP_CONDITION_KEY, ValueFactory.create(forLoopCondition));
        when(context.getLanguageVariable(LoopCondition.LOOP_CONDITION_KEY))
                .thenReturn(langVars.get(LoopCondition.LOOP_CONDITION_KEY));
        when(context.getImmutableViewOfLanguageVariables())
                .thenReturn(Collections.unmodifiableMap(langVars));
        loopsBinding.getOrCreateLoopCondition(createBasicForStatement(), context, EMPTY_SET, "node");
        Assert.assertEquals(true, context.getImmutableViewOfLanguageVariables()
                .containsKey(LoopCondition.LOOP_CONDITION_KEY));
        Assert.assertEquals(forLoopCondition, context.getImmutableViewOfLanguageVariables()
                .get(LoopCondition.LOOP_CONDITION_KEY).get());
    }

    @Test
    public void testIncrementListForLoop() throws Exception {
        Serializable nextValue = "1";
        Context context = mock(Context.class);
        ForLoopCondition forLoopCondition = mock(ForLoopCondition.class);
        when(forLoopCondition.next()).thenReturn(ValueFactory.create(nextValue));

        loopsBinding.incrementListForLoop("varName", context, forLoopCondition);

        verify(forLoopCondition).next();
        verify(context).putVariable("varName", ValueFactory.create(nextValue));
    }

    @Test
    public void testIncrementMapForLoop() throws Exception {
        Context context = mock(Context.class);
        ForLoopCondition forLoopCondition = mock(ForLoopCondition.class);
        when(forLoopCondition.next())
                .thenReturn(ValueFactory.create(Pair.of(ValueFactory.create("john"), ValueFactory.create(1))));

        loopsBinding.incrementMapForLoop("k", "v", context, forLoopCondition);

        verify(forLoopCondition).next();
        verify(context).putVariable("k", ValueFactory.create("john"));
        verify(context).putVariable("v", ValueFactory.create(1));
    }

}