package org.openscore.lang.cli.utils;

/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/


import org.openscore.lang.entities.CompilationArtifact;
import java.io.IOException;
import java.util.List;

public interface CompilerHelper {
    public CompilationArtifact compile(String filePath, String opName, List<String> dependencies) throws IOException;
}
