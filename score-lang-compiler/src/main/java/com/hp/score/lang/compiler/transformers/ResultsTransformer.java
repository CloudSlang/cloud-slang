package com.hp.score.lang.compiler.transformers;
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

/*
 * Created by orius123 on 05/11/14.
 */

import com.hp.score.lang.entities.ScoreLangConstants;
import com.hp.score.lang.entities.bindings.Result;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class ResultsTransformer implements Transformer<List, List<Result>> {

    @Override
    public List<Result> transform(List rawData) {
        List<Result> results = new ArrayList<>();
        // If there are no results specified, add the default SUCCESS & FAILURE results
        if(CollectionUtils.isEmpty(rawData)){
            results.add(createNoExpressionResult(ScoreLangConstants.SUCCESS_RESULT));
            results.add(createNoExpressionResult(ScoreLangConstants.FAILURE_RESULT));
            return results;
        }
        for (Object rawResult : rawData) {
            if (rawResult instanceof String) {
                //- some_result
                results.add(createNoExpressionResult((String) rawResult));
            } else if (rawResult instanceof Map) {
                // - some_result: some_expression
                // the value of the result is an expression we need to evaluate at runtime
                @SuppressWarnings("unchecked") Map.Entry<String, ?> entry = (Map.Entry<String, ?>) (((Map) rawResult).entrySet()).iterator().next();
                if(entry.getValue() instanceof Boolean) {
                    results.add(createExpressionResult(entry.getKey(), String.valueOf(entry.getValue())));
                } else {
                    results.add(createExpressionResult(entry.getKey(), (String)entry.getValue()));
                }
            }
        }
        return results;
    }

    @Override
    public List<Scope> getScopes() {
        return Arrays.asList(Scope.AFTER_EXECUTABLE);
    }

    @Override
    public String keyToTransform() {
        return null;
    }

    @Override
    public String keyToRegister() {
        return null;
    }

    private Result createNoExpressionResult(String rawResult) {
        return new Result(rawResult, null);
    }

    private Result createExpressionResult(String resultName, String resultExpression) {
        return new Result(resultName, resultExpression);
    }
}

