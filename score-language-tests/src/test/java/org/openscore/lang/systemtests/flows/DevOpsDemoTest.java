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
import org.junit.Ignore;
import org.junit.Test;
import org.openscore.events.EventConstants;
import org.openscore.events.ScoreEvent;
import org.openscore.lang.compiler.SlangSource;
import org.openscore.lang.entities.CompilationArtifact;
import org.openscore.lang.systemtests.SystemsTestsParent;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DevOpsDemoTest extends SystemsTestsParent {

    @Test
    @Ignore
    public void testCompileAndRunFlow() throws Exception {
        URI resource = getClass().getResource("/yaml/docker-demo/demo_dev_ops_flow.yaml").toURI();
        URI operations = getClass().getResource("/yaml/docker-demo/").toURI();

        SlangSource operationsSource = SlangSource.fromFile(operations);
        Set<SlangSource> path = Sets.newHashSet(operationsSource);
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("dockerHost", "{{ dockerHost }}");
        userInputs.put("dockerUsername", "{{ dockerUsername }}");
        userInputs.put("dockerPassword", "{{ dockerPassword }}");
        userInputs.put("emailHost", "{{ emailHost }}");
        userInputs.put("emailPort", "{{ emailPort }}");
        userInputs.put("emailSender", "{{ emailSender }}");
        userInputs.put("emailRecipient", "{{ emailRecipient }}");
        ScoreEvent event = trigger(compilationArtifact, userInputs, null);
        Assert.assertEquals(EventConstants.SCORE_FINISHED_EVENT, event.getEventType());
    }
}
