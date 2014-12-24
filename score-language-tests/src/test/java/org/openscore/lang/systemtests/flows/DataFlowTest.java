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
import org.openscore.lang.entities.CompilationArtifact;
import org.openscore.lang.entities.ScoreLangConstants;
import org.openscore.lang.systemtests.SystemsTestsParent;
import org.openscore.events.EventConstants;
import org.openscore.events.ScoreEvent;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
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
        startOperationMonitoring();
        URI resource = getClass().getResource("/yaml/system-flows/data_flow.yaml").toURI();
        URI operations = getClass().getResource("/yaml/system-flows/").toURI();

        Set<File> path = Sets.newHashSet(new File(operations));
        CompilationArtifact compilationArtifact = slang.compile(new File(resource), path);


        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("myMessage", "hello world");
        userInputs.put("tryToChangeMessage", "changed");

        ScoreEvent event = trigger(compilationArtifact, userInputs);
        Assert.assertEquals(EventConstants.SCORE_FINISHED_EVENT, event.getEventType());

        List<String> expectedResults = new ArrayList<>();

        expectedResults.add(ScoreLangConstants.SUCCESS_RESULT);
        expectedResults.add(ScoreLangConstants.SUCCESS_RESULT);

        verifyResults(expectedResults);
    }
}
