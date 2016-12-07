/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller.result;

import java.util.List;
import java.util.Map;

public class ParseMetadataModellingResult implements ModellingResult {
    private final Map<String, String> parseResult;
    private final List<RuntimeException> errors;

    public ParseMetadataModellingResult(Map<String, String> parseResult, List<RuntimeException> errors) {
        this.parseResult = parseResult;
        this.errors = errors;
    }

    public Map<String, String> getParseResult() {
        return parseResult;
    }

    @Override
    public List<RuntimeException> getErrors() {
        return errors;
    }
}
