/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build.validation;

import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.model.Metadata;
import java.io.File;
import java.util.Queue;

public interface StaticValidator {
    void validateSlangFile(File slangFile,
                           Executable executable,
                           Metadata sourceMetadata,
                           boolean shouldValidateDescription,
                           Queue<RuntimeException> exceptions);
}
