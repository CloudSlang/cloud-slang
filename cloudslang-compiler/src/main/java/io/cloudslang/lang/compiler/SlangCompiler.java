/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.compiler;

import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.SystemProperty;

import java.util.Set;

public interface SlangCompiler {

    /**
     * Compile a CloudSlang source and its path to a {@link io.cloudslang.lang.entities.CompilationArtifact} object
     * @param source the CloudSlang source file
     * @param path a set of CloudSlang sources containing the source dependencies
     * @return the compiled {@link io.cloudslang.lang.entities.CompilationArtifact}
     */
    CompilationArtifact compile(SlangSource source, Set<SlangSource> path);

    /**
     * Pre-compile a CloudSlang source into an {@link io.cloudslang.lang.compiler.modeller.model.Executable}.
     * If an error is found, an exception is thrown
     * @param source the {@link SlangSource}
     * @return an {@link io.cloudslang.lang.compiler.modeller.model.Executable} object, containing either a flow or
     * operation in the file.
     */
    Executable preCompile(SlangSource source);

    /**
     * Pre-compile a CloudSlang source into an {@link ExecutableModellingResult}.
     * All errors that are found during pre-compilation are collected (an exception is not thrown)
     * @param source the {@link SlangSource}
     * @return an {@link ExecutableModellingResult} object, containing an executable which is either a flow
     * or an operations in the file, and a list of all the errors that were found (if any).
     */
    ExecutableModellingResult preCompileSource(SlangSource source);

    Set<SystemProperty> loadSystemProperties(SlangSource source);

}
