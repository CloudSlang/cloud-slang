/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller.model;

import java.io.Serializable;
import java.util.Objects;

public class SeqStep implements Serializable {
    private static final long serialVersionUID = -628265720682478108L;
    private String id;
    private String name;
    private String objectPath;
    private String action;
    private String args;
    private String defaultArgs;
    private String highlightId;
    private String snapshot;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObjectPath() {
        return objectPath;
    }

    public void setObjectPath(String objectPath) {
        this.objectPath = objectPath;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public String getHighlightId() {
        return highlightId;
    }

    public void setHighlightId(String highlightId) {
        this.highlightId = highlightId;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }

    public String getDefaultArgs() {
        return this.defaultArgs;
    }

    public void setDefaultArgs(String defaultArgs) {
        this.defaultArgs = defaultArgs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SeqStep seqStep = (SeqStep) o;
        return Objects.equals(id, seqStep.id) &&
                Objects.equals(name, seqStep.name) &&
                Objects.equals(objectPath, seqStep.objectPath) &&
                Objects.equals(action, seqStep.action) &&
                Objects.equals(args, seqStep.args) &&
                Objects.equals(defaultArgs, seqStep.defaultArgs) &&
                Objects.equals(highlightId, seqStep.highlightId) &&
                Objects.equals(snapshot, seqStep.snapshot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, objectPath, action, args, defaultArgs, highlightId, snapshot);
    }
}
