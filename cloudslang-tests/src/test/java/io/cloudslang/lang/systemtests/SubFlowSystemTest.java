/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.systemtests;

import com.google.common.collect.Sets;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.score.events.ScoreEvent;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import static com.google.common.collect.Sets.newHashSet;
import static io.cloudslang.lang.compiler.SlangSource.fromFile;
import static org.junit.Assert.assertTrue;

/*
 * Created by orius123 on 12/11/14.
 */
public class SubFlowSystemTest extends SystemsTestsParent {

    @Test
    public void testCompileAndRunSubFlowBasic() throws Exception {
        URI resource = getClass().getResource("/yaml/sub-flow/parent_flow.sl").toURI();
        URI subFlow = getClass().getResource("/yaml/sub-flow/child_flow.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/test_op.sl").toURI();
        URI operation2 = getClass().getResource("/yaml/check_weather.sl").toURI();
        URI operation3 = getClass().getResource("/yaml/get_time_zone.sl").toURI();
        URI operation4 = getClass().getResource("/yaml/check_number.sl").toURI();
        Set<SlangSource> path = newHashSet(fromFile(subFlow),
                fromFile(operation1),
                fromFile(operation2),
                fromFile(operation3),
                fromFile(operation4));
        CompilationArtifact compilationArtifact = slang.compile(fromFile(resource), path);
        Assert.assertEquals("the system properties size is not as expected",
                2, compilationArtifact.getSystemProperties().size());
        Set<SystemProperty> systemProperties = new HashSet<>();
        systemProperties.add(new SystemProperty("user.sys", "props.port", "22"));
        systemProperties.add(new SystemProperty("user.sys", "props.alla", "balla"));
        Map<String, Value> userInputs = new HashMap<>();
        userInputs.put("input1", ValueFactory.create("value1"));
        ScoreEvent event = trigger(compilationArtifact, userInputs, systemProperties);
        Assert.assertEquals(ScoreLangConstants.EVENT_EXECUTION_FINISHED, event.getEventType());
    }

    @Test
    public void testSubFlowMissingRequiredInputs() throws Exception {
        URI resource = getClass().getResource("/yaml/sub-flow/parent_flow_missing_inputs.sl").toURI();
        URI subFlow = getClass().getResource("/yaml/sub-flow/child_flow.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/test_op.sl").toURI();
        URI operation2 = getClass().getResource("/yaml/check_weather.sl").toURI();
        URI operation3 = getClass().getResource("/yaml/get_time_zone.sl").toURI();
        URI operation4 = getClass().getResource("/yaml/check_number.sl").toURI();
        Set<SlangSource> path = newHashSet(fromFile(subFlow),
                fromFile(operation1),
                fromFile(operation2),
                fromFile(operation3),
                fromFile(operation4));
        try {
            slang.compile(fromFile(resource), path);
            Assert.fail();
        } catch (RuntimeException e) {
            Assert.assertNotNull(e.getCause());
            assertTrue("got wrong error type: expected [" + IllegalArgumentException.class + "] got [" +
                    e.getCause().getClass() + "]", e.getCause() instanceof IllegalArgumentException);
            String errorMessage = e.getCause().getMessage();
            Assert.assertNotNull(errorMessage);
            assertTrue("Did not get error from expected parent flow [user.flows.parent_flow_missing_inputs]",
                    errorMessage.contains("user.flows.parent_flow_missing_inputs"));
            assertTrue("Did not get error from expected step [step1]", errorMessage.contains("step1"));
            assertTrue("Did not get error from expected missing input [city]", errorMessage.contains("city"));
            assertTrue("Did not get error from expected subflow [user.ops.check_weather]",
                    errorMessage.contains("user.ops.check_weather"));
        }

    }

    @Test
    public void testSubFlowMissingRequiredInputsRecursive() throws Exception {
        URI resource = getClass().getResource("/yaml/sub-flow/parent_flow_for_child_flow_missing_inputs.sl").toURI();
        URI subFlow = getClass().getResource("/yaml/sub-flow/child_flow_missing_inputs.sl").toURI();
        URI operation1 = getClass().getResource("/yaml/test_op.sl").toURI();
        URI operation2 = getClass().getResource("/yaml/check_weather.sl").toURI();
        URI operation3 = getClass().getResource("/yaml/get_time_zone.sl").toURI();
        URI operation4 = getClass().getResource("/yaml/check_number.sl").toURI();
        Set<SlangSource> path = newHashSet(fromFile(subFlow),
                fromFile(operation1),
                fromFile(operation2),
                fromFile(operation3),
                fromFile(operation4));
        try {
            slang.compile(fromFile(resource), path);
            Assert.fail();
        } catch (RuntimeException e) {
            Assert.assertNotNull(e.getCause());
            assertTrue("got wrong error type: expected [" + IllegalArgumentException.class + "] got [" +
                    e.getCause().getClass() + "]", e.getCause() instanceof IllegalArgumentException);
            String errorMessage = e.getCause().getMessage();
            Assert.assertNotNull(errorMessage);
            assertTrue(
                    "Did not get error from expected parent flow [user.flows.child_flow_missing_inputs]",
                    errorMessage.contains("user.flows.child_flow_missing_inputs")
            );
            assertTrue("Did not get error from expected step [step01]", errorMessage.contains("step01"));
            assertTrue("Did not get error from expected missing input [time_zone_as_string]",
                    errorMessage.contains("time_zone_as_string"));
            assertTrue(
                    "Did not get error from expected subflow [user.ops.get_time_zone]",
                    errorMessage.contains("user.ops.get_time_zone")
            );
        }
    }

}
