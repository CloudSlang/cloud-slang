package org.openscore.lang.tools.build.tester.parse;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by stoneo on 3/16/2015.
 */
public class ParsedSlangTestCase implements Serializable{

    private Map<String, List> inputs;

    private Map<String, String> description;

    private Map<String, List> testSuits;

    private Map<String, String> systemPropertiesFile;

    private Map<String, Boolean> throwsException;

    private Map<String, String> result;


    public Map<String, List> getInputs() {
        return inputs;
    }

    public Map<String, String> getDescription() {
        return description;
    }

    public Map<String, List> getTestSuits() {
        return testSuits;
    }

    public Map<String, String> getSystemPropertiesFile() {
        return systemPropertiesFile;
    }

    public Map<String, Boolean> getThrowsException() {
        return throwsException;
    }

    public Map<String, String> getResult() {
        return result;
    }
}
