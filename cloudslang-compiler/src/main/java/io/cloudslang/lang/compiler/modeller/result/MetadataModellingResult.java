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
import io.cloudslang.lang.compiler.modeller.model.StepMetadata;
import java.util.ArrayList;
import java.util.List;

public class MetadataModellingResult implements ModellingResult {
    private final Metadata metadata;
    private final List<StepMetadata> stepDescriptions;
    private final List<RuntimeException> errors;

    public MetadataModellingResult(
            Metadata metadata,
            List<StepMetadata> stepDescriptions,
            List<RuntimeException> errors) {
        this.metadata = metadata;
        this.stepDescriptions = stepDescriptions;
        this.errors = errors;
    }

    @Deprecated
    public MetadataModellingResult(Metadata metadata, List<RuntimeException> errors) {
        this.metadata = metadata;
        this.stepDescriptions = new ArrayList<>();
        this.errors = errors;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public List<StepMetadata> getStepDescriptions() {
        return stepDescriptions;
    }

    @Override
    public List<RuntimeException> getErrors() {
        return errors;
    }
}
