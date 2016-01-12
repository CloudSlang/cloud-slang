package io.cloudslang.lang.cli.utils;

import io.cloudslang.lang.compiler.modeller.model.Metadata;

import java.io.File;
import java.io.IOException;

/**
 * User: bancl
 * Date: 1/11/2016
 */
public interface MetadataHelper {

    Metadata extractMetadata(File file) throws IOException;

}
