/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.systemtests.decisions;

import com.google.common.collect.Sets;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.systemtests.StepData;
import io.cloudslang.lang.systemtests.SystemsTestsParent;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Bonczidai Levente
 * @since 07/07/2016
 */
public class DecisionsTest extends SystemsTestsParent {

    private Set<String> emptySet = Collections.emptySet();
    private Set<SystemProperty> emptyProperties = Collections.emptySet();

    @Test
    public void testValues1() throws Exception {
        CompilationArtifact compilationArtifact = getCompilationArtifact("/yaml/decisions/decision_1.sl");
        Map<String, StepData> executionData =
                triggerWithData(compilationArtifact, getUserInputs(), emptyProperties).getSteps();
        StepData decisionData = executionData.get(EXEC_START_PATH);

        verifyInputs1(decisionData);
        verifyOutputs1(decisionData);
        verifyResult1(decisionData);
    }

    @Test
    public void testValues2() throws Exception {
        CompilationArtifact compilationArtifact = getCompilationArtifact("/yaml/decisions/decision_2.sl");
        Map<String, StepData> executionData =
                triggerWithData(compilationArtifact, getUserInputs(), emptyProperties).getSteps();
        StepData decisionData = executionData.get(EXEC_START_PATH);

        verifyInputs2(decisionData);
        verifyOutputs2(decisionData);
        verifyResult1(decisionData);
    }

    @Test
    public void testValuesSystemProperties() throws Exception {
        CompilationArtifact compilationArtifact = getCompilationArtifact("/yaml/decisions/decision_3_sp.sl");
        Map<String, StepData> executionData =
                triggerWithData(compilationArtifact, getUserInputs(), getSystemProperties()).getSteps();
        StepData decisionData = executionData.get(EXEC_START_PATH);

        verifyInputs1(decisionData);
        verifyOutputs1(decisionData);
        verifyResult1(decisionData);
    }

    @Test
    public void testDecisionInFlow() throws Exception {
        CompilationArtifact compilationArtifact =
                getCompilationArtifactWithDependencies(
                        "/yaml/decisions/flow_with_decision_1.sl",
                        Sets.newHashSet(
                                "/yaml/decisions/decision_1.sl",
                                "/yaml/noop.sl"
                        )
                );
        Map<String, StepData> executionData =
                triggerWithData(compilationArtifact, getUserInputs(), emptyProperties).getSteps();
        StepData decisionData = executionData.get(EXEC_START_PATH);

        verifyOutputs1(decisionData);
        verifyResult1(decisionData);
    }

    private CompilationArtifact getCompilationArtifact(String path) throws URISyntaxException {
        return getCompilationArtifactWithDependencies(path, emptySet);
    }

    private CompilationArtifact getCompilationArtifactWithDependencies(String flowPath,
                                                                       Set<String> dependencyPaths)
            throws URISyntaxException {
        URI flow = getClass().getResource(flowPath).toURI();
        Set<SlangSource> dependencies = new HashSet<>();
        for (String dependencyPath : dependencyPaths) {
            dependencies.add(SlangSource.fromFile(getClass().getResource(dependencyPath).toURI()));
        }
        return slang.compile(SlangSource.fromFile(flow), dependencies);
    }

    private Map<String, Value> getUserInputs() {
        Map<String, Value> userInputs = new HashMap<>();
        userInputs.put("x", ValueFactory.create("2"));
        userInputs.put("y", ValueFactory.create("3"));
        return userInputs;
    }

    private void verifyInputs1(StepData data) {
        Map<String, Serializable> expectedInputs = new LinkedHashMap<>();
        expectedInputs.put("x", "2");
        expectedInputs.put("y", "3");
        Assert.assertEquals("decision input values not as expected", expectedInputs, data.getInputs());
    }

    private void verifyInputs2(StepData data) {
        Map<String, Serializable> expectedInputs = new LinkedHashMap<>();
        expectedInputs.put("x", "2");
        expectedInputs.put("y", "3");
        expectedInputs.put("z", "default_value");
        Assert.assertEquals("decision input values not as expected", expectedInputs, data.getInputs());
    }

    private void verifyOutputs1(StepData data) {
        Map<String, Serializable> expectedOutputs = new LinkedHashMap<>();
        expectedOutputs.put("sum", "5");
        Assert.assertEquals("decision output values not as expected", expectedOutputs, data.getOutputs());
    }

    private void verifyOutputs2(StepData data) {
        Assert.assertEquals("decision output values not as expected", Collections.emptyMap(), data.getOutputs());
    }


    private void verifyResult1(StepData data) {
        Assert.assertEquals("decision result not as expected", "LESS_THAN", data.getResult());
    }

    private Set<SystemProperty> getSystemProperties() {
        return Sets.newHashSet(
                new SystemProperty("user.sys", "prop1", "2"),
                new SystemProperty("user.sys", "prop2", "3"),
                new SystemProperty("user.sys", "prop3", "5"),
                new SystemProperty("user.sys", "prop4", "6")
        );
    }

}
