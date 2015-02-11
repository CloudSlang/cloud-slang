/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.openscore.lang.systemtests;

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.openscore.lang.compiler.SlangSource;
import org.openscore.lang.entities.CompilationArtifact;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LoopFlowsTest extends SystemsTestsParent{

    @Test
    public void testFlowWithLoops() throws Exception {
        URI resource = getClass().getResource("/yaml/loops/simple_loop.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/loops/print.sl").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation1));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        Map<String, StepData> stepsData = triggerWithData(compilationArtifact, userInputs, null);
        StepData firstTask = stepsData.get(FIRST_STEP_PATH);
        StepData secondTask = stepsData.get(SECOND_STEP_KEY);
        StepData thirdTask = stepsData.get(THIRD_STEP_KEY);
        Assert.assertTrue(firstTask.getInputs().containsValue(1));
        Assert.assertTrue(secondTask.getInputs().containsValue(2));
        Assert.assertTrue(thirdTask.getInputs().containsValue(3));
    }

}
