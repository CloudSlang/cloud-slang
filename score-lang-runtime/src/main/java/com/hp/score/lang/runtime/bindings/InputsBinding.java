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

import com.hp.score.lang.entities.bindings.Input;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public final class InputsBinding {

    @Autowired
    ScriptEvaluator scriptEvaluator;

    /**
     * Binds the inputs to a new result map
     * @param context : initial context
     * @param inputs : the inputs to bind
     * @return : a new map with all inputs resolved (does not include initial context)
     */
    public Map<String,Serializable> bindInputs(Map<String,Serializable> context,
                                               List<Input> inputs){
        Map<String,Serializable> resultContext = new HashMap<>();
        for(Input input:inputs){
            bindInput(input,context,resultContext);
        }
        return resultContext;
    }

    private void bindInput(Input input, Map<String,Serializable> context,Map<String,Serializable> resultContext) {
        String inputName = input.getName();
        Serializable value = resolveValue(inputName, input, context);

        if(input.isRequired() && value == null) {
            throw new RuntimeException("Input with name :"+ inputName + " is Required, but value is empty");
        }

        resultContext.put(inputName,value);
    }

    private Serializable resolveValue(String inputName, Input input, Map<String, Serializable> context) {
        Serializable value = null;

        if(context.containsKey(inputName) && !input.isOverride()){
            value = context.get(inputName);
        }

        if(value == null && input.getDefaultValue() != null){
            value = input.getDefaultValue();
        }

        if(value == null && StringUtils.isNotEmpty(input.getExpression())){
            String expr = input.getExpression();
            value = scriptEvaluator.evalExpr(expr, context);
        }

        return value;
    }


}
