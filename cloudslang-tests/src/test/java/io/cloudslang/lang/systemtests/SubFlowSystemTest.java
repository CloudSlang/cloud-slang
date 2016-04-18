/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.systemtests;

import com.google.common.collect.Sets;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.score.events.ScoreEvent;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
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
        URI operation1 = getClass().getResource("/yaml/test_op.sl").toURI();
        URI operation2 = getClass().getResource("/yaml/check_weather.sl").toURI();
        URI operation3 = getClass().getResource("/yaml/get_time_zone.sl").toURI();
        URI operation4 = getClass().getResource("/yaml/check_number.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(subFlow),
                SlangSource.fromFile(operation1),
                SlangSource.fromFile(operation2),
                SlangSource.fromFile(operation3),
                SlangSource.fromFile(operation4));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource), path);
        Assert.assertEquals("the system properties size is not as expected", 2, compilationArtifact.getSystemProperties().size());
		Set<SystemProperty> systemProperties = new HashSet<>();
        systemProperties.add(new SystemProperty("user.sys", "props.port", "22"));
        systemProperties.add(new SystemProperty("user.sys", "props.alla", "balla"));
        Map<String, Serializable> userInputs = new HashMap<>();
        userInputs.put("input1", "value1");
        ScoreEvent event = trigger(compilationArtifact, userInputs, systemProperties);
        Assert.assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
    }

    @Test
    /*
    * Parent flow 'parent_flow_missing_inputs' does not provide a required & overidable with no default value input parameter 'city'
    * for subflow/operation 'check_weather'
    * */

    public void testSubFlowMissingRequiredInputs() throws Exception {
        URI resource = getClass().getResource("/yaml/sub-flow/parent_flow_missing_inputs.yaml").toURI();
        URI subFlow = getClass().getResource("/yaml/sub-flow/child_flow.yaml").toURI();
        URI operation1 = getClass().getResource("/yaml/test_op.sl").toURI();
        URI operation2 = getClass().getResource("/yaml/check_weather.sl").toURI();
        URI operation3 = getClass().getResource("/yaml/get_time_zone.sl").toURI();
        URI operation4 = getClass().getResource("/yaml/check_number.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(subFlow),
                SlangSource.fromFile(operation1),
                SlangSource.fromFile(operation2),
                SlangSource.fromFile(operation3),
                SlangSource.fromFile(operation4));
        try {
            slang.compile(SlangSource.fromFile(resource), path);
            Assert.fail();
        }catch (RuntimeException e){
            Assert.assertNotNull(e.getCause());
            Assert.assertTrue("got wrong error type: expected [" + IllegalArgumentException.class + "] got [" + e.getCause().getClass() + "]", e.getCause() instanceof IllegalArgumentException);
            String errorMessage = e.getCause().getMessage();
            Assert.assertNotNull(errorMessage);
            Assert.assertTrue("Did not get error from expected parent flow [parent_flow_missing_inputs]", errorMessage.contains("parent_flow_missing_inputs"));
            Assert.assertTrue("Did not get error from expected step [step1]", errorMessage.contains("step1"));
            Assert.assertTrue("Did not get error from expected missing input [city]", errorMessage.contains("city"));
            Assert.assertTrue("Did not get error from expected subflow [user.ops.check_weather]", errorMessage.contains("user.ops.check_weather"));
        }

    }


}
