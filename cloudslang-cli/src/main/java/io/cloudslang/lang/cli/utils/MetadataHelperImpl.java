package io.cloudslang.lang.cli.utils;

import io.cloudslang.lang.api.Slang;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.modeller.model.Metadata;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * User: bancl
 * Date: 1/11/2016
 */
@Component
public class MetadataHelperImpl implements MetadataHelper {

    @Autowired
    private Slang slang;

    @Override
    public Metadata extractMetadata(File file) throws IOException {
        Validate.notNull(file.getAbsolutePath(), "File path can not be null");
        Validate.isTrue(file.isFile(), "File: " + file.getName() + " was not found");

        return slang.extractMetadata(SlangSource.fromFile(file));
    }
}
