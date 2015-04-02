package io.cloudslang.lang.runtime.bindings;
/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/


import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Result;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     *
     * Throws a runtime exception in the following cases:
     * 1. No possible results were given
     * 2. In the case of a flow, the preset result name is not present in the possible results list
     * 3. One or more of the results contains an illegal expression - not evaluated to true\false value
     * 4. No result was resolved - none of the results expression returned true
     *
     * @param inputs the executable's inputs
     * @param context the run context
     * @param possibleResults list of all the possible Result objects of the executable
     * @param presetResult a given result name. Will be not null only in the case of resolving a result of a flow
     * @return the resolved result name
     */
    public String resolveResult(Map<String, Serializable> inputs,
                                Map<String, Serializable> context,
                                List<Result> possibleResults,
                                String presetResult) {

        // We must have possible results
        if (CollectionUtils.isEmpty(possibleResults)) {
            throw new RuntimeException("No results were found");
        }

        // In case of calculating the result of a flow, we already have a preset result from the last task of the flow,
        // we look for it in the possible results of the flow.
        // If the flow has it as a possible result, we return it as the resolved result.
        // If not, we throw an exception
        if (presetResult != null){
            for (Result possibleResult : possibleResults){
                if(presetResult.equals(possibleResult.getName())){
                    return presetResult;
                }
            }
            throw new RuntimeException("Result: " + presetResult + " that was calculated in the last task is not a possible result of the flow.");
        }

        // In the case of operation, we resolve the result by searching for the first result with a true expression
        // An empty expression passes as true
        for(Result result : possibleResults){
            String expression = result.getExpression();
            // If the answer has no expression, we treat it as a true expression, and choose it
            if(StringUtils.isEmpty(expression)) {
                return result.getName();
            }
            //construct script context
            Map<String, Serializable> scriptContext = new HashMap<>();
            //put action outputs
            scriptContext.putAll(context);
            //put executable inputs as a map
            if(MapUtils.isNotEmpty(inputs)) {
                scriptContext.put(ScoreLangConstants.BIND_OUTPUT_FROM_INPUTS_KEY, (Serializable) inputs);
            }

            try {
                Serializable expressionResult = scriptEvaluator.evalExpr(expression, scriptContext);
                Boolean evaluatedResult;
                if (expressionResult instanceof Integer) {
                    evaluatedResult = (Integer) expressionResult != 0;
                } else {
                    evaluatedResult = (Boolean) expressionResult;
                }
                if(evaluatedResult == null){
                    throw new RuntimeException("Expression of the operation result: " + expression + " cannot be evaluated correctly to true or false value");
                }
                if(evaluatedResult) {
                    return result.getName();
                }
            } catch (ClassCastException ex){
                throw new RuntimeException("Error resolving the result. The expression " + expression + " does not return boolean value", ex);
            } catch (Throwable t) {
                throw new RuntimeException("Error evaluating result: '" + result.getName()+ "',\n\tError is: " + t.getMessage(), t);
            }
        }
        throw new RuntimeException("No possible result was resolved");
    }

}
