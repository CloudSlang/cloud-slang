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



import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.utils.ExpressionUtils;
import io.cloudslang.lang.entities.utils.MapUtils;
import io.cloudslang.lang.entities.utils.ResultUtils;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptEvaluator;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * User: stoneo
 * Date: 06/11/2014
 * Time: 09:32
 */
@Component
public class ResultsBinding {

    @Autowired
    public ScriptEvaluator scriptEvaluator;

    /**
     * Resolves the result name of an executable based on the list of all the possible results, the run context
     * and in the case of a flow, also the already preset result name
     * <p>
     * Throws a runtime exception in the following cases:
     * 1. No possible results were given
     * 2. In the case of a flow, the preset result name is not present in the possible results list
     * 3. One or more of the results contains an illegal expression - not evaluated to true\false value
     * 4. No result was resolved - none of the results expression returned true
     *
     * @param inputs          the executable's inputs
     * @param context         the run context
     * @param possibleResults list of all the possible Result objects of the executable
     * @param presetResult    a given result name. Will be not null only in the case of resolving a result of a flow
     * @return the resolved result name
     */
    public String resolveResult(Map<String, Value> inputs,
                                Map<String, Value> context,
                                Set<SystemProperty> systemProperties,
                                List<Result> possibleResults,
                                String presetResult) {

        // We must have possible results
        if (CollectionUtils.isEmpty(possibleResults)) {
            throw new RuntimeException("No results were found");
        }

        // In case of calculating the result of a flow, we already have a preset result from the last step of the flow,
        // we look for it in the possible results of the flow.
        // If the flow has it as a possible result, we return it as the resolved result.
        // If not, we throw an exception
        if (presetResult != null) {
            for (Result possibleResult : possibleResults) {
                if (presetResult.equals(possibleResult.getName())) {
                    return presetResult;
                }
            }
            throw new RuntimeException("Result: " + presetResult +
                    " that was calculated in the last step is not a possible result of the flow.");
        }

        // In the case of operation, we resolve the result by searching for the first result with a true expression
        // An empty expression passes as true
        for (Result result : possibleResults) {
            String resultName = result.getName();

            if (ResultUtils.isDefaultResult(result)) {
                return resultName;
            }

            Serializable rawValue = result.getValue().get();
            if (rawValue instanceof String) {
                String expression = ExpressionUtils.extractExpression(rawValue);
                if (expression == null) {
                    throw new RuntimeException(
                            "Error resolving the result. The expression: '" + rawValue + "' is not valid." +
                                    " Accepted format is: " + ScoreLangConstants.EXPRESSION_START_DELIMITER +
                                    " expression " + ScoreLangConstants.EXPRESSION_END_DELIMITER);
                }

                Map<String, Value> scriptContext = MapUtils.mergeMaps(inputs, context);

                try {
                    Value expressionResult = scriptEvaluator.evalExpr(expression, scriptContext, systemProperties,
                            result.getFunctionDependencies());
                    Boolean evaluatedResult;
                    if (expressionResult.get() instanceof Integer) {
                        evaluatedResult = (Integer) expressionResult.get() != 0;
                    } else {
                        evaluatedResult = (Boolean) expressionResult.get();
                    }
                    if (evaluatedResult == null) {
                        throw new RuntimeException("Expression of the operation result: " + expression +
                                " cannot be evaluated correctly to true or false value");
                    }
                    if (evaluatedResult) {
                        return resultName;
                    }
                } catch (ClassCastException ex) {
                    throw new RuntimeException("Error resolving the result. The expression " + expression +
                            " does not return boolean value", ex);
                } catch (Throwable t) {
                    throw new RuntimeException("Error evaluating result: '" + resultName + "',\n\tError is: " +
                            t.getMessage(), t);
                }
            } else {
                throw new RuntimeException("Error resolving the result. Value: '" + rawValue + "' is not valid.");
            }
        }
        throw new RuntimeException("No possible result was resolved");
    }

}
