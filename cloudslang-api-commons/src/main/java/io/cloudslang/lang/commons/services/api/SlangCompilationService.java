/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.commons.services.api;

import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.modeller.result.CompilationModellingResult;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface SlangCompilationService {
    String INVALID_DIRECTORY_ERROR_MESSAGE_SUFFIX = "' is not a directory";

    List<CompilationModellingResult> compileFolders(final List<String> foldersPaths,
                                                    final CompilationHelper compilationHelper);

    File getFile(final String filePath);

    Set<SlangSource> getSourcesFromFolders(final List<String> dependencies);

    // e.g. exclude .prop.sl from .sl set
    Collection<File> listSlangFiles(final File directory, final boolean recursive);
}
