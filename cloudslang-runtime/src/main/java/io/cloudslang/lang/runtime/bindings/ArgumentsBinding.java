/**
 * ****************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 * <p/>
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * *****************************************************************************
 */
package io.cloudslang.lang.runtime.bindings;

import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.utils.ExpressionUtils;
import io.cloudslang.lang.runtime.bindings.scripts.ScriptEvaluator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Bonczidai Levente
 * @since 8/17/2015
 */
@Component
public class ArgumentsBinding extends AbstractBinding {

    @Autowired
    private ScriptEvaluator scriptEvaluator;

    public Map<String, Value> bindArguments(
            List<Argument> arguments,
            Map<String, ? extends Value> context,
            Set<SystemProperty> systemProperties) {
        Map<String, Value> resultContext = new HashMap<>();

        //we do not want to change original context map
        Map<String, Value> srcContext = new HashMap<>(context);

        for (Argument argument : arguments) {
            bindArgument(argument, srcContext, systemProperties, resultContext);
        }

        return resultContext;
    }

    private void bindArgument(
            Argument argument,
            Map<String, ? extends Value> srcContext,
            Set<SystemProperty> systemProperties,
            Map<String, Value> targetContext) {
        Value inputValue;
        String inputName = argument.getName();
        String errorMessagePrefix = "Error binding step input: '" + inputName;

        try {
            inputValue = srcContext.get(inputName);
            if (argument.isPrivateArgument()) {
                Value rawValue = argument.getValue();
                String expressionToEvaluate = ExpressionUtils.extractExpression(rawValue == null ? null : rawValue.get());
                if (expressionToEvaluate != null) {
                    //we do not want to change original context map
                    Map<String, Value> scriptContext = new HashMap<>(srcContext);
                    scriptContext.put(inputName, inputValue);
                    //so you can resolve previous arguments already bound
                    scriptContext.putAll(targetContext);
                    inputValue = scriptEvaluator.evalExpr(expressionToEvaluate, scriptContext, systemProperties, argument.getFunctionDependencies());
                } else {
                    inputValue = rawValue;
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException(errorMessagePrefix + "', \n\tError is: " + t.getMessage(), t);
        }
        validateStringValue(errorMessagePrefix, inputValue);
        targetContext.put(inputName, inputValue);
    }

}
