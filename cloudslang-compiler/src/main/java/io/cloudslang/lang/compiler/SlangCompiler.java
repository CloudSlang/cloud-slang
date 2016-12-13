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

import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.result.CompilationModellingResult;
import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import io.cloudslang.lang.compiler.modeller.result.SystemPropertyModellingResult;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.SystemProperty;
import java.util.List;
import java.util.Set;

public interface SlangCompiler {

    /**
     * Compile a CloudSlang source and its path to a
     *     {@link io.cloudslang.lang.entities.CompilationArtifact} object
     *
     * @param source the CloudSlang source file
     * @param path   a set of CloudSlang sources containing the source dependencies
     * @return the compiled {@link io.cloudslang.lang.entities.CompilationArtifact}
     */
    CompilationArtifact compile(SlangSource source, Set<SlangSource> path);

    /**
     * Compile a CloudSlang source and its path to a
     *     {@link io.cloudslang.lang.entities.CompilationArtifact} object
     *
     * @param source the CloudSlang source file
     * @param path   a set of CloudSlang sources containing the source dependencies
     * @param precompileStrategy with / without cache
     * @return the compiled {@link io.cloudslang.lang.entities.CompilationArtifact}
     */
    CompilationArtifact compile(SlangSource source, Set<SlangSource> path, PrecompileStrategy precompileStrategy);

    /**
     * Compile a CloudSlang source and its dependencies to a
     *     {@link io.cloudslang.lang.entities.CompilationArtifact} object
     *
     * @param source the CloudSlang source file
     * @param path   a set of CloudSlang sources containing the source dependencies
     * @return the compiled {@link CompilationModellingResult} object, containing an compilation artifact, and a list
     *     of all the errors that were found(if any).
     */
    CompilationModellingResult compileSource(SlangSource source, Set<SlangSource> path);

    /**
     * Compile a CloudSlang source and its dependencies to a
     *     {@link io.cloudslang.lang.entities.CompilationArtifact} object
     *
     * @param source the CloudSlang source file
     * @param path   a set of CloudSlang sources containing the source dependencies
     * @param precompileStrategy with / without cache
     * @return the compiled {@link CompilationModellingResult} object, containing an compilation artifact, and a list
     *     of all the errors that were found(if any).
     */
    CompilationModellingResult compileSource(
            SlangSource source, Set<SlangSource> path,
            PrecompileStrategy precompileStrategy);

    /**
     * Pre-compile a CloudSlang source into an {@link io.cloudslang.lang.compiler.modeller.model.Executable}.
     * If an error is found, an exception is thrown
     *
     * @param source the {@link SlangSource}
     * @return an {@link io.cloudslang.lang.compiler.modeller.model.Executable} object, containing either a flow or
     *     operation in the file.
     */
    Executable preCompile(SlangSource source);

    /**
     * Pre-compile a CloudSlang source into an {@link io.cloudslang.lang.compiler.modeller.model.Executable}.
     * If an error is found, an exception is thrown
     *
     * @param source the {@link SlangSource}
     * @param precompileStrategy with / without cache
     * @return an {@link io.cloudslang.lang.compiler.modeller.model.Executable} object, containing either a flow or
     *     operation in the file.
     */
    Executable preCompile(SlangSource source, PrecompileStrategy precompileStrategy);

    /**
     * Pre-compile a CloudSlang source into an {@link ExecutableModellingResult}.
     * All errors that are found during pre-compilation are collected (an exception is not thrown)
     *
     * @param source the {@link SlangSource}
     * @return an {@link ExecutableModellingResult} object, containing an executable which is either a flow
     *     or an operations in the file, and a list of all the errors that were found (if any).
     */
    ExecutableModellingResult preCompileSource(SlangSource source);

    /**
     * Pre-compile a CloudSlang source into an {@link ExecutableModellingResult}.
     * All errors that are found during pre-compilation are collected (an exception is not thrown)
     *
     * @param source             the {@link SlangSource}
     * @param precompileStrategy whether to use caching in pre-compile.
     * @return an {@link ExecutableModellingResult} object, containing an executable which is either a flow
     *     or an operations in the file, and a list of all the errors that were found (if any).
     */
    ExecutableModellingResult preCompileSource(SlangSource source, PrecompileStrategy precompileStrategy);

    /**
     * Remove all elements in pre-compile cache. No-cached calls are not affected.
     */
    void invalidateAllInPreCompileCache();

    /**
     * Validate that the given {@Link io.cloudslang.lang.compiler.modeller.model.Executable} is valid regarding
     * its wiring to its dependencies
     * Current validations:
     * - Validates that (required, non private, non empty) inputs of the dependency have a matching input in the step
     * - Validate step navigation section declares the exact set of dependency results
     * - Validate step input names are different from dependency output names
     * - Validate break section results are declared in dependency
     *
     * @param slangModel               the CloudSlang model to validate
     * @param directDependenciesModels the CloudSlang models of the direct dependencies
     * @return a list of the exceptions that were found (if any)
     */
    List<RuntimeException> validateSlangModelWithDirectDependencies(Executable slangModel,
                                                                    Set<Executable> directDependenciesModels);

    Set<SystemProperty> loadSystemProperties(SlangSource source);

    SystemPropertyModellingResult loadSystemPropertiesFromSource(SlangSource source);
}
