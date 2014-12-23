/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package com.hp.score.lang.tests.operation.flows;

import com.google.common.collect.Sets;
import com.hp.score.lang.entities.CompilationArtifact;
import com.hp.score.lang.tests.operation.SystemsTestsParent;
import org.openscore.events.EventConstants;
import org.openscore.events.ScoreEvent;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Date: 11/21/2014
 *
 * @author Bonczidai Levente
 */
public class ClearContainersTest  extends SystemsTestsParent {

    @Test
    @Ignore
    public void testCompileAndRunFlow() throws Exception {
        URI resource = getClass().getResource("/yaml/docker-demo/clear_containers_flow.yaml").toURI();
        URI operations = getClass().getResource("/yaml/docker-demo/").toURI();

        Set<File> path = Sets.newHashSet(new File(operations));
        CompilationArtifact compilationArtifact = slang.compile(new File(resource), path);

        //TODO: remove default values for inputs
        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("dbContainerID", "{{ dbContainerID }}");
        userInputs.put("linkedContainerID", "{{ linkedContainerID }}");
        userInputs.put("dockerHost", "{{ dockerHost }}");
        userInputs.put("dockerUsername", "{{ dockerUsername }}");
        userInputs.put("dockerPassword", "{{ dockerPassword }}");
        ScoreEvent event = trigger(compilationArtifact, userInputs);
        Assert.assertEquals(EventConstants.SCORE_FINISHED_EVENT, event.getEventType());
    }
}

