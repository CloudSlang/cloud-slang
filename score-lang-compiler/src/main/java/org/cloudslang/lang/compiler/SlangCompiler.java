/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.cloudslang.lang.compiler;

import java.util.Set;

import org.cloudslang.lang.compiler.modeller.model.Executable;
import org.cloudslang.lang.entities.CompilationArtifact;

public interface SlangCompiler {

    /**
     * Compile a Slang source and its path to a {@link org.cloudslang.lang.entities.CompilationArtifact} object
     * @param source the slang source file
     * @param path a set of slang sources containing the source dependencies
     * @return the compiled {@link org.cloudslang.lang.entities.CompilationArtifact}
     */
    CompilationArtifact compile(SlangSource source, Set<SlangSource> path);

    /**
     * Pre-compile a Slang source into an {@link org.cloudslang.lang.compiler.modeller.model.Executable}
     * @param source the {@link SlangSource}
     * @return an {@link org.cloudslang.lang.compiler.modeller.model.Executable} object, containing either a flow or operation in the file.
     */
    Executable preCompile(SlangSource source);

}
