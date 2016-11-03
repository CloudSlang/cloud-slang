/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller.transformers;

import io.cloudslang.lang.compiler.modeller.result.TransformModellingResult;
import io.cloudslang.lang.entities.bindings.Output;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.cloudslang.lang.compiler.SlangTextualKeys.SENSITIVE_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.VALUE_KEY;

/**
 * Date: 11/7/2014
 *
 * @author Bonczidai Levente
 */
public class OutputsTransformer extends AbstractOutputsTransformer implements Transformer<List<Object>, List<Output>> {

    private static final List<String> KNOWN_KEYS = Arrays.asList(SENSITIVE_KEY, VALUE_KEY);

    @Override
    public TransformModellingResult<List<Output>> transform(List<Object> rawData) {
        return super.transform(rawData);
    }

    @Override
    public List<Scope> getScopes() {
        return Collections.singletonList(Scope.AFTER_EXECUTABLE);
    }

    @Override
    public String keyToTransform() {
        return null;
    }

    @Override
    void handleOutputProperties(List<Output> transformedData,
                                Map.Entry<String, ?> entry, List<RuntimeException> errors) {
        //noinspection unchecked
        addOutput(transformedData, createPropOutput((Map.Entry<String, Map<String, Serializable>>) entry), errors);
    }

    private Output createPropOutput(Map.Entry<String, Map<String, Serializable>> entry) {
        Map<String, Serializable> props = entry.getValue();
        validateKeys(entry, props);
        // default is sensitive=false
        String outputName = entry.getKey();
        boolean sensitive = props.containsKey(SENSITIVE_KEY) && (boolean) props.get(SENSITIVE_KEY);
        Serializable value = props.get(VALUE_KEY);
        if (value == null) {
            return createRefOutput(outputName, sensitive);
        }

        return createOutput(outputName, value, sensitive);
    }

    private void validateKeys(Map.Entry<String, Map<String, Serializable>> entry, Map<String, Serializable> props) {
        for (String key : props.keySet()) {
            if (!KNOWN_KEYS.contains(key)) {
                throw new RuntimeException("Key: " + key + " in output: " +
                        entry.getKey() + " is not a known property");
            }
        }
    }

}
