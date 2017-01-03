/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build.commands;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.CommaParameterSplitter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class ApplicationArgs {

    @Parameter
    public List<String> parameters = new ArrayList<>();

    @Parameter(names = {"--project-root", "-pr"},
            description = "Project root directory")
    public String projectRoot;

    @Parameter(names = {"--content-root", "-cr"},
            description = "Content root directory")
    public String contentRoot;

    @Parameter(names = {"--test-root", "-tr"},
            description = "Test root directory")
    public String testRoot;

    @Parameter(names = {"--test-suites", "-ts"},
            description = "Comma-separated list of group names to be run",
            splitter = CommaParameterSplitter.class)
    public List<String> testSuites;

    @Parameter(names = {"--coverage", "-cov"},
            description = "Whether or not test coverage data should be outputted")
    public boolean coverage = false;

    @Parameter(names = {"--parallel", "-par"},
            description = "Whether or not parallel test execution should be used")
    public boolean parallel = false;

    @Parameter(names = {"--thread-count", "-th"},
            description = "Number of threads to be used in case of parallel test execution. " +
                    "Has no effect for sequential execution. By default, it is set to the number of processors.")
    public String threadCount;

    @Parameter(names = {"--run-config-file", "-rcf"},
            description = "Specifies the absolute path for the run configuration properties file.")
    public String runConfigPath;

    @Parameter(names = {"--description", "-des"},
            description = "Whether or not to validate the inputs, outputs and results have description")
    public boolean validateDescription = false;

    @Parameter(names = {"--checkstyle", "-cs"},
            description = "Whether or not to validate the checkstyle of the description")
    public boolean validateCheckstyle = false;

    @Parameter(names = {"--changes-only", "-co"},
            description = "Run only tests from active suites that were affected by this changelist")
    public String changesOnlyConfigPath;

    @Parameter(names = {"--help", "-h"}, help = true,
            description = "Display help information")
    private boolean help;

    @DynamicParameter(names = "-D", description = "Dynamic parameters go here")
    public Map<String, String> dynamicParams = new HashMap<>();

    public List<String> getParameters() {
        return parameters;
    }

    public String getProjectRoot() {
        return projectRoot;
    }

    public String getContentRoot() {
        return contentRoot;
    }

    public String getTestRoot() {
        return testRoot;
    }

    public List<String> getTestSuites() {
        return testSuites;
    }

    public boolean shouldOutputCoverage() {
        return coverage;
    }

    public boolean shouldValidateDescription() {
        return validateDescription;
    }

    public boolean shouldValidateCheckstyle() {
        return validateCheckstyle;
    }

    public boolean isHelp() {
        return help;
    }

    public Map<String, String> getDynamicParams() {
        return dynamicParams;
    }

    public boolean isParallel() {
        return parallel;
    }

    public String getThreadCount() {
        return threadCount;
    }

    public String getRunConfigPath() {
        return isEmpty(runConfigPath) ? "" : runConfigPath;
    }

    public String getChangesOnlyConfigPath() {
        return changesOnlyConfigPath;
    }
}
