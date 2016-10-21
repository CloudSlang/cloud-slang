/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.systemtests;

import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.Value;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.Test;
import org.springframework.stereotype.Component;

import static io.cloudslang.lang.compiler.SlangSource.fromFile;
import static org.junit.Assert.assertEquals;

/**
 * Simple operation test
 * <p>
 * Created by Ifat Gavish on 30/05/2016
 */
@Component
public class SimpleOperationTest extends SystemsTestsParent {

    @Test
    public void testEqualsOperation() throws Exception {

        URI resource = getClass().getResource("/yaml/check_equals.sl").toURI();
        CompilationArtifact compilationArtifact = slang.compile(fromFile(resource), new HashSet<SlangSource>());

        Map<String, Value> userInputs = new HashMap<>();
        Map<String, StepData> steps =
                triggerWithData(compilationArtifact, userInputs, new HashSet<SystemProperty>()).getSteps();

        StepData data = steps.get(EXEC_START_PATH);
        assertEquals(data.getOutputs().get("return_result"), "Parsing successful.");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSortListOperation() throws Exception {

        URI resource = getClass().getResource("/yaml/check_sort_list.sl").toURI();
        CompilationArtifact compilationArtifact = slang.compile(fromFile(resource), new HashSet<SlangSource>());

        Map<String, Value> userInputs = new HashMap<>();
        Map<String, StepData> steps =
                triggerWithData(compilationArtifact, userInputs, new HashSet<SystemProperty>()).getSteps();

        StepData data = steps.get(EXEC_START_PATH);
        String outputs = (String) data.getOutputs().get("result");
        assertEquals(outputs, "['element1', 'element2', 'element3', 'element4', 'element5', 'element6', 'element7']");
    }
}
