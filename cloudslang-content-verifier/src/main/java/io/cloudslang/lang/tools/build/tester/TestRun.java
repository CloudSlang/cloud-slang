/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build.tester;

import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;

/**
 * Created by stoneo on 4/27/2015.
 **/

/**
 * Holds a test case run - including the test case and the message outcome of the run
 */
public class TestRun {

    private final SlangTestCase testCase;

    private final String message;

    public TestRun(SlangTestCase testCase, String message) {
        this.testCase = testCase;
        this.message = message;
    }

    public SlangTestCase getTestCase() {
        return testCase;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TestRun testRun = (TestRun) o;

        if (testCase != null ? !testCase.equals(testRun.testCase) : testRun.testCase != null) {
            return false;
        }
        return !(message != null ? !message.equals(testRun.message) : testRun.message != null);

    }

    @Override
    public int hashCode() {
        int result = testCase != null ? testCase.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }
}
