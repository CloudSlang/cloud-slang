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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: stoneo
 * Date: 12/11/2014
 * Time: 11:33
 */
public class AbstractOutputsTransformer {

    public List<Output> transform(List<Object> rawData) {

        List<Output> outputs = new ArrayList<>();
        if ((rawData == null) || rawData.isEmpty()){
            return outputs;
        }
        for (Object rawOutput : rawData) {
            if (rawOutput instanceof Map) {
                @SuppressWarnings("unchecked") Map.Entry<String, Serializable> entry =
                        (Map.Entry<String, Serializable>) (((Map) rawOutput).entrySet()).iterator().next();
                // - some_output: some_expression
                outputs.add(createExpressionOutput(entry.getKey(), entry.getValue()));
            } else {
                //- some_output
                //this is our default behavior that if the user specifies only a key, the key is also the ref we look for
                outputs.add(createRefOutput((String) rawOutput));
            }
        }
        return outputs;
    }

    private Output createRefOutput(String rawOutput) {
        return new Output(rawOutput, transformNameToExpression(rawOutput));
    }

    private Output createExpressionOutput(String outputName, Serializable outputExpression){
        return new Output(outputName, outputExpression);
    }

    private String transformNameToExpression(String name) {
        return ScoreLangConstants.EXPRESSION_START_DELIMITER + name + ScoreLangConstants.EXPRESSION_END_DELIMITER;
    }

}
