/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.parser.model;

import io.cloudslang.lang.compiler.Extension;

import java.util.Map;

/*
 * Created by orius123 on 05/11/14.
 */
public class ParsedSlang {

    private Map<String, String> imports;
    private Map<String, Object> flow;
    private Map<String, Object> operation;
    private Map<String, Object> decision;
    private Object properties;
    private String namespace;
    private String name;
    private Extension fileExtension;
    private Object extensions;

    public String getNamespace() {
        return namespace;
    }

    public Map<String, Object> getFlow() {
        return flow;
    }

    public Map<String, String> getImports() {
        return imports;
    }

    public Map<String, Object> getOperation() {
        return operation;
    }

    public Object getProperties() {
        return properties;
    }

    public Object getExtensions() {
        return extensions;
    }

    public Map<String, Object> getDecision() {
        return decision;
    }

    public Type getType() {
        if (flow != null) {
            return Type.FLOW;
        }
        if (operation != null) {
            return Type.OPERATION;
        }
        if (decision != null) {
            return Type.DECISION;
        }
        if (properties != null) {
            return Type.SYSTEM_PROPERTY_FILE;
        }
        throw new RuntimeException(
                "Source " + name + " has no content associated with " +
                        Type.FLOW.key() + "/" +
                        Type.OPERATION.key() + "/" +
                        Type.DECISION.key() + "/" +
                        Type.SYSTEM_PROPERTY_FILE.key() + " property."
        );
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Extension getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(Extension extension) {
        this.fileExtension = extension;
    }

    public enum Type {
        FLOW("flow"),
        OPERATION("operation"),
        DECISION("decision"),
        SYSTEM_PROPERTY_FILE("properties");

        private String key;

        Type(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

}
