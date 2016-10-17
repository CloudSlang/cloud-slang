/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.systemtests.sensitive;

import com.google.common.collect.Sets;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.systemtests.StepData;
import io.cloudslang.lang.systemtests.ValueSyntaxParent;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * Sensitive values in python expressions
 * <p>
 * Created by Ifat Gavish on 19/04/2016
 */
public class SensitiveValuesInPythonExpressionsFlowTest extends ValueSyntaxParent {

    @Test
    public void testValues() throws Exception {
        // compile
        URI resource = getClass().getResource("/yaml/sensitive/sensitive_values_in_python_expressions_flow.sl").toURI();
        URI op1 = getClass().getResource("/yaml/noop.sl").toURI();
        URI op2 = getClass().getResource("/yaml/print.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(op1), SlangSource.fromFile(op2));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

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
