package org.openscore.lang.compiler.model;
/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/


/*
 * Created by orius123 on 05/11/14.
 */

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ParsedSlang {

    private Map<String, String> imports;
    private Map<String, Object> flow;
    private List<Map<String, Map<String, Object>>> operations;
    private Map<String, ? extends Serializable> variables;
    private String namespace;
    private String name;

    //todo add constructor?

    public String getNamespace() {
        return namespace;
    }

    public Map<String, Object> getFlow() {
        return flow;
    }

    public Map<String, String> getImports() {
        return imports;
    }

    public List<Map<String, Map<String, Object>>> getOperations() {
        return operations;
    }

    public Map<String, ? extends Serializable> getVariables() {
        return variables;
    }

    public Type getType() {
        if(flow != null) return Type.FLOW;
        if(variables != null) return Type.VARIABLES;
        return Type.OPERATIONS;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static enum Type {
        FLOW, OPERATIONS, VARIABLES
    }

}
