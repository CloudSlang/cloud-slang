/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.openscore.lang.compiler.scorecompiler;

import org.openscore.lang.compiler.modeller.model.Executable;
import org.openscore.lang.entities.CompilationArtifact;

import java.util.Set;

/*
 * Created by stoneo on 2/2/2015.
 */

/**
 * Score Compiler - compiles Slang Executable model to a CompilationArtifact
 * CompilationArtifact is an object holding an ExecutionPlan - compilation result of a workflow
 * in the score format. This object can be run on score engine.
 */
public interface ScoreCompiler {

    /**
     * Compile an Executable & its path to a CompilationArtifact object
     * @param source the executable source
     * @param path a set of executables containing the source dependencies
     * @return the compiled CompilationArtifact
     */
    public CompilationArtifact compile(Executable source, Set<Executable> path);

}
