/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build;

import io.cloudslang.lang.tools.build.tester.IRunTestResults;

import java.util.List;

/**
 * Created by stoneo on 4/1/2015.
 **/

/**
 * Holds the results of the CloudSlang build including the number of CloudSlang sources
 * that were compiled, and the tests results
 */
public class SlangBuildResults {

    private final int numberOfCompiledSources;
    private final IRunTestResults runTestsResults;
    private List<RuntimeException> compilationExceptions;

    public SlangBuildResults(int numberOfCompiledSources,
                             IRunTestResults runTestsResults,
                             List<RuntimeException> exceptions) {
        this.numberOfCompiledSources = numberOfCompiledSources;
        this.runTestsResults = runTestsResults;
        this.compilationExceptions = exceptions;
    }

    public int getNumberOfCompiledSources() {
        return numberOfCompiledSources;
    }

    public IRunTestResults getRunTestsResults() {
        return runTestsResults;
    }

    public List<RuntimeException> getCompilationExceptions() {
        return compilationExceptions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SlangBuildResults that = (SlangBuildResults) o;

        if (numberOfCompiledSources != that.numberOfCompiledSources) {
            return false;
        }
        return !(runTestsResults != null ?
                !runTestsResults.equals(that.runTestsResults) : that.runTestsResults != null);

    }

    @Override
    public int hashCode() {
        int result = numberOfCompiledSources;
        result = 31 * result + (runTestsResults != null ? runTestsResults.hashCode() : 0);
        return result;
    }
}
