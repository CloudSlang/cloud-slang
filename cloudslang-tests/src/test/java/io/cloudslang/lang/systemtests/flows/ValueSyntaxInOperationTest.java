/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.systemtests.flows;

import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.systemtests.StepData;
import io.cloudslang.lang.systemtests.ValueSyntaxParent;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;

import org.junit.Test;

/**
 * @author Bonczidai Levente
 * @since 11/6/2015
 */
public class ValueSyntaxInOperationTest extends ValueSyntaxParent {

    @Test
    public void testValues() throws Exception {
        Map<String, StepData> steps = prepareAndRun(getCompilationArtifact());

        // verify
        StepData operationData = steps.get(EXEC_START_PATH);

        verifyExecutableInputs(operationData);
        verifyExecutableOutputs(operationData);
        verifySuccessResult(operationData);
    }

    @Test
    public void testValuesCamelCaseDefault() throws Exception {
        Map<String, StepData> steps = prepareAndRunDefault(getCompilationArtifact());

        // verify
        StepData operationData = steps.get(EXEC_START_PATH);

        verifyExecutableInputsDefault(operationData);
        verifyExecutableOutputs(operationData);
        verifySuccessResult(operationData);
    }

    private CompilationArtifact getCompilationArtifact() throws URISyntaxException {
        // compile
        URI resource = getClass().getResource("/yaml/formats/values_op.sl").toURI();
        return slang.compile(SlangSource.fromFile(resource), new HashSet<SlangSource>());
    }

}
