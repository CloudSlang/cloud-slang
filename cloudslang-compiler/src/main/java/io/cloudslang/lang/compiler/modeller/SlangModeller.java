/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller;

import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import io.cloudslang.lang.compiler.modeller.result.ParseModellingResult;

/*
 * Created by stoneo on 2/2/2015.
 */

/**
 * Slang modeller - transforms a {@link io.cloudslang.lang.compiler.parser.model.ParsedSlang} source into
 * the Slang model - an {@link io.cloudslang.lang.compiler.modeller.model.Executable} object
 */
public interface SlangModeller {

    /**
     * Pre-compile a Slang source into an {@link ExecutableModellingResult}
     * @param parseModellingResult the {@link io.cloudslang.lang.compiler.parser.model.ParsedSlang} source
     *
     * @return an {@link ExecutableModellingResult} object, containing an executable
     *     which is either a flow or an operations in the file
     */
    ExecutableModellingResult createModel(ParseModellingResult parseModellingResult);
}
