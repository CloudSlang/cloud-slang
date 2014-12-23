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
package org.openscore.lang.tests.operation;

import com.google.common.collect.Sets;
import org.openscore.lang.entities.CompilationArtifact;
import org.openscore.events.EventConstants;
import org.openscore.events.ScoreEvent;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*
 * Created by orius123 on 12/11/14.
 */
public class SubFlowSystemTests extends SystemsTestsParent {

    @Test
    public void testCompileAndRunSubFlowBasic() throws Exception {
        URI resource = getClass().getResource("/yaml/sub-flow/parent_flow.yaml").toURI();
        URI subFlow = getClass().getResource("/yaml/sub-flow/child_flow.yaml").toURI();
        URI operations = getClass().getResource("/yaml/simple_operations.yaml").toURI();

        Set<File> path = Sets.newHashSet(new File(subFlow), new File(operations));
        CompilationArtifact compilationArtifact = slang.compile(new File(resource), path);

        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("input1", "value1");
        ScoreEvent event = trigger(compilationArtifact, userInputs);
        Assert.assertEquals(EventConstants.SCORE_FINISHED_EVENT, event.getEventType());
    }
}
