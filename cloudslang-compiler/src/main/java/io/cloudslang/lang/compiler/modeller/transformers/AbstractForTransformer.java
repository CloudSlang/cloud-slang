/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller.transformers;

import io.cloudslang.lang.compiler.validator.ExecutableValidator;

public abstract class AbstractForTransformer extends AbstractInOutForTransformer {

    private ExecutableValidator executableValidator;

    // case: value in variable_name
    protected static final String FOR_REGEX = "^(\\s+)?(\\w+)\\s+(in)\\s+(\\w+)(\\s+)?$";
    // case: key, value
    protected static final String KEY_VALUE_PAIR_REGEX = "^(\\s+)?(\\w+)(\\s+)?(,)(\\s+)?(\\w+)(\\s+)?$";
    protected static final String FOR_IN_KEYWORD = " in ";

    protected void validateLoopStatementVariable(String name) {
        executableValidator.validateLoopStatementVariable(name);
    }

    public void setExecutableValidator(ExecutableValidator executableValidator) {
        this.executableValidator = executableValidator;
    }
}
