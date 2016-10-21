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

import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.systemtests.StepData;
import io.cloudslang.lang.systemtests.ValueSyntaxParent;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;

import org.junit.Test;

import static io.cloudslang.lang.compiler.SlangSource.fromFile;

/**
 * Sensitive values in python expressions
 * <p>
 * Created by Ifat Gavish on 19/04/2016
 */
public class SensitiveValuesInPythonExpressionsOperationTest extends ValueSyntaxParent {

    @Test
    public void testValues() throws Exception {
        // compile
        URI resource = getClass().getResource("/yaml/sensitive/sensitive_values_in_python_expressions_op.sl").toURI();
        CompilationArtifact compilationArtifact = slang.compile(fromFile(resource), new HashSet<SlangSource>());

        // trigger
        Map<String, StepData> steps = prepareAndRun(compilationArtifact);

        // verify
        StepData data = steps.get(EXEC_START_PATH);

        verifyInOutParams(data.getInputs());
        verifyInOutParams(data.getOutputs());
    }
}
