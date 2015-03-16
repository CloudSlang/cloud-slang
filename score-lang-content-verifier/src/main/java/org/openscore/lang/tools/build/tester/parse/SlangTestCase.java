/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package org.openscore.lang.tools.build.tester.parse;

import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by stoneo on 3/15/2015.
 **/
public class SlangTestCase {

    private final String name;

    private final String testFlowPath;

    private final String description;

    private final List<String> testSuits;

    private final String systemPropertiesFile;

    private final Map<String, Serializable> inputs;

    private final Boolean throwsException;

    private final String result;

    public static final String BASE_TEST_SUITE = "base";


    public SlangTestCase(String name, String testFlowPath, String description, List<String> testSuits,
                         String systemPropertiesFile, Map<String, Serializable> inputs,
                         Boolean throwsException, String result){
        this.name = name;
        this.testFlowPath = testFlowPath;
        this.description = description;
        this.systemPropertiesFile = systemPropertiesFile;
        if(CollectionUtils.isEmpty(testSuits)){
            this.testSuits = new ArrayList<>();
            this.testSuits.add(BASE_TEST_SUITE);
        } else {
            this.testSuits = testSuits;
        }
        this.inputs = inputs;
        this.throwsException = throwsException;
        this.result = result;
    }

    public String getName() {
        return name;
    }

    public String getTestFlowPath() {
        return testFlowPath;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTestSuits() {
        return testSuits;
    }

    public String getSystemPropertiesFile() {
        return systemPropertiesFile;
    }

    public Map<String, Serializable> getInputs() {
        return inputs;
    }

    public Boolean getThrowsException() {
        return throwsException;
    }

    public String getResult() {
        return result;
    }
}
