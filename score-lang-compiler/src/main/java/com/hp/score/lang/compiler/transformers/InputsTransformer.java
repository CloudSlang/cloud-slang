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

import com.hp.score.lang.compiler.SlangTextualKeys;
import com.hp.score.lang.entities.bindings.Input;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Component
public class InputsTransformer implements Transformer<List<Object>, List<Input>> {

    @Override
    public List<Input> transform(List<Object> rawData) {
        List<Input> result = new ArrayList<>();
        for (Object rawInput : rawData) {
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
        boolean required = prop.containsKey(SlangTextualKeys.REQUIRED_KEY) && ((boolean) prop.get(SlangTextualKeys.REQUIRED_KEY));
        boolean encrypted = prop.containsKey(SlangTextualKeys.ENCRYPTED_KEY) && ((boolean) prop.get(SlangTextualKeys.ENCRYPTED_KEY));

        Serializable valueProp = prop.containsKey(SlangTextualKeys.DEFAULT_KEY) ? (prop.get(SlangTextualKeys.DEFAULT_KEY)) : null;

        return createPropInput(entry.getKey(), required, encrypted, valueProp);
    }

    private Input createPropInput(String inputName, boolean required,boolean encrypted, Serializable value){
        String expression = null;
        Serializable defaultValue = null;
        if(isExpression(value)){
            expression = extractExpression((String)value);
        }
        else if(value != null){
            defaultValue = value;
        }
        else{
            expression = inputName ;
        }
        return new Input(inputName, expression, defaultValue, encrypted, required);
    }

    private String extractExpression(String defaultProp) {
        return StringUtils.removeStart(defaultProp,SlangTextualKeys.EXPRESSION_PREFIX_KEY);
    }

    private boolean isExpression(Serializable defaultValue) {
        return (defaultValue instanceof String) && StringUtils.startsWith((String)defaultValue,SlangTextualKeys.EXPRESSION_PREFIX_KEY);
    }

    private Input createExpressionInput(Map.Entry<String, String> entry) {
        return createPropInput(entry.getKey() , true, false, entry.getValue());
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
