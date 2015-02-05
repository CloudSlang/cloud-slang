/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.openscore.lang.compiler.modeller;

import org.openscore.lang.compiler.modeller.model.Executable;
import org.openscore.lang.compiler.parser.model.ParsedSlang;

/**
 * Created by stoneo on 2/2/2015.
 */
public interface SlangModeller {
    /**
     * Pre-compile a Slang file into a list of Executables
     * @param parsedSlang the parsed Slang source file
     * @return a list of Executable objects, containing either a flow or a list of all the operations in the file
     */
    public Executable createModel(ParsedSlang parsedSlang);
}
