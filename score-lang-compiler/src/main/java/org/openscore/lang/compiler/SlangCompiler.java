/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.openscore.lang.compiler;

import org.openscore.lang.compiler.model.Executable;
import org.openscore.lang.entities.CompilationArtifact;

import java.util.List;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

//todo: Eliya - add JavaDoc
public interface SlangCompiler {

    CompilationArtifact compileFlow(SlangSource source, Set<SlangSource> path);

    CompilationArtifact compile(SlangSource source, String operationName, Set<SlangSource> path);

    /**
     * Pre-compile a Slang file into a list of Executables
     * @param source the slang source file
     * @return a list of Executable objects, containing either a flow or a list of all the operations in the file
     */
    List<Executable> preCompile(SlangSource source);

    /**
     * Load system property sources written in slang and map them to fully qualified names
     * @param sources the slang sources containing the system properties
     * @return map containing all of the system properties with fully qualified keys
     */
    Map<String, ? extends Serializable> loadSystemProperties(SlangSource... sources);

}
