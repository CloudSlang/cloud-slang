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
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.openscore.events.ScoreEvent;
import org.openscore.lang.compiler.SlangSource;
import org.openscore.lang.entities.CompilationArtifact;
import org.openscore.lang.entities.ScoreLangConstants;

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

        SlangSource operationsSource = SlangSource.fromFile(operations);
        Set<SlangSource> path = Sets.newHashSet(operationsSource);
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("object_value", "SessionValue");
        ScoreEvent event = trigger(compilationArtifact, userInputs, null);
        Assert.assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
    }

	@SafeVarargs
	private final void compileAndRunSimpleFlow(Map.Entry<String, ? extends Serializable>... inputs) throws Exception {
		URI flow = getClass().getResource("/yaml/simple_flow.yaml").toURI();
		URI operations = getClass().getResource("/yaml/simple_operations.yaml").toURI();
        SlangSource operationsSource = SlangSource.fromFile(operations);
		URI vars = getClass().getResource("/yaml/simple_variables.yaml").toURI();
		SlangSource varsSource = SlangSource.fromFile(vars);
        Set<SlangSource> path = Sets.newHashSet(operationsSource);
		CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(flow), path);
		HashMap<String, Serializable> userInputs = new HashMap<>();
        for (Entry<String, ? extends Serializable> input : inputs) {
            userInputs.put(input.getKey(), input.getValue());
        }
		ScoreEvent event = trigger(compilationArtifact, userInputs, slang.loadVariables(varsSource));
		Assert.assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
	}

}
