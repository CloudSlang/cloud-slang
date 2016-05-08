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
package io.cloudslang.lang.systemtests.flows;

import com.google.common.collect.Sets;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.systemtests.StepData;
import io.cloudslang.lang.systemtests.ValueSyntaxParent;
import org.junit.Test;

import java.io.Serializable;
import java.net.URI;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * InOutParam value
 *
 * Created by Ifat Gavish on 19/04/2016
 */
public class SensitiveValueSyntaxInFlowTest extends ValueSyntaxParent {

    @Test
    public void testValues() throws Exception {
        // compile
        URI resource = getClass().getResource("/yaml/formats/sensitive_values_flow.sl").toURI();
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

    private void verifyInOutParams(Map<String, Value> params) {
        for (Map.Entry<String, Value> entry : params.entrySet()) {
            String name = entry.getKey();
            Serializable value = entry.getValue() == null ? null : entry.getValue().get();
            boolean sensitive = entry.getValue() != null && entry.getValue().isSensitive();
            assertTrue(name.contains("sensitive") && sensitive || !name.contains("sensitive") && !sensitive);
            if (!name.contains("sensitive")) {
                for (Map.Entry<String, Value> otherEntry : params.entrySet()) {
                    if (otherEntry.getKey().replaceAll("_sensitive", "").equals(name)) {
                        Serializable otherValue = otherEntry.getValue() == null ? null : otherEntry.getValue().get();
                        if (value == null) {
                            assertTrue(otherValue == null || otherValue.equals("default_value"));
                        } else {
                            assertEquals(value, otherValue);
                        }
                    }
                }
            }
        }
    }
}
