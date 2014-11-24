package com.hp.score.lang.runtime.bindings;
/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/

import com.hp.score.lang.entities.bindings.Result;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hp.score.lang.entities.ScoreLangConstants.BIND_OUTPUT_FROM_INPUTS_KEY;

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
                                Map<String, String> context,
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
                scriptContext.put(BIND_OUTPUT_FROM_INPUTS_KEY, (Serializable) inputs);
            }

            try {
                Boolean evaluatedResult = (Boolean) scriptEvaluator.evalExpr(expression, scriptContext);
                if(evaluatedResult == null){
                    throw new RuntimeException("Expression of the operation result: " + expression + " cannot be evaluated correctly to true or false value");
                }
                if(evaluatedResult) {
                    return result.getName();
                }
            } catch (ClassCastException ex){
                throw new RuntimeException("Error resolving the result. The expression " + expression + " does not return boolean value", ex);
            }
        }
        throw new RuntimeException("No possible result was resolved");
    }

}
