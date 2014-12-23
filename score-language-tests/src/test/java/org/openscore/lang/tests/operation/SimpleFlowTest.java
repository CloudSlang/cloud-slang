/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.lang.tests.operation;

import com.google.common.collect.Sets;
import org.openscore.lang.entities.CompilationArtifact;
import org.apache.commons.lang3.tuple.Pair;
import org.openscore.events.EventConstants;
import org.openscore.events.ScoreEvent;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Date: 11/14/2014
 * d
 *
 * @author Bonczidai Levente
 */
public class SimpleFlowTest extends SystemsTestsParent {

    private static final long DEFAULT_TIMEOUT = 20000;

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testSimpleFlowBasic() throws Exception {
		compileAndRunSimpleFlow(Pair.of("input1", "-2"),Pair.of("time_zone_as_string", "+2"));
    }

	@Test(timeout = DEFAULT_TIMEOUT)
	public void testSimpleFlowNavigation() throws Exception {
		compileAndRunSimpleFlow(Pair.of("input1", -999));
	}

    @Test
    public void testFlowWithGlobalSession() throws Exception {
        URI resource = getClass().getResource("/yaml/flow_using_global_session.yaml").toURI();
        URI operations = getClass().getResource("/yaml/simple_operations.yaml").toURI();

        Set<File> path = Sets.newHashSet(new File(operations));
        CompilationArtifact compilationArtifact = slang.compile(new File(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("object_value", "SessionValue");
        ScoreEvent event = trigger(compilationArtifact, userInputs);
        Assert.assertEquals(EventConstants.SCORE_FINISHED_EVENT, event.getEventType());
    }

	@SafeVarargs
	private final void compileAndRunSimpleFlow(Map.Entry<String, ? extends Serializable>... inputs) throws Exception {
		URI flow = getClass().getResource("/yaml/simple_flow.yaml").toURI();
		URI operations = getClass().getResource("/yaml/simple_operations.yaml").toURI();
		Set<File> path = Sets.newHashSet(new File(operations));
		CompilationArtifact compilationArtifact = slang.compile(new File(flow), path);
		HashMap<String, Serializable> userInputs = new HashMap<>();
        for (Entry<String, ? extends Serializable> input : inputs) {
            userInputs.put(input.getKey(), input.getValue());
        }
		ScoreEvent event = trigger(compilationArtifact, userInputs);
		Assert.assertEquals(EventConstants.SCORE_FINISHED_EVENT, event.getEventType());
	}

}
