package io.cloudslang.lang.compiler.modeller.transformers;
/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/


/*
 * Created by orius123 on 05/11/14.
 */

import io.cloudslang.lang.entities.bindings.Argument;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;

@Component
public class DoTransformer implements Transformer<Map<String, Object>, List<Argument>> {

    @Override
    public List<Argument> transform(Map<String, Object> rawData) {
        List<Argument> result = new ArrayList<>();
        if ((rawData == null) || rawData.isEmpty()) {
            return result;
        } else if (rawData.size() > 1) {
            throw new RuntimeException("Task has to many keys under the 'do' keyword,\n" +
                    "May happen due to wrong indentation");
        }
        Map.Entry<String, Object> argumentsEntry = rawData.entrySet().iterator().next();
        Object rawArguments = argumentsEntry.getValue();
        if (rawArguments instanceof List) {
            // list syntax
            List rawArgumentsList = (List) rawArguments;
            for (Object rawArgument : rawArgumentsList) {
                Argument argument = transformListArgument(rawArgument);
                result.add(argument);
            }
        } else if (rawArguments != null) {
            throw new RuntimeException("Task arguments should be defined using a standard YAML list.");
        }

        return result;
    }

    @Override
    public List<Scope> getScopes() {
        return Collections.singletonList(Scope.BEFORE_TASK);
    }

    @Override
    public String keyToTransform() {
        return null;
    }

    private Argument transformListArgument(Object rawArgument) {
        // - some_arg
        // this is our default behaviour that if the user specifies only a key, the key is also the ref we look for
        if (rawArgument instanceof String) {
            String argumentName = (String) rawArgument;
            return new Argument(argumentName, null);
        } else if (rawArgument instanceof Map) {
            @SuppressWarnings("unchecked")
            Map.Entry<String, Serializable> entry = ((Map<String, Serializable>) rawArgument).entrySet().iterator().next();
            Serializable entryValue = entry.getValue();
            if(entryValue == null){
                throw new RuntimeException("Could not transform task argument: " +
                        rawArgument + ". Since it has a null value.\n" +
                        "Make sure a value is specified or that indentation is properly done."
                );
            }
            // - some_input: some_expression
            return new Argument(entry.getKey(), entryValue);
        }
        throw new RuntimeException("Could not transform task argument: " + rawArgument);
    }
}