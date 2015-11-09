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

import io.cloudslang.lang.entities.bindings.Argument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Bonczidai Levente
 * @since 8/17/2015
 */
@Component
public class ArgumentsBinding extends Binding {

    @Autowired
    private ScriptEvaluator scriptEvaluator;

    public Map<String, Serializable> bindArguments(
            List<Argument> arguments,
            Map<String, ? extends Serializable> context) {
        Map<String, Serializable> resultContext = new HashMap<>();

        //we do not want to change original context map
        Map<String, Serializable> srcContext = new HashMap<>(context);

        for (Argument argument : arguments) {
            bindArgument(argument, srcContext, resultContext);
        }

        return resultContext;
    }

    private void bindArgument(
            Argument argument,
            Map<String, ? extends Serializable> srcContext,
            Map<String, Serializable> targetContext) {
        Serializable argumentValue;
        String argumentName = argument.getName();

        try {
            Serializable rawExpression = argument.getExpression();
            String expressionToEvaluate = extractExpression(rawExpression);
            if (expressionToEvaluate != null) {
                //we do not want to change original context map
                Map<String, Serializable> scriptContext = new HashMap<>(srcContext);

                argumentValue = srcContext.get(argumentName);
                scriptContext.put(argumentName, argumentValue);

                //so you can resolve previous arguments already bound
                scriptContext.putAll(targetContext);
                argumentValue = scriptEvaluator.evalExpr(expressionToEvaluate, scriptContext);
            } else {
                argumentValue = rawExpression;
            }
        } catch (Throwable t) {
            throw new RuntimeException("Error binding task argument: '" + argumentName + "', \n\tError is: " + t.getMessage(), t);
        }

        targetContext.put(argumentName, argumentValue);
    }

}
