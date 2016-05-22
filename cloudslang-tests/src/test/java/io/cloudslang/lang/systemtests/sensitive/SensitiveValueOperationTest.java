/**
 * ****************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 * <p/>
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * *****************************************************************************
 */
package io.cloudslang.lang.systemtests.sensitive;

import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.systemtests.StepData;
import io.cloudslang.lang.systemtests.ValueSyntaxParent;
import org.junit.Test;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;

/**
 * Sensitive value test
 *
 * Created by Ifat Gavish on 19/04/2016
 */
public class SensitiveValueOperationTest extends ValueSyntaxParent {

    @Test
    public void testValues() throws Exception {
        // compile
        URI resource = getClass().getResource("/yaml/sensitive/sensitive_values_op.sl").toURI();
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), new HashSet<SlangSource>());

        // trigger
        Map<String, StepData> steps = prepareAndRun(compilationArtifact);

        // verify
        StepData flowData = steps.get(EXEC_START_PATH);
        StepData stepData = steps.get(FIRST_STEP_PATH);

        verifyInOutParams(flowData.getInputs());
        verifyInOutParams(flowData.getOutputs());
        verifyInOutParams(stepData.getInputs());
        verifyInOutParams(stepData.getOutputs());
        verifySuccessResult(flowData);
    }
}
