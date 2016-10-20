/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.cli.utils;

import io.cloudslang.lang.compiler.modeller.result.CompilationModellingResult;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.Value;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CompilerHelper {

    CompilationArtifact compile(String filePath, List<String> dependencies);

    CompilationModellingResult compileSource(String filePath, List<String> dependencies);

    List<CompilationModellingResult> compileFolders(List<String> foldersPaths);

    /**
     * Load system property sources written in yaml and map them to fully qualified names
     *
     * @param systemPropertyFiles paths to the files containing the system properties
     * @return map containing all of the system properties with fully qualified keys
     */
    Set<SystemProperty> loadSystemProperties(List<String> systemPropertyFiles);

    /**
     * Load input sources written in yaml and map them to fully qualified names
     *
     * @param inputFiles paths to the files containing the inputs
     * @return map containing all of the inputs with fully qualified keys
     */
    Map<String, Value> loadInputsFromFile(List<String> inputFiles);

}
