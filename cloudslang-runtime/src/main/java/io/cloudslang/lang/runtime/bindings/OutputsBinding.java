/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
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
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.runtime.steps.ReadOnlyContextAccessor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.cloudslang.lang.entities.utils.ExpressionUtils.extractExpression;

/**
 * Date: 11/7/2014
 *
 * @author Bonczidai Levente
 */
@Component
public class OutputsBinding extends AbstractBinding {

    public Map<String, Value> bindOutputs(
            ReadOnlyContextAccessor contextAccessor,
            Set<SystemProperty> systemProperties,
            List<Output> possibleOutputs) {

        Map<String, Value> outputs = new LinkedHashMap<>();
        Map<String, Value> context = contextAccessor.getMergedContexts();

        if (possibleOutputs != null) {
            for (Output output : possibleOutputs) {
                String outputKey = output.getName();
                String errorMessagePrefix = "Binding output: '" + output.getName() + " failed";
                Value rawValue = output.getValue();
                Value valueToAssign = rawValue;
                String expressionToEvaluate = extractExpression(rawValue == null ? null : rawValue.get());
                if (expressionToEvaluate != null) {
                    // initialize with null value if key does not exist
                    context.put(outputKey, context.get(outputKey));
                    try {
                        //evaluate expression
                        Value value = scriptEvaluator.evalExpr(expressionToEvaluate, context,
                                systemProperties, output.getFunctionDependencies());
                        valueToAssign = ValueFactory.create(value, rawValue != null && rawValue.isSensitive());
                    } catch (Throwable t) {
                        throw new RuntimeException(errorMessagePrefix + "',\n\t" + t.getMessage(), t);
                    }
                }
                validateStringValue(errorMessagePrefix, valueToAssign);
                outputs.put(outputKey, valueToAssign);
                context.put(outputKey, valueToAssign);
            }
        }
        return outputs;
    }
}
