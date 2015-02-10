package org.openscore.lang.compiler.modeller.model;/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/


import java.io.Serializable;
import java.util.Map;

/*
 * Created by orius123 on 06/11/14.
 */
public class Task {

    private final String name;
    private final Map<String, Serializable> preTaskActionData;
    private final Map<String, Serializable> postTaskActionData;
    private final Map<String, String> navigationStrings;
    private final String refId;

    public Task(String name, Map<String, Serializable> preTaskActionData, Map<String, Serializable> postTaskActionData, Map<String, String> navigationStrings, String refId) {
        this.name = name;
        this.preTaskActionData = preTaskActionData;
        this.postTaskActionData = postTaskActionData;
        this.navigationStrings = navigationStrings;
        this.refId = refId;
    }

    public String getName() {
        return name;
    }

    public Map<String, Serializable> getPreTaskActionData() {
        return preTaskActionData;
    }

    public Map<String, Serializable> getPostTaskActionData() {
        return postTaskActionData;
    }

    public Map<String, String> getNavigationStrings() {
        return navigationStrings;
    }

    public String getRefId() {
        return refId;
    }
}
