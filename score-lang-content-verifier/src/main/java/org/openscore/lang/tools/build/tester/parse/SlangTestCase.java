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

    private final String description;

    private final List<String> testSuits;

    private final String systemPropertiesFile;

    private final Map<String, Serializable> inputs;

    private final Boolean throwsException;

    private final String result;

    public static final String BASE_TEST_SUITE = "base";


    public SlangTestCase(String description, List<String> testSuits, String systemPropertiesFile,
                         Map<String, Serializable> inputs, Boolean throwsException, String result){
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
