/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.commons.services.impl;

import io.cloudslang.lang.commons.services.api.SlangSourceService;
import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * @author Bonczidai Levente
 * @since 8/23/2016
 */
@Service
public class SlangSourceServiceImpl implements SlangSourceService {
    @Override
    public Map<String, Value> convertInputFromMap(Map<String, ? extends Serializable> rawMap, String artifact) {
        Map<String, Value> result = new HashMap<>();
        for (Map.Entry<String, ? extends Serializable> property : rawMap.entrySet()) {
            if (property.getValue() instanceof Map) {
                @SuppressWarnings("unchecked") Map<String, ? extends Serializable> valueMap = (Map) property.getValue();
                validateKeys(valueMap, artifact);
                result.put(
                        property.getKey(),
                        ValueFactory.create(valueMap.get(SlangTextualKeys.VALUE_KEY), isSensitiveValue(valueMap))
                );
            } else {
                result.put(property.getKey(), ValueFactory.create(property.getValue(), false));
            }
        }
        return result;
    }

    private void validateKeys(Map<String, ? extends Serializable> valueMap, String artifact) {
        List<String> knownModifierKeys = Arrays.asList(SlangTextualKeys.SENSITIVE_KEY, SlangTextualKeys.VALUE_KEY);
        for (String modifierKey : valueMap.keySet()) {
            if (!knownModifierKeys.contains(modifierKey)) {
                throw new RuntimeException(
                        "Artifact {" + artifact + "} has unrecognized tag {" + modifierKey + "}" +
                                ". Please take a look at the supported features per versions link");
            }
        }
    }

    private Boolean isSensitiveValue(Map<String, ? extends Serializable> valueMap) {
        Boolean isSensitive = false;
        if (valueMap.get(SlangTextualKeys.SENSITIVE_KEY) instanceof Boolean) {
            isSensitive = (Boolean) valueMap.get(SlangTextualKeys.SENSITIVE_KEY);
        }
        return isSensitive;
    }

}
