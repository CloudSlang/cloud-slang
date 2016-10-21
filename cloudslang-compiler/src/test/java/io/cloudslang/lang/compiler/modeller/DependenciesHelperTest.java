/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller;

import com.google.common.collect.Sets;
import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.compiler.modeller.model.Operation;
import io.cloudslang.lang.compiler.modeller.model.Step;
import io.cloudslang.lang.compiler.modeller.model.Workflow;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Bonczidai Levente
 * @since 10/12/2016
 */
public class DependenciesHelperTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private DependenciesHelper dependenciesHelper;

    @Before
    public void setUp() throws Exception {
        dependenciesHelper = new DependenciesHelper();
    }

    @Test
    public void testFetchDependenciesNullExecutable() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage("The validated object is null");
        dependenciesHelper.fetchDependencies(null, Collections.<String, Executable>emptyMap());
    }

    @Test
    public void testFetchDependenciesNullDependencies() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage("The validated object is null");
        dependenciesHelper.fetchDependencies(null, Collections.<String, Executable>emptyMap());
    }

    @Test
    public void testFetchDependenciesOperation() throws Exception {
        Executable executable = mock(Executable.class);
        when(executable.getType()).thenReturn(SlangTextualKeys.OPERATION_TYPE);

        Set<String> result = dependenciesHelper.fetchDependencies(executable,
                Collections.<String, Executable>emptyMap());

        assertEquals(Collections.emptySet(), result);
    }

    @Test
    public void testFetchDependenciesFlowOneStep() throws Exception {
        Flow flow = mock(Flow.class);
        final Workflow workflow = mock(Workflow.class);
        Step step = mock(Step.class);
        Deque<Step> steps = new ArrayDeque<>();
        steps.add(step);
        String stepRefId = "a.b.c.op_01";
        Operation operation = mock(Operation.class);
        Map<String, Executable> availableDependencies = new HashMap<>();
        availableDependencies.put(stepRefId, operation);

        when(flow.getType()).thenReturn(SlangTextualKeys.FLOW_TYPE);
        when(operation.getType()).thenReturn(SlangTextualKeys.OPERATION_TYPE);
        when(flow.getWorkflow()).thenReturn(workflow);
        when(workflow.getSteps()).thenReturn(steps);
        when(step.getRefId()).thenReturn(stepRefId);

        Set<String> result = dependenciesHelper.fetchDependencies(flow, availableDependencies);

        assertEquals(Sets.newHashSet(stepRefId), result);
    }

}