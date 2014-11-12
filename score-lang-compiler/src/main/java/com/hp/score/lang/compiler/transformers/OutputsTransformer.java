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
package com.hp.score.lang.compiler.transformers;

import com.hp.score.lang.entities.bindings.Output;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Date: 11/7/2014
 *
 * @author Bonczidai Levente
 */
@Component
public class OutputsTransformer implements Transformer<List<Object>, List<Output>> {

    @Override
    public List<Output> transform(List<Object> rawData) {
        List<Output> outputs = new ArrayList<>();
        if (CollectionUtils.isEmpty(rawData)){
            return outputs;
        }
        for (Object rawOutput : rawData) {
            //- some_output
            //this is our default behavior that if the user specifies only a key, the key is also the ref we look for
            if (rawOutput instanceof String) {
                outputs.add(createRefOutput((String) rawOutput));
            } else if (rawOutput instanceof Map) {
                @SuppressWarnings("unchecked") Map.Entry<String, ?> entry = (Map.Entry<String, ?>) (((Map) rawOutput).entrySet()).iterator().next();
                // - some_output: some_expression
                // the value of the input is an expression we need to evaluate at runtime
                if (entry.getValue() instanceof String) {
                    outputs.add(createExpressionOutput(entry.getKey(), (String)entry.getValue()));
                }
            }
        }
        return outputs;
    }

    private Output createRefOutput(String rawOutput) {
        return new Output(rawOutput, rawOutput);
    }

    private Output createExpressionOutput(String outputName, String outputExpression){//Map.Entry<String, String> entry) {
        return new Output(outputName, outputExpression);
    }

    @Override
    public List<Scope> getScopes() {
        return Arrays.asList(Scope.AFTER_EXECUTABLE);
    }

    @Override
    public String keyToTransform() {
        return null;
    }

}
