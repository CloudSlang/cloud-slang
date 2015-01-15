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
import org.openscore.events.ScoreEvent;
import org.openscore.lang.compiler.SlangSource;
import org.openscore.lang.entities.CompilationArtifact;
import org.openscore.lang.entities.ScoreLangConstants;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*
 * Created by orius123 on 12/11/14.
 */
public class SubFlowSystemTest extends SystemsTestsParent {

    @Test
    public void testCompileAndRunSubFlowBasic() throws Exception {
        URI resource = getClass().getResource("/yaml/sub-flow/parent_flow.yaml").toURI();
        URI subFlow = getClass().getResource("/yaml/sub-flow/child_flow.yaml").toURI();
        URI operations = getClass().getResource("/yaml/simple_operations.yaml").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(subFlow), SlangSource.fromFile(operations));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("input1", "value1");
		URI vars = getClass().getResource("/yaml/simple_variables.yaml").toURI();
		SlangSource varsSource = SlangSource.fromFile(vars);
        ScoreEvent event = trigger(compilationArtifact, userInputs, slang.loadVariables(varsSource));
        Assert.assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
    }

}
