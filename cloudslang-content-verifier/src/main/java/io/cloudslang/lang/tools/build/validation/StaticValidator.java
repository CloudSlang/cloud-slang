package io.cloudslang.lang.tools.build.validation;

import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.model.Metadata;

import java.io.File;

/**
 * Created by bancl on 8/30/2016.
 */
public interface StaticValidator {
    void validateSlangFile(File slangFile, Executable executable, Metadata sourceMetadata, boolean shouldValidateDescription);
}
