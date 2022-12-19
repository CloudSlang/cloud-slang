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

import io.cloudslang.lang.entities.ParallelLoopStatement;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.runtime.env.Context;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static io.cloudslang.lang.entities.bindings.values.Value.toStringSafe;
import static java.lang.Integer.getInteger;
import static java.lang.Integer.min;
import static java.lang.Integer.parseInt;
import static java.util.Objects.requireNonNull;

@Component
public class ParallelLoopBinding extends AbstractBinding {

    private static final Logger logger = LogManager.getLogger(ParallelLoopBinding.class);

    private static final String EXPRESSION_ERROR_MESSAGE = "Error evaluating parallel loop expression in step";
    private static final String THROTTLE_ERROR_MESSAGE = "Error evaluating parallel loop throttle expression in step";
    private static final int DEFAULT_THROTTLE = 20;

    private final int systemMaxThrottle;

    public ParallelLoopBinding() {
        int parallelThrottle = getInteger("worker.parallelMaxThrottle", DEFAULT_THROTTLE);
        systemMaxThrottle = (parallelThrottle > 1 && parallelThrottle <= 100) ? parallelThrottle : DEFAULT_THROTTLE;

        logger.info("Worker parallel max throttle: " + systemMaxThrottle);
    }

    private static String generateParallelLoopExpressionMessage(String nodeName, String message) {
        return EXPRESSION_ERROR_MESSAGE + " '" + nodeName + "', error is: \n" + message;
    }

    public List<Value> bindParallelLoopList(ParallelLoopStatement parallelLoopStatement,
                                            Context flowContext,
                                            Set<SystemProperty> systemProperties,
                                            String nodeName) {
        if ((parallelLoopStatement == null) || (flowContext == null) ||
                (systemProperties == null) || (nodeName == null)) {
            requireNonNull(parallelLoopStatement, "parallel loop statement cannot be null");
            requireNonNull(flowContext, "flow context cannot be null");
            requireNonNull(systemProperties, "system properties cannot be null");
            throw new NullPointerException("node name cannot be null");
        }

        List<Value> result;
        try {
            Value evalResult = scriptEvaluator.evalExpr(parallelLoopStatement.getExpression(),
                    flowContext.getImmutableViewOfVariables(),
                    systemProperties, parallelLoopStatement.getFunctionDependencies());

            evalResult = getEvalResultForMap(evalResult, parallelLoopStatement, parallelLoopStatement.getExpression());

            result = (List<Value>) getIterableFromEvalResult(evalResult);
        } catch (Exception exc) {
            throw new RuntimeException(generateParallelLoopExpressionMessage(nodeName, exc.getMessage()), exc);
        }
        if (!CollectionUtils.isEmpty(result)) {
            return result;
        } else {
            throw new RuntimeException(generateParallelLoopExpressionMessage(nodeName, "expression is empty"));
        }
    }

    public int bindParallelLoopThrottle(ParallelLoopStatement parallelLoopStatement,
                                        Context flowContext,
                                        Set<SystemProperty> systemProperties,
                                        String nodeName) {

        if (parallelLoopStatement.getThrottleExpression() == null) {
            return systemMaxThrottle;
        }

        if ((parallelLoopStatement == null) || (flowContext == null) ||
                (systemProperties == null) || (nodeName == null)) {
            requireNonNull(parallelLoopStatement, "parallel loop statement cannot be null");
            requireNonNull(flowContext, "flow context cannot be null");
            requireNonNull(systemProperties, "system properties cannot be null");
            throw new NullPointerException("node name cannot be null");
        }

        try {
            Value evalResult = scriptEvaluator.evalExpr(parallelLoopStatement.getThrottleExpression(),
                    flowContext.getImmutableViewOfVariables(),
                    systemProperties,
                    parallelLoopStatement.getFunctionDependencies());
            int throttleSize = parseInt(toStringSafe(evalResult));

            if (throttleSize <= 0) {
                throw new RuntimeException("'max_throttle' input is not valid");
            }

            return min(throttleSize, systemMaxThrottle);
        } catch (Exception exc) {
            throw new RuntimeException(
                    THROTTLE_ERROR_MESSAGE + " '" + nodeName + "', error is: \n" + exc.getMessage(), exc);
        }
    }
}
