/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.openscore.lang.runtime.bindings;

import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.openscore.lang.entities.ForLoopStatement;
import org.openscore.lang.entities.MapForLoopStatement;
import org.openscore.lang.runtime.env.Context;
import org.openscore.lang.runtime.env.ForLoopCondition;
import org.openscore.lang.runtime.env.LoopCondition;
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

import static org.openscore.lang.runtime.env.LoopCondition.LOOP_CONDITION_KEY;

@Component
public class LoopsBinding {

    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    private ScriptEvaluator scriptEvaluator;

    public LoopCondition getOrCreateLoopCondition(ForLoopStatement forLoopStatement, Context flowContext, String nodeName) {
        Validate.notNull(forLoopStatement, "loop statement cannot be null");
        Validate.notNull(flowContext, "flow context cannot be null");
        Validate.notNull(nodeName, "node name cannot be null");

        Map<String, Serializable> langVariables = flowContext.getLangVariables();
        if (!langVariables.containsKey(LOOP_CONDITION_KEY)) {
            LoopCondition loopCondition = createForLoopCondition(forLoopStatement, flowContext, nodeName);
            langVariables.put(LOOP_CONDITION_KEY, loopCondition);
        }
        return (LoopCondition) langVariables.get(LOOP_CONDITION_KEY);
    }

    public void incrementListForLoop(String varName, Context flowContext, ForLoopCondition forLoopCondition) {
        Validate.notNull(varName, "loop var name cannot be null");
        Validate.notNull(flowContext, "flow context cannot be null");
        Validate.notNull(forLoopCondition, "for condition cannot be null");

        Serializable varValue = forLoopCondition.next();
        flowContext.putVariable(varName, varValue);
        logger.debug("name: " + varName + ", value: " + varValue);
    }

    public void incrementMapForLoop(String keyName, String valueName, Context flowContext, ForLoopCondition forLoopCondition) {
        Validate.notNull(keyName, "loop key name cannot be null");
        Validate.notNull(keyName, "loop value name cannot be null");
        Validate.notNull(flowContext, "flow context cannot be null");
        Validate.notNull(forLoopCondition, "for condition cannot be null");

        @SuppressWarnings("unchecked") Map.Entry<Serializable, Serializable> entry = (Map.Entry<Serializable, Serializable>) forLoopCondition.next();
        Serializable keyFromIteration = entry.getKey();
        Serializable valueFromIteration = entry.getValue();

        flowContext.putVariable(keyName, keyFromIteration);
        flowContext.putVariable(valueName, valueFromIteration);
        logger.debug("key name: " + keyName + ", value: " + keyFromIteration);
        logger.debug("value name: " + keyName + ", value: " + valueFromIteration);
    }

    private LoopCondition createForLoopCondition(ForLoopStatement forLoopStatement, Context flowContext, String nodeName) {
        Map<String, Serializable> variables = flowContext.getImmutableViewOfVariables();
        Serializable evalResult;
        String collectionExpression = forLoopStatement.getExpression();
        try {
            evalResult = scriptEvaluator.evalExpr(collectionExpression, variables);
        } catch (Throwable t) {
            throw new RuntimeException("Error evaluating for loop expression in task '" + nodeName + "', error is: \n" + t.getMessage(), t);
        }

        if (forLoopStatement instanceof MapForLoopStatement) {
            if (evalResult instanceof Map) {
                List<Map.Entry<Serializable, Serializable>> entriesAsSerializable = new ArrayList<>();
                @SuppressWarnings("unchecked") Set<Map.Entry<Serializable, Serializable>> entrySet = ((Map) evalResult).entrySet();
                for (Map.Entry<Serializable, Serializable> entry : entrySet) {
                    entriesAsSerializable.add(Pair.of(entry.getKey(), entry.getValue()));
                }
                evalResult = (Serializable) entriesAsSerializable;
            } else {
                throw new RuntimeException("Invalid expression for iterating maps: " + collectionExpression);
            }
        }

        ForLoopCondition forLoopCondition = createForLoopCondition(evalResult);
        if (forLoopCondition == null) {
            throw new RuntimeException("collection expression: '" + collectionExpression + "' in the 'for' loop " +
                    "in task: '" + nodeName + "' " +
                    "doesn't return an iterable, other types are not supported");
        }
        return forLoopCondition;
    }

    private ForLoopCondition createForLoopCondition(Serializable loopCollection){
        Iterable<? extends Serializable> iterable;

        if (loopCollection instanceof Iterable) {
            iterable = (Iterable<Serializable>) loopCollection;
        } else if (loopCollection instanceof String) {
            String[] strings = ((String) loopCollection).split(Pattern.quote(","));
            iterable = Arrays.asList(strings);
        } else if (loopCollection instanceof PyObject) {
            PyObject pyObject = (PyObject) loopCollection;
            iterable = pyObject.asIterable();
        } else {
            return null;
        }

        return new ForLoopCondition(iterable);
    }
}
