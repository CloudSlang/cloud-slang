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
import io.cloudslang.lang.runtime.bindings.scripts.ScriptEvaluator;
import io.cloudslang.lang.runtime.env.Context;

import java.util.List;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Date: 3/25/2015
 *
 * @author Bonczidai Levente
 */
@Component
public class ParallelLoopBinding extends AbstractBinding {

    public static final String PARALLEL_LOOP_EXPRESSION_ERROR_MESSAGE =
            "Error evaluating parallel loop expression in step";

    @Autowired
    private ScriptEvaluator scriptEvaluator;

    public static String generateParallelLoopExpressionMessage(String nodeName, String message) {
        return PARALLEL_LOOP_EXPRESSION_ERROR_MESSAGE + " '" + nodeName + "', error is: \n" + message;
    }

    public List<Value> bindParallelLoopList(
            LoopStatement parallelLoopStatement,
            Context flowContext,
            Set<SystemProperty> systemProperties,
            String nodeName) {
        Validate.notNull(parallelLoopStatement, "parallel loop statement cannot be null");
        Validate.notNull(flowContext, "flow context cannot be null");
        Validate.notNull(systemProperties, "system properties cannot be null");
        Validate.notNull(nodeName, "node name cannot be null");

        List<Value> result;
        try {
            Value evalResult = scriptEvaluator.evalExpr(parallelLoopStatement.getExpression(),
                    flowContext.getImmutableViewOfVariables(),
                    systemProperties, parallelLoopStatement.getFunctionDependencies());

            evalResult = getEvalResultForMap(evalResult, parallelLoopStatement, parallelLoopStatement.getExpression());

            result = (List<Value>) getIterableFromEvalResult(evalResult);
        } catch (Throwable t) {
            throw new RuntimeException(generateParallelLoopExpressionMessage(nodeName, t.getMessage()), t);
        }
        if (CollectionUtils.isEmpty(result)) {
            throw new RuntimeException(generateParallelLoopExpressionMessage(nodeName, "expression is empty"));
        }
        return result;
    }
}
