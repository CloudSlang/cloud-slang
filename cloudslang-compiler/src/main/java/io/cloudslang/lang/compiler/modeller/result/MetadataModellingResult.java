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

import io.cloudslang.lang.compiler.modeller.model.Metadata;

import java.util.List;

public class MetadataModellingResult implements ModellingResult {
    private final Metadata metadata;
    private final List<RuntimeException> errors;

    public MetadataModellingResult(Metadata metadata, List<RuntimeException> errors) {
        this.metadata = metadata;
        this.errors = errors;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    @Override
    public List<RuntimeException> getErrors() {
        return errors;
    }
}
