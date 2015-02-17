/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.openscore.lang.compiler;

import org.openscore.lang.compiler.modeller.model.Executable;
import org.openscore.lang.entities.CompilationArtifact;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public interface SlangCompiler {

    /**
     * Compile a Slang source and its path to a {@link org.openscore.lang.entities.CompilationArtifact} object
     * @param source the slang source file
     * @param path a set of slang sources containing the source dependencies
     * @return the compiled {@link org.openscore.lang.entities.CompilationArtifact}
     */
    CompilationArtifact compile(SlangSource source, Set<SlangSource> path);

    /**
     * Pre-compile a Slang source into an {@link org.openscore.lang.compiler.modeller.model.Executable}
     * @param source the {@link org.openscore.lang.compiler.SlangSource}
     * @return an {@link org.openscore.lang.compiler.modeller.model.Executable} object, containing either a flow or operation in the file.
     *          Returns null if the source contains Slang system properties
     */
    Executable preCompile(SlangSource source);

    /**
     * Load system property sources written in slang and map them to fully qualified names
     * @param sources the {@link org.openscore.lang.compiler.SlangSource}s containing the system properties
     * @return map containing all of the system properties with fully qualified keys
     */
    Map<String, ? extends Serializable> loadSystemProperties(SlangSource... sources);

    /**
     * Load input files sources written in slang and map them to fully qualified names
     * @param sources the {@link org.openscore.lang.compiler.SlangSource}s containing the input files
     * @return map containing all of the inputs with fully qualified keys
     */
    Map<String, Serializable> loadFileInputs(SlangSource... sources);


}
