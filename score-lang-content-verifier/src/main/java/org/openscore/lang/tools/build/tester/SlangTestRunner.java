/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package org.openscore.lang.tools.build.tester;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.openscore.lang.compiler.SlangSource;
import org.openscore.lang.tools.build.tester.parse.SlangTestCase;
import org.openscore.lang.tools.build.tester.parse.TestCasesYamlParser;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by stoneo on 3/15/2015.
 **/
public class SlangTestRunner {

    @Autowired
    private TestCasesYamlParser parser;

    private String[] TEST_CASE_FILE_EXTENSIONS = {"yaml", "yml"};

    private final static Logger log = Logger.getLogger(SlangTestRunner.class);

    public Map<String, SlangTestCase> createTestCases(String testPath) {
        Validate.notEmpty(testPath, "You must specify a path for tests");
        Validate.isTrue(new File(testPath).isDirectory(), "Directory path argument \'" + testPath + "\' does not lead to a directory");
        Map<String, SlangTestCase> testCases = new HashMap<>();
        Collection<File> testCasesFiles = FileUtils.listFiles(new File(testPath), TEST_CASE_FILE_EXTENSIONS, true);
        log.info("Start parsing all test cases files under: " + testPath);
        log.info(testCasesFiles.size() + " test cases files were found");
        for(File testCaseFile: testCasesFiles){
            Validate.isTrue(testCaseFile.isFile(), "file path \'" + testCaseFile.getAbsolutePath() + "\' must lead to a file");
            testCases = parser.parse(SlangSource.fromFile(testCaseFile));
        }
        //todo: temp solution, until we have the data from the parse
        testCases = createMockTestCases();

        return testCases;
    }

    private Map<String, SlangTestCase> createMockTestCases() {
        Map<String, SlangTestCase> testCases = new HashMap<>();
//        SlangTestCase testCase1 = new SlangTestCase("Test case 1", null, )

        return testCases;
    }

}
