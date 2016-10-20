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

import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import java.util.List;

/**
 * @author Bonczidai Levente
 * @since 8/25/2016
 */
public class ParseModellingResult implements ModellingResult {
    private final ParsedSlang parsedSlang;
    private final List<RuntimeException> errors;

    public ParseModellingResult(ParsedSlang parsedSlang, List<RuntimeException> errors) {
        this.parsedSlang = parsedSlang;
        this.errors = errors;
    }

    public ParsedSlang getParsedSlang() {
        return parsedSlang;
    }

    @Override
    public List<RuntimeException> getErrors() {
        return errors;
    }

}
