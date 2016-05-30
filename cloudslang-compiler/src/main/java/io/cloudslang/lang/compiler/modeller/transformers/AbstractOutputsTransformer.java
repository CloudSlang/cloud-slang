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


import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static io.cloudslang.lang.compiler.SlangTextualKeys.VALUE_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SENSITIVE_KEY;

/**
 * User: stoneo
 * Date: 12/11/2014
 * Time: 11:33
 */
public class AbstractOutputsTransformer  extends InOutTransformer {

    public List<Output> transform(List<Object> rawData) {

        List<Output> outputs = new ArrayList<>();
        if (CollectionUtils.isEmpty(rawData)){
            return outputs;
        }
        for (Object rawOutput : rawData) {
            if (rawOutput instanceof Map) {

                @SuppressWarnings("unchecked")
                Map.Entry<String, ?> entry = ((Map<String, ?>) rawOutput).entrySet().iterator().next();
                Serializable entryValue = (Serializable) entry.getValue();
                if(entryValue == null){
                    throw new RuntimeException("Could not transform Output : " + rawOutput + " since it has a null value.\n\nMake sure a value is specified or that indentation is properly done.");
                }
                if (entryValue instanceof Map) {
                    // - some_output:
                    //     property1: value1
                    //     property2: value2
                    // this is the verbose way of defining outputs with all of the properties available
                    //noinspection unchecked
                    outputs.add(createPropOutput((Map.Entry<String, Map<String, Serializable>>) entry));
                } else {
                    // - some_output: some_expression
                    outputs.add(createOutput(entry.getKey(), entryValue, false));
                }
            } else {
                //- some_output
                //this is our default behavior that if the user specifies only a key, the key is also the ref we look for
                outputs.add(createRefOutput((String) rawOutput));
            }
        }
        return outputs;
    }

    private Output createPropOutput(Map.Entry<String, Map<String, Serializable>> entry) {
        Map<String, Serializable> props = entry.getValue();
        List<String> knownKeys = Arrays.asList(SENSITIVE_KEY, VALUE_KEY);

        for (String key : props.keySet()) {
            if (!knownKeys.contains(key)) {
                throw new RuntimeException("key: " + key + " in output: " + entry.getKey() + " is not a known property");
            }
        }

        // default is sensitive=false
        String outputName = entry.getKey();
        boolean sensitive = props.containsKey(SENSITIVE_KEY) && (boolean) props.get(SENSITIVE_KEY);
        Serializable value = props.get(VALUE_KEY);
        if (value == null) {
            throw new RuntimeException("Output: " + outputName + " was not specified");
        }

        return createOutput(outputName, value, sensitive);
    }

    private Output createOutput(String outputName, Serializable outputExpression, boolean sensitive){
        Accumulator accumulator = extractFunctionData(outputExpression);
        return new Output(
                outputName,
                ValueFactory.create(outputExpression, sensitive),
                accumulator.getFunctionDependencies(),
                accumulator.getSystemPropertyDependencies()
        );
    }

    private Output createRefOutput(String rawOutput) {
        return new Output(rawOutput, ValueFactory.create(transformNameToExpression(rawOutput)));
    }

    private String transformNameToExpression(String name) {
        return ScoreLangConstants.EXPRESSION_START_DELIMITER + name + ScoreLangConstants.EXPRESSION_END_DELIMITER;
    }

}
