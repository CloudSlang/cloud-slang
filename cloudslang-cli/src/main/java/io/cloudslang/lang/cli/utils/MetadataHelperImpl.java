package io.cloudslang.lang.cli.utils;

import io.cloudslang.lang.api.Slang;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.modeller.model.Metadata;
import java.io.File;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * User: bancl
 * Date: 1/11/2016
 */
@Component
public class MetadataHelperImpl implements MetadataHelper {

    private static final String NO_METADATA_TO_DISPLAY = "No metadata to display.";

    @Autowired
    private Slang slang;

    @Override
    public String extractMetadata(File file) {
        Validate.notNull(file, "File can not be null");
        Validate.notNull(file.getAbsolutePath(), "File path can not be null");
        Validate.isTrue(file.isFile(), "File: " + file.getName() + " was not found");

        Metadata metadata = slang.extractMetadata(SlangSource.fromFile(file));

        return prettyPrint(metadata);
    }

    private String prettyPrint(Metadata metadata) {
        if (emptyMetadata(metadata)) {
            return NO_METADATA_TO_DISPLAY;
        } else {
            return metadata.prettyPrint();
        }
    }

    private boolean emptyMetadata(Metadata metadata) {
        return "".equals(metadata.getDescription()) && "".equals(metadata.getPrerequisites()) &&
                (metadata.getInputs().size() == 0) && (metadata.getOutputs().size() == 0) &&
                (metadata.getResults().size() == 0);
    }
}
