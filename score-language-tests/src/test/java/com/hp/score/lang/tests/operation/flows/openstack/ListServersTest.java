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
package com.hp.score.lang.tests.operation.flows.openstack;

import com.google.common.collect.Sets;
import com.hp.score.lang.entities.CompilationArtifact;
import com.hp.score.lang.tests.operation.SystemsTestsParent;

import org.eclipse.score.events.EventConstants;
import org.eclipse.score.events.ScoreEvent;
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
 * Date: 12/5/2014
 *
 * @author Bonczidai Levente
 */
public class ListServersTest  extends SystemsTestsParent{

    @Test
    @Ignore
    public void testCompileAndRunFlow() throws Exception {
        URI resource = getClass().getResource("/yaml/openstack/flow_list_servers.yaml").toURI();
        URI operations = getClass().getResource("/yaml/openstack/").toURI();

        Set<File> path = Sets.newHashSet(new File(operations));
        CompilationArtifact compilationArtifact = slang.compile(new File(resource), path);

        //TODO: remove default values for inputs
        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("openstackHost", "16.77.58.96");
        userInputs.put("openstackIdentityPort", "5000");
        userInputs.put("openstackComputePort", "8774");
        userInputs.put("openstackUsername", "demo");
        userInputs.put("openstackPassword", "B33f34t3r");
        ScoreEvent event = trigger(compilationArtifact, userInputs);
        Assert.assertEquals(EventConstants.SCORE_FINISHED_EVENT, event.getEventType());
    }

}
