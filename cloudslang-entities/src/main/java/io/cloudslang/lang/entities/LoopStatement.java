/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.entities;

import org.apache.commons.lang3.Validate;

import java.io.Serializable;

/**
 * Date: 3/25/2015
 *
 * @author Bonczidai Levente
 */
public abstract class LoopStatement implements Serializable {
    private final String expression;

    public LoopStatement(String expression) {
        Validate.notBlank(expression, "loop expression cannot be empty");

        this.expression = expression;
    }

    /**
     * only here to satisfy serialization libraries
     */
    protected LoopStatement() {
        expression = null;
    }

    public String getExpression() {
        return expression;
    }
}
