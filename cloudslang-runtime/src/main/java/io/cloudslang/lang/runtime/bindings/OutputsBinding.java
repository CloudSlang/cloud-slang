/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.lang.runtime.bindings;

import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Output;

import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Date: 11/7/2014
 *
 * @author Bonczidai Levente
 */
@Component
public class OutputsBinding {

    @Autowired
    ScriptEvaluator scriptEvaluator;

    public Map<String, Serializable> bindOutputs(Map<String, Serializable> inputs,
                                           Map<String, Serializable> actionReturnValues,
                                           List<Output> possibleOutputs) {

        Map<String, Serializable> outputs = new LinkedHashMap<>();
        //construct script context
        Map<String, Serializable> scriptContext = new HashMap<>();
        //put action outputs
        scriptContext.putAll(actionReturnValues);
        if (possibleOutputs != null) {
            for (Output output : possibleOutputs) {
                String outputKey = output.getName();
                String outputExpr = output.getExpression();
                if (outputExpr != null) {
                    //declare the new output
                    if (!actionReturnValues.containsKey(outputKey)) {
                        scriptContext.put(outputKey, null);
                    }
                    //put operation inputs as a map
                    if(MapUtils.isNotEmpty(inputs)) {
                        scriptContext.put(ScoreLangConstants.BIND_OUTPUT_FROM_INPUTS_KEY, (Serializable) inputs);
                    }

                    Serializable scriptResult;
                    try {
                        //evaluate expression
                        scriptResult = scriptEvaluator.evalExpr(outputExpr, scriptContext);
                    } catch (Throwable t) {
                        throw new RuntimeException("Error binding output: '" + output.getName() + "',\n\tError is: " + t.getMessage(), t);
                    }

                    try {
                        outputs.put(outputKey, scriptResult);
                        scriptContext.put(outputKey, scriptResult);
                    } catch (ClassCastException ex) {
                        throw new RuntimeException("The output expression " + outputExpr + " does not return serializable value", ex);
                    }
                } else {
                    throw new RuntimeException("Output: " + outputKey + " has no expression");
                }
            }
        }
        return outputs;
    }

}
