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

import io.cloudslang.lang.entities.LoopStatement;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.runtime.env.Context;
import io.cloudslang.lang.runtime.env.ForLoopCondition;
import io.cloudslang.lang.runtime.env.LoopCondition;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

import static io.cloudslang.lang.runtime.env.LoopCondition.LOOP_CONDITION_KEY;
import static java.util.Objects.requireNonNull;

@Component
public class LoopsBinding extends AbstractBinding {

    public static final String FOR_LOOP_EXPRESSION_ERROR_MESSAGE = "Error evaluating for loop expression in step";
    public static final String INVALID_MAP_EXPRESSION_MESSAGE = "Invalid expression for iterating maps";

    public LoopCondition getOrCreateLoopCondition(
            LoopStatement forLoopStatement,
            Context flowContext,
            Set<SystemProperty> systemProperties,
            String nodeName) {

        // Perform validations
        if ((forLoopStatement == null) || (flowContext == null) || (systemProperties == null) || (nodeName == null)) {
            requireNonNull(forLoopStatement, "loop statement cannot be null");
            requireNonNull(flowContext, "flow context cannot be null");
            requireNonNull(systemProperties, "system properties cannot be null");
            throw new NullPointerException("node name cannot be null");
        }

        Value loopConditionValue = flowContext.getLanguageVariable(LOOP_CONDITION_KEY);
        if (loopConditionValue == null) {
            LoopCondition loopCondition =
                    createForLoopCondition(forLoopStatement, flowContext, systemProperties, nodeName);
            Value value = ValueFactory.create(loopCondition);
            flowContext.putLanguageVariable(LOOP_CONDITION_KEY, value);
            return loopCondition;
        } else {
            return (LoopCondition) flowContext.getLanguageVariable(LOOP_CONDITION_KEY).get();
        }
    }

    public void incrementListForLoop(String varName, Context flowContext, ForLoopCondition forLoopCondition) {
        // Perform validations
        if ((varName == null) || (flowContext == null) || (forLoopCondition == null)) {
            requireNonNull(varName, "loop var name cannot be null");
            requireNonNull(flowContext, "flow context cannot be null");
            throw new NullPointerException("for condition cannot be null");
        }

        Value varValue = forLoopCondition.next();
        flowContext.putVariable(varName, varValue);
    }

    public void incrementMapForLoop(String keyName, String valueName, Context flowContext,
                                    ForLoopCondition forLoopCondition) {
        // Perform validations
        if ((keyName == null) || (valueName == null) || (flowContext == null) || (forLoopCondition == null)) {
            requireNonNull(keyName, "loop key name cannot be null");
            requireNonNull(valueName, "loop value name cannot be null");
            requireNonNull(flowContext, "flow context cannot be null");
            throw new NullPointerException("for condition cannot be null");
        }

        @SuppressWarnings("unchecked") Map.Entry<Value, Value> entry =
                (Map.Entry<Value, Value>) forLoopCondition.next().get();
        Value keyFromIteration = entry.getKey();
        Value valueFromIteration = entry.getValue();

        flowContext.putVariable(keyName, keyFromIteration);
        flowContext.putVariable(valueName, valueFromIteration);
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
            throw new RuntimeException(FOR_LOOP_EXPRESSION_ERROR_MESSAGE + " '" +
                    nodeName + "',\n\tError is: " + t.getMessage(), t);
        }

        evalResult = getEvalResultForMap(evalResult, forLoopStatement, collectionExpression);

        ForLoopCondition forLoopCondition = createForLoopCondition(evalResult);
        if (forLoopCondition == null) {
            throw new RuntimeException("collection expression: '" + collectionExpression +
                    "' in the 'for' loop " +
                    "in step: '" + nodeName + "' " +
                    "doesn't return an iterable, other types are not supported");
        }
        if (!forLoopCondition.hasMore()) {
            throw new RuntimeException(FOR_LOOP_EXPRESSION_ERROR_MESSAGE + " '" + nodeName +
                    "',\n\tError is: expression is empty");
        }
        return forLoopCondition;
    }

    private ForLoopCondition createForLoopCondition(Value evalResult) {
        Iterable<Value> iterable = getIterableFromEvalResult(evalResult);
        return iterable != null ? new ForLoopCondition(iterable) : null;
    }

}
