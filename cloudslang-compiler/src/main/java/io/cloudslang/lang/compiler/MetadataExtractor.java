package io.cloudslang.lang.compiler;

import io.cloudslang.lang.compiler.modeller.model.Metadata;

/**
 * User: bancl
 * Date: 1/11/2016
 */
public interface MetadataExtractor {

    Metadata extractMetadata(SlangSource source);
}
