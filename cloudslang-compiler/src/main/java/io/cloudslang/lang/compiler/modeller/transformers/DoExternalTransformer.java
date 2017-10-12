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

import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.entities.bindings.Argument;
import org.apache.commons.collections4.MapUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DoExternalTransformer extends DoTransformer implements Transformer<Map<String, Object>, List<Argument>> {

    @Override
    protected List<RuntimeException> validateRawData(Map<String, Object> rawData) {
        if (MapUtils.isEmpty(rawData)) {
            return Collections.singletonList(new RuntimeException("Step has no reference information."));
        } else if (rawData.size() > 1) {
            return Collections.singletonList(
                    new RuntimeException("Step has too many keys under the 'do_external' keyword,\n" +
                            "May happen due to wrong indentation."));
        }
        return Collections.emptyList();
    }

    @Override
    public String keyToTransform() {
        return SlangTextualKeys.DO_EXTERNAL_KEY;
    }
}
