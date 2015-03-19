/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.openscore.lang.entities;

import java.io.Serializable;

/**
 * Date: 3/17/2015
 *
 * @author Bonczidai Levente
 */
public class AsyncLoopStatement implements Serializable {

    private final String varName;
    private final String listExpression;

    public AsyncLoopStatement(String varName, String listExpression) {
        this.varName = varName;
        this.listExpression = listExpression;
    }

    public String getVarName() {
        return varName;
    }

    public String getListExpression() {
        return listExpression;
    }
}
