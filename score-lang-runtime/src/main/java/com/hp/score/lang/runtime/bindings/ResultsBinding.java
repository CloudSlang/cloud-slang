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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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

    public String resolveResult(Map<String, String> context,
                                List<Result> results) {
        if(results.isEmpty()) {
            throw new RuntimeException("No results were found");
        }

        for(Result result : results){
            String expression = result.getExpression();
            // If the answer has no expression, we treat it as a true expression, and choose it
            if(StringUtils.isEmpty(expression)) {
                return result.getName();
            }
            try {
                Boolean evalResult = (Boolean) scriptEvaluator.evalExpr(expression, context);
                if(evalResult) {
                    return result.getName();
                }
            } catch (ClassCastException ex){
                throw new RuntimeException("Error resolving the result. The expression " + expression + " does not return boolean value", ex);
            }
        }
        throw new RuntimeException("No possible result was resolved");
    }

}
