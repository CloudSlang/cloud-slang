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

import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.Output;

import io.cloudslang.lang.entities.utils.ExpressionUtils;
import io.cloudslang.lang.entities.utils.MapUtils;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;

/**
 * Date: 11/7/2014
 *
 * @author Bonczidai Levente
 */
@Component
public class OutputsBinding {

    @Autowired
    ScriptEvaluator scriptEvaluator;

    public Map<String, Serializable> bindOutputs(
            Map<String, Serializable> initialContext,
            Map<String, Serializable> returnContext,
            Set<SystemProperty> systemProperties,
            List<Output> possibleOutputs) {

        Map<String, Serializable> outputs = new LinkedHashMap<>();
        Map<String, Serializable> scriptContext = MapUtils.mergeMaps(initialContext, returnContext);

        if (possibleOutputs != null) {
            for (Output output : possibleOutputs) {
                String outputKey = output.getName();
                Serializable rawValue = output.getValue();
                Serializable valueToAssign = rawValue;
                String expressionToEvaluate = ExpressionUtils.extractExpression(rawValue);
                if (expressionToEvaluate != null) {
                    // initialize with null value if key does not exist
                    scriptContext.put(outputKey, scriptContext.get(outputKey));
                    try {
                        //evaluate expression
                        valueToAssign = scriptEvaluator.evalExpr(expressionToEvaluate, scriptContext, systemProperties, output.getFunctionDependencies());
                    } catch (Throwable t) {
                        throw new RuntimeException("Error binding output: '" + output.getName() + "',\n\tError is: " + t.getMessage(), t);
                    }
                }
                try {
                    outputs.put(outputKey, valueToAssign);
                    scriptContext.put(outputKey, valueToAssign);
                } catch (ClassCastException ex) {
                    throw new RuntimeException("The output value: " + rawValue + " is not serializable", ex);
                }
            }
        }
        return outputs;
    }

}
