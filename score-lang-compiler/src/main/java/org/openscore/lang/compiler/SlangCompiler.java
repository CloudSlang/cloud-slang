package org.openscore.lang.compiler;/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/


import org.openscore.lang.entities.CompilationArtifact;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

//todo: Eliya - add JavaDoc
public interface SlangCompiler {

	CompilationArtifact compileFlow(SlangSource source, Set<SlangSource> path);

    CompilationArtifact compile(SlangSource source, String operationName, Set<SlangSource> path);

    /**
     * Load variable sources written in slang and map them to fully qualified names
     * @param sources the slang sources containing the variables
     * @return map containing all of the variables with fully qualified keys
     */
    Map<String, ? extends Serializable> loadVariables(SlangSource... sources);

}
