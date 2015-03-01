/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/


package org.openscore.lang.systemtests.flows;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openscore.lang.compiler.SlangSource;
import org.openscore.lang.entities.CompilationArtifact;
import org.openscore.lang.entities.ScoreLangConstants;
import org.openscore.lang.systemtests.StepData;
import org.openscore.lang.systemtests.SystemsTestsParent;

import java.io.Serializable;
import java.net.URI;
import java.util.*;


/**
 * Date: 12/11/2014
 *
 * @author lesant
 */
public class DataFlowTest extends SystemsTestsParent {

    @Test
    public void testDataFlow() throws Exception {
        URI resource = getClass().getResource("/yaml/system-flows/data_flow.yaml").toURI();
        URI operations = getClass().getResource("/yaml/system-flows/data_flow_op.sl").toURI();

        SlangSource dep = SlangSource.fromFile(operations);
        Set<SlangSource> path = Sets.newHashSet(dep);
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);


        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("myMessage", "hello world");
        userInputs.put("tryToChangeMessage", "changed");

        Map<String, StepData> steps = triggerWithData(compilationArtifact, userInputs, null);

        Assert.assertEquals(ScoreLangConstants.SUCCESS_RESULT, steps.get(EXEC_START_PATH).getResult());
        Assert.assertEquals(ScoreLangConstants.SUCCESS_RESULT, steps.get(FIRST_STEP_PATH).getResult());
    }

    @Test
    public void testBindingsFlow() throws Exception {
        URI resource = getClass().getResource("/yaml/system-flows/bindings_flow.yaml").toURI();
        URI operations = getClass().getResource("/yaml/system-flows/binding_flow_op.sl").toURI();

        SlangSource dep = SlangSource.fromFile(operations);
        Set<SlangSource> path = Sets.newHashSet(dep);
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("base_input", ">");

        Map<String, StepData> steps = triggerWithData(compilationArtifact, userInputs, null);

        Map<String, Serializable> flowOutputs = steps.get(EXEC_START_PATH).getOutputs();
        String final_output = (String) flowOutputs.get("final_output");
        Assert.assertEquals("some of the inputs or outputs were not bound correctly",
                13, final_output.length());
        Assert.assertEquals("some of the inputs were not bound correctly",
                6, StringUtils.countMatches(final_output, ">"));
        Assert.assertEquals("some of the outputs were not bound correctly",
                5, StringUtils.countMatches(final_output, "<"));
        Assert.assertEquals("some of the results were not bound correctly",
                2, StringUtils.countMatches(final_output, "|"));
    }

    @Test
    public void testBindingsFlowIntegers() throws Exception {
        URI resource = getClass().getResource("/yaml/system-flows/bindings_flow_int.sl").toURI();
        URI operations = getClass().getResource("/yaml/system-flows/binding_flow_int_op.sl").toURI();

        SlangSource dep = SlangSource.fromFile(operations);
        Set<SlangSource> path = Sets.newHashSet(dep);
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("base_input", 1);

        Map<String, StepData> steps = triggerWithData(compilationArtifact, userInputs, null);

        Map<String, Serializable> flowOutputs = steps.get(EXEC_START_PATH).getOutputs();
        int final_output = (int) flowOutputs.get("final_output");
        Assert.assertEquals("some of the inputs or outputs were not bound correctly",
                13, final_output);
    }
}
