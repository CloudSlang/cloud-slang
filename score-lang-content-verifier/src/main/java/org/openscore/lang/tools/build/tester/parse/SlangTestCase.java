package org.openscore.lang.tools.build.tester.parse;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by stoneo on 3/15/2015.
 **/
public class SlangTestCase {

    private final String description;

    private final String testSuits;

    private final String systemPropertiesFile;

    private final Map<String, Serializable> inputs;

    private final Boolean throwsException;

    private final String result;


    public SlangTestCase(String description, String testSuits, String systemPropertiesFile,
                         Map<String, Serializable> inputs, Boolean throwsException, String result){
        this.description = description;
        this.testSuits = testSuits;
        this.systemPropertiesFile = systemPropertiesFile;
        this.inputs = inputs;
        this.throwsException = throwsException;
        this.result = result;
    }

    public String getDescription() {
        return description;
    }

    public String getTestSuits() {
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
}
