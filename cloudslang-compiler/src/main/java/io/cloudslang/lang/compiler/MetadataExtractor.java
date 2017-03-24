/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler;

import io.cloudslang.lang.compiler.modeller.model.Metadata;
import io.cloudslang.lang.compiler.modeller.result.MetadataModellingResult;
import java.util.List;

public interface MetadataExtractor {

    Metadata extractMetadata(SlangSource source);

    Metadata extractMetadata(SlangSource source, boolean shouldValidateDescription);

    MetadataModellingResult extractMetadataModellingResult(SlangSource source);

    MetadataModellingResult extractMetadataModellingResult(SlangSource source, boolean shouldValidateCheckstyle);

    List<RuntimeException> validateCheckstyle(SlangSource source);

}
