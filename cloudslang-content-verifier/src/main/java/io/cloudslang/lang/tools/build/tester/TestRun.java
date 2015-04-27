/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package io.cloudslang.lang.tools.build.tester;

import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;

/**
 * Created by stoneo on 4/27/2015.
 **/

/**
 * Holds a test case run - including the test case and the message outcome of the run
 */
public class TestRun {

    private SlangTestCase testCase;

    private String message;

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
}
