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
import org.openscore.lang.runtime.env.Context;
import org.openscore.lang.runtime.env.ForLoopCondition;
import org.openscore.lang.runtime.env.LoopCondition;
import org.python.core.PyObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
            LoopCondition loopCondition = createForLoopCondition(
                    forLoopStatement.getCollectionExpression(), flowContext, nodeName);
            langVariables.put(LOOP_CONDITION_KEY, loopCondition);
        }
        return (LoopCondition) langVariables.get(LOOP_CONDITION_KEY);
    }

    public void incrementForLoop(String varName, Context flowContext, ForLoopCondition forLoopCondition) {
        Validate.notNull(varName, "loop var name cannot be null");
        Validate.notNull(flowContext, "flow context cannot be null");
        Validate.notNull(forLoopCondition, "for condition cannot be null");

        Serializable varValue = forLoopCondition.next();
        flowContext.putVariable(varName, varValue);
        logger.debug("name: " + varName + ", value: " + varValue);
    }

    private LoopCondition createForLoopCondition(String collectionExpression, Context flowContext, String nodeName) {
        Map<String, Serializable> variables = flowContext.getImmutableViewOfVariables();
        Serializable evalResult;
        try {
            evalResult = scriptEvaluator.evalExpr(collectionExpression, variables);
        } catch (Throwable t) {
            throw new RuntimeException("Error evaluating for loop expression in task '" + nodeName + "', error is: \n" + t.getMessage(), t);
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
        Iterator<? extends Serializable> iterator;

        if (loopCollection instanceof Iterable) {
            Iterable<Serializable> serializableIterable = (Iterable<Serializable>) loopCollection;
            iterator = serializableIterable.iterator();
        } else if (loopCollection instanceof String) {
            String[] strings = ((String) loopCollection).split(Pattern.quote(","));
            List<String> list = Arrays.asList(strings);
            iterator = list.iterator();
        } else if (loopCollection instanceof PyObject) {
            PyObject pyObject = (PyObject) loopCollection;
            iterator = pyObject.asIterable().iterator();
        } else {
            return null;
        }

        return new ForLoopCondition(iterator);
    }
}
