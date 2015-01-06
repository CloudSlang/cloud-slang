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
        URI operations = getClass().getResource("/yaml/system-flows/data_flow_operations.yaml").toURI();

        SlangSource dep = SlangSource.fromFile(operations);
        Set<SlangSource> path = Sets.newHashSet(dep);
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);


        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("myMessage", "hello world");
        userInputs.put("tryToChangeMessage", "changed");

        Map<String, StepData> tasks = triggerWithData(compilationArtifact, userInputs, null);

        Assert.assertEquals(3, tasks.size());
        Assert.assertEquals(ScoreLangConstants.SUCCESS_RESULT, tasks.get("0/0").getResult());
        Assert.assertEquals(ScoreLangConstants.SUCCESS_RESULT, tasks.get("0/1").getResult());
    }
}
