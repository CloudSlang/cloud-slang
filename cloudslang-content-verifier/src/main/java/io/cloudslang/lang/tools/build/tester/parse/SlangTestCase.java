/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build.tester.parse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.cloudslang.lang.tools.build.SlangBuildMain;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

/**
 * Created by stoneo on 3/15/2015.
 **/
public class SlangTestCase implements Serializable {
    private static final String UNKNOWN_FILE_PATH = "<unknown file path>";

    private String name;

    private String testFlowPath;

    private String description;

    private List<String> testSuites;

    private String systemPropertiesFile;

    @JsonIgnore
    private List<Map> inputs;

    private List<Map> outputs;

    private Boolean throwsException;

    private String result;

    @JsonIgnore
    private String filePath;

    //for jackson
    private SlangTestCase() {
    }

    public SlangTestCase(String name, String testFlowPath, String description, List<String> testSuites,
                         String systemPropertiesFile, List<Map> inputs, List<Map> outputs,
                         Boolean throwsException, String result) {
        this.name = name;
        this.testFlowPath = testFlowPath;
        this.description = description;
        this.systemPropertiesFile = systemPropertiesFile;
        this.testSuites = testSuites;
        this.inputs = inputs;
        this.outputs = outputs;
        this.throwsException = throwsException;
        this.result = result;
        this.filePath = null;
    }

    public SlangTestCase(String name, String testFlowPath, String description, List<String> testSuites,
                         String systemPropertiesFile, List<Map> inputs, List<Map> outputs,
                         Boolean throwsException, String result, String filePath) {
        this.name = name;
        this.testFlowPath = testFlowPath;
        this.description = description;
        this.systemPropertiesFile = systemPropertiesFile;
        this.testSuites = testSuites;
        this.inputs = inputs;
        this.outputs = outputs;
        this.throwsException = throwsException;
        this.result = result;
        this.filePath = filePath;
    }

    public static String generateTestCaseReference(SlangTestCase slangTestCase) {
        return slangTestCase.getName() + " [" + (StringUtils.isEmpty(slangTestCase.getFilePath()) ?
                UNKNOWN_FILE_PATH : slangTestCase.getFilePath()) + "]";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTestFlowPath() {
        return testFlowPath;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTestSuites() {
        List<String> localTestSuites = this.testSuites;
        return isEmpty(localTestSuites) ? newArrayList(SlangBuildMain.DEFAULT_TESTS) : newArrayList(localTestSuites);
    }


    public String getSystemPropertiesFile() {
        return systemPropertiesFile;
    }

    public void setInputs(List<Map> inputs) {
        this.inputs = inputs;
    }

    public List<Map> getInputs() {
        return inputs;
    }

    public List<Map> getOutputs() {
        return outputs;
    }

    public Boolean getThrowsException() {
        return throwsException;
    }

    public void setThrowsException(boolean throwsException) {
        this.throwsException = throwsException;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }


    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
