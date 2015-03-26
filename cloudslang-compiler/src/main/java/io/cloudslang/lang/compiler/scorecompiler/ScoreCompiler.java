/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.compiler.scorecompiler;

import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.entities.CompilationArtifact;

import java.util.Set;

/*
 * Created by stoneo on 2/2/2015.
 */

/**
 * Score Compiler - compiles CloudSlang {@link io.cloudslang.lang.compiler.modeller.model.Executable} model
 * to a {@link io.cloudslang.lang.entities.CompilationArtifact}
 * {@link io.cloudslang.lang.entities.CompilationArtifact} is an object holding an {@link io.cloudslang.api.ExecutionPlan}:
 * compilation result of a workflow
 * in the score format. This object can be run on score engine.
 */
public interface ScoreCompiler {

    /**
     * Compile an {@link io.cloudslang.lang.compiler.modeller.model.Executable} and its path
     * to a {@link io.cloudslang.lang.entities.CompilationArtifact} object
     * @param source the {@link io.cloudslang.lang.compiler.modeller.model.Executable} source
     * @param path a set of {@link io.cloudslang.lang.compiler.modeller.model.Executable}s containing the source dependencies
     * @return the compiled {@link io.cloudslang.lang.entities.CompilationArtifact}
     */
    CompilationArtifact compile(Executable source, Set<Executable> path);

}
