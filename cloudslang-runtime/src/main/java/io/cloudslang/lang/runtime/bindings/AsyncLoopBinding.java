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

import io.cloudslang.lang.entities.AsyncLoopStatement;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptEvaluator;
import io.cloudslang.lang.runtime.env.Context;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Date: 3/25/2015
 *
 * @author Bonczidai Levente
 */
@Component
public class AsyncLoopBinding {

    public static final String ASYNC_LOOP_EXPRESSION_ERROR_MESSAGE = "Error evaluating async loop expression in step";

    @Autowired
    private ScriptEvaluator scriptEvaluator;

    public static String generateAsyncLoopExpressionMessage(String nodeName, String message) {
        return ASYNC_LOOP_EXPRESSION_ERROR_MESSAGE + " '" + nodeName + "', error is: \n" + message;
    }

    public List<Value> bindAsyncLoopList(
            AsyncLoopStatement asyncLoopStatement,
            Context flowContext,
            Set<SystemProperty> systemProperties,
            String nodeName) {
        Validate.notNull(asyncLoopStatement, "async loop statement cannot be null");
        Validate.notNull(flowContext, "flow context cannot be null");
        Validate.notNull(systemProperties, "system properties cannot be null");
        Validate.notNull(nodeName, "node name cannot be null");

        List<Value> result = new ArrayList<>();
        try {
            Value evalResult = scriptEvaluator.evalExpr(asyncLoopStatement.getExpression(), flowContext.getImmutableViewOfVariables(), systemProperties);
            if (evalResult != null && evalResult.get() != null) {
                //noinspection unchecked
                for (Serializable serializable : ((List<Serializable>)evalResult.get())) {
                    Value value = ValueFactory.create(serializable, evalResult.isSensitive());
                    result.add(value);
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException(generateAsyncLoopExpressionMessage(nodeName, t.getMessage()), t);
        }
        if (CollectionUtils.isEmpty(result)) {
            throw new RuntimeException(generateAsyncLoopExpressionMessage(nodeName, "expression is empty"));
        }
        return result;
    }
}
