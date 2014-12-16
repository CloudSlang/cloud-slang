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
import com.hp.score.lang.entities.ScoreLangConstants;
import com.hp.score.lang.tests.operation.SystemsTestsParent;
import org.eclipse.score.events.EventConstants;
import org.eclipse.score.events.ScoreEvent;
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
