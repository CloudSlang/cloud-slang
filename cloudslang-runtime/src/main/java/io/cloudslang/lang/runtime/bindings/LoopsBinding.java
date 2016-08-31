/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.bindings;

import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptEvaluator;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import io.cloudslang.lang.entities.MapForLoopStatement;
import io.cloudslang.lang.runtime.env.LoopCondition;
import io.cloudslang.lang.entities.LoopStatement;
import io.cloudslang.lang.runtime.env.Context;
import io.cloudslang.lang.runtime.env.ForLoopCondition;
import org.python.core.PyObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.regex.Pattern;

import static io.cloudslang.lang.runtime.env.LoopCondition.LOOP_CONDITION_KEY;

@Component
public class LoopsBinding {

    public static final String FOR_LOOP_EXPRESSION_ERROR_MESSAGE = "Error evaluating for loop expression in step";
    public static final String INVALID_MAP_EXPRESSION_MESSAGE = "Invalid expression for iterating maps";

    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    private ScriptEvaluator scriptEvaluator;

    public LoopCondition getOrCreateLoopCondition(
            LoopStatement forLoopStatement,
            Context flowContext,
            Set<SystemProperty> systemProperties,
            String nodeName) {
        Validate.notNull(forLoopStatement, "loop statement cannot be null");
        Validate.notNull(flowContext, "flow context cannot be null");
        Validate.notNull(systemProperties, "system properties cannot be null");
        Validate.notNull(nodeName, "node name cannot be null");

        Value LoopConditionValue = flowContext.getLanguageVariable(LOOP_CONDITION_KEY);
        if (LoopConditionValue == null) {
            LoopCondition loopCondition = createForLoopCondition(forLoopStatement, flowContext, systemProperties, nodeName);
            flowContext.putLanguageVariable(LOOP_CONDITION_KEY, ValueFactory.create(loopCondition));
        }
        return (LoopCondition) flowContext.getLanguageVariable(LOOP_CONDITION_KEY).get();
    }

    public void incrementListForLoop(String varName, Context flowContext, ForLoopCondition forLoopCondition) {
        Validate.notNull(varName, "loop var name cannot be null");
        Validate.notNull(flowContext, "flow context cannot be null");
        Validate.notNull(forLoopCondition, "for condition cannot be null");

        Value varValue = forLoopCondition.next();
        flowContext.putVariable(varName, varValue);
        logger.debug("name: " + varName + ", value: " + varValue);
    }

    public void incrementMapForLoop(String keyName, String valueName, Context flowContext, ForLoopCondition forLoopCondition) {
        Validate.notNull(keyName, "loop key name cannot be null");
        Validate.notNull(keyName, "loop value name cannot be null");
        Validate.notNull(flowContext, "flow context cannot be null");
        Validate.notNull(forLoopCondition, "for condition cannot be null");

        @SuppressWarnings("unchecked") Map.Entry<Value, Value> entry = (Map.Entry<Value, Value>) forLoopCondition.next().get();
        Value keyFromIteration = entry.getKey();
        Value valueFromIteration = entry.getValue();

        flowContext.putVariable(keyName, keyFromIteration);
        flowContext.putVariable(valueName, valueFromIteration);
        logger.debug("key name: " + keyName + ", value: " + keyFromIteration);
        logger.debug("value name: " + valueName + ", value: " + valueFromIteration);
    }

    private LoopCondition createForLoopCondition(
            LoopStatement forLoopStatement,
            Context flowContext,
            Set<SystemProperty> systemProperties,
            String nodeName) {
        Map<String, Value> variables = flowContext.getImmutableViewOfVariables();
        Value evalResult;
        String collectionExpression = forLoopStatement.getExpression();
        try {
            evalResult = scriptEvaluator.evalExpr(collectionExpression, variables, systemProperties,
                    forLoopStatement.getFunctionDependencies());
        } catch (Throwable t) {
            throw new RuntimeException(FOR_LOOP_EXPRESSION_ERROR_MESSAGE + " '" + nodeName + "',\n\tError is: " + t.getMessage(), t);
        }

        if (forLoopStatement instanceof MapForLoopStatement) {
            if (evalResult != null && evalResult.get() instanceof Map) {
                List<Map.Entry<Value, Value>> entriesAsValues = new ArrayList<>();
                @SuppressWarnings("unchecked") Set<Map.Entry<Serializable, Serializable>> entrySet = ((Map) evalResult.get()).entrySet();
                for (Map.Entry<Serializable, Serializable> entry : entrySet) {
                    entriesAsValues.add(Pair.of(
                            ValueFactory.create(entry.getKey(), evalResult.isSensitive()),
                            ValueFactory.create(entry.getValue(), evalResult.isSensitive())));
                }
                evalResult = ValueFactory.create((Serializable)entriesAsValues);
            } else {
                throw new RuntimeException(INVALID_MAP_EXPRESSION_MESSAGE + ": " + collectionExpression);
            }
        }

        ForLoopCondition forLoopCondition = createForLoopCondition(evalResult);
        if (forLoopCondition == null) {
            throw new RuntimeException("collection expression: '" + collectionExpression + "' in the 'for' loop " +
                    "in step: '" + nodeName + "' " +
                    "doesn't return an iterable, other types are not supported");
        }
        if (!forLoopCondition.hasMore()) {
            throw new RuntimeException(FOR_LOOP_EXPRESSION_ERROR_MESSAGE + " '" + nodeName + "',\n\tError is: expression is empty");
        }
        return forLoopCondition;
    }

    private ForLoopCondition createForLoopCondition(Value loopCollection){
        Iterable<Value> iterable;

        Serializable loopCollectionContent = loopCollection.get();
        if (loopCollectionContent instanceof Iterable) {
            //noinspection unchecked
            iterable = (Iterable<Value>) loopCollectionContent;
        } else if (loopCollectionContent instanceof String) {
            String[] strings = ((String) loopCollectionContent).split(Pattern.quote(","));
            iterable = convert(Arrays.asList(strings), loopCollection.isSensitive());
        } else if (loopCollectionContent instanceof PyObject) {
            PyObject pyObject = (PyObject) loopCollectionContent;
            iterable = convert(pyObject.asIterable(), loopCollection.isSensitive());
        } else {
            return null;
        }

        return new ForLoopCondition(iterable);
    }

    private Iterable<Value> convert(Iterable<? extends Serializable> iterable, boolean sensitive) {
        List<Value> values = new ArrayList<>();
        for (Serializable serializable : iterable) {
            values.add(ValueFactory.create(serializable, sensitive));
        }
        return values;
    }
}
