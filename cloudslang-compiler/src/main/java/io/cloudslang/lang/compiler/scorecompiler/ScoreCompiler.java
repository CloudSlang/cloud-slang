/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.scorecompiler;

import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.result.CompilationModellingResult;
import io.cloudslang.lang.entities.CompilationArtifact;
import java.util.List;
import java.util.Set;

/*
 * Created by stoneo on 2/2/2015.
 */

/**
 * Score Compiler - compiles CloudSlang {@link io.cloudslang.lang.compiler.modeller.model.Executable} model
 * to a {@link io.cloudslang.lang.entities.CompilationArtifact}
 * {@link io.cloudslang.lang.entities.CompilationArtifact} is an object holding
 *     \an {@link io.cloudslang.score.api.ExecutionPlan}:
 * compilation result of a workflow
 * in the score format. This object can be run on score engine.
 */
public interface ScoreCompiler {

    /**
     * Compile an {@link io.cloudslang.lang.compiler.modeller.model.Executable} and its path
     * to a {@link io.cloudslang.lang.entities.CompilationArtifact} object
     * Fails by throwing the first exception from the accumulated exceptions.
     *
     * @param source the {@link Executable} source
     * @param path   a set of {@link Executable}s containing the source dependencies
     * @return the compiled {@link io.cloudslang.lang.entities.CompilationArtifact}
     */
    CompilationArtifact compile(Executable source, Set<Executable> path);

    /**
     * Compile an {@link io.cloudslang.lang.compiler.modeller.model.Executable} and its path
     * to a {@link io.cloudslang.lang.entities.CompilationArtifact} object.
     * Does not fail but returns all the accumulated exceptions.
     *
     * @param source the {@link Executable} source
     * @param path   a set of {@link Executable}s containing the source dependencies
     * @return modelling resutl containing the compiled {@link io.cloudslang.lang.entities.CompilationArtifact}
     */
    CompilationModellingResult compileSource(Executable source, Set<Executable> path);

    /**
     * Validate that the given {@Link io.cloudslang.lang.compiler.modeller.model.Executable} is valid regarding
     * its wiring to its dependencies
     * Current validations:
     * - Validates that required inputs of the dependency have a matching input in the step
     * - Validate that every result of teh dependency has a matching navigation in the step
     *
     * @param slangModel               the CloudSlang model to validate
     * @param directDependenciesModels the CloudSlang models of the direct dependencies
     * @return a list of the exceptions that were found (if any)
     */
    List<RuntimeException> validateSlangModelWithDirectDependencies(Executable slangModel,
                                                                    Set<Executable> directDependenciesModels);

}
