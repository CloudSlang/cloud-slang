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

/**
* Date: 11/17/2014
*
* @author Bonczidai Levente
*/
public class CreateDbContainerTest  extends SystemsTestsParent {

    @Test
    @Ignore
    public void testCreateDbContainer() throws Exception {
        URI resource = getClass().getResource("/yaml/docker-demo/create_db_container.yaml").toURI();
        URI operations = getClass().getResource("/yaml/docker-demo/").toURI();

        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operations));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("host", "{{ host }}");
        userInputs.put("username", "{{ username }}");
        userInputs.put("password", "{{ password }}");
        ScoreEvent event = trigger(compilationArtifact, userInputs, null);
        Assert.assertEquals(EventConstants.SCORE_FINISHED_EVENT, event.getEventType());
    }

}
