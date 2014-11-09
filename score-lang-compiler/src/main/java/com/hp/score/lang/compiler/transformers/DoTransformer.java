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

import com.hp.score.lang.entities.bindings.Input;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;


@Component
public class DoTransformer implements Transformer<LinkedHashMap<String, List>, List<Input>> {

    private static final String DEFAULT_KEY = "default";

    private static final String EXPRESSION_KEY = "expression";

    private static final String REQUIRED_KEY = "required";

    private static final String ENCRYPTED_KEY = "encrypted";

    @Override
    public List<Input> transform(LinkedHashMap<String, List> rawData) {
        List<Input> result = new ArrayList<>();
        Map.Entry<String, List> inputsEntry = rawData.entrySet().iterator().next();
        for (Object rawInput : inputsEntry.getValue()) {
            //- some_input
            //this is our default behavior that if the user specifies only a key, the key is also the ref we look for
            if (rawInput instanceof String) {
                result.add(createRefInput((String) rawInput));
            } else if (rawInput instanceof Map) {
                Map.Entry entry = (Map.Entry) ((Map) rawInput).entrySet().iterator().next();
                // - some_input: some_expression
                // the value of the input is an expression we need to evaluate at runtime
                if (entry.getValue() instanceof String) {
                    result.add(createExpressionInput(entry));
                }
                // - some_inputs:
                //      property1: value1
                //      property2: value2
                // this is the verbose way of defining inputs with all of the properties available
                else if (entry.getValue() instanceof Map) {
                    result.add(createPropInput(entry));
                }
            }
        }
        return result;
    }

    private Input createPropInput(Map.Entry<String, Map<String, Serializable>> entry) {
        Map<String, Serializable> prop = entry.getValue();
        boolean required = prop.containsKey(REQUIRED_KEY) && ((boolean) prop.get(REQUIRED_KEY));
        boolean encrypted = prop.containsKey(ENCRYPTED_KEY) && ((boolean) prop.get(ENCRYPTED_KEY));
        String expression = prop.containsValue(EXPRESSION_KEY) ? ((String) prop.get(ENCRYPTED_KEY)) : null;
        Serializable defaultValue = prop.containsValue(DEFAULT_KEY) ? (prop.get(DEFAULT_KEY)) : null;
        return new Input(entry.getKey(), expression, defaultValue, encrypted, required);
    }

    private Input createExpressionInput(Map.Entry<String, String> entry) {
        return new Input(entry.getKey(), entry.getValue());
    }

    private Input createRefInput(String rawInput) {
        return new Input(rawInput, rawInput);
    }

    @Override
    public List<Scope> getScopes() {
        return Arrays.asList(Scope.BEFORE_OPERATION, Scope.BEFORE_TASK);
    }

    @Override
    public String keyToTransform() {
        return null;
    }

    @Override
    public String keyToRegister() {
        return null;
    }

}
