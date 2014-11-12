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
package com.hp.score.lang.runtime.bindings;

import com.hp.score.lang.entities.bindings.Output;

import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.hp.score.lang.entities.ScoreLangConstants.BIND_OUTPUT_FROM_INPUTS_KEY;

/**
 * Date: 11/7/2014
 *
 * @author Bonczidai Levente
 */
@Component
public class OutputsBinding {

    @Autowired
    ScriptEvaluator scriptEvaluator;

    public Map<String, String> bindOutputs(Map<String, Serializable> inputs,
                                           Map<String, String> actionReturnValues,
                                           List<Output> possibleOutputs) {

        Map<String, String> outputs = new LinkedHashMap<>();
        if (possibleOutputs != null) {
            for (Output output : possibleOutputs) {
                String outputKey = output.getName();
                String outputExpr = output.getExpression();
                if (outputExpr != null) {
                    //construct script context
                    Map<String, Serializable> scriptContext = new HashMap<>();
                    //put action outputs
                    scriptContext.putAll(actionReturnValues);
                    //put operation inputs as a map
                    if(MapUtils.isNotEmpty(inputs)) {
                        scriptContext.put(BIND_OUTPUT_FROM_INPUTS_KEY, (Serializable) inputs);
                    }

                    //evaluate expression
                    Serializable scriptResult = scriptEvaluator.evalExpr(outputExpr, scriptContext);

                    if (scriptResult != null) {
                        try {
                            outputs.put(outputKey, (String) scriptResult);
                        } catch (ClassCastException ex) {
                            throw new RuntimeException("The output expression " + outputExpr + " does not return String value", ex);
                        }
                    } else {
                        throw new RuntimeException("The output expression " + outputExpr + " is illegal");
                    }
                } else {
                    throw new RuntimeException("Output: " + outputKey + " has no expression");
                }
            }
        }
        return outputs;
    }

}
