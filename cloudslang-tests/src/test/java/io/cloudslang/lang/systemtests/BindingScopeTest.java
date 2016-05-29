/**
 * ****************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 * <p/>
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * *****************************************************************************
 */
package io.cloudslang.lang.systemtests;

import com.google.common.collect.Sets;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.utils.ExpressionUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Bonczidai Levente
 * @since 3/18/2016
 */
public class BindingScopeTest extends SystemsTestsParent {

    @Test
    public void testStepPublishValues() throws Exception {
        URL resource = getClass().getResource("/yaml/binding_scope_flow.sl");
        URI operation = getClass().getResource("/yaml/binding_scope_op.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource.toURI()), path);

        Map<String, Value> userInputs = Collections.emptyMap();
        Set<SystemProperty> systemProperties = Collections.emptySet();

        // trigger ExecutionPlan
        RuntimeInformation runtimeInformation = triggerWithData(compilationArtifact, userInputs, systemProperties);

        Map<String, StepData> executionData = runtimeInformation.getSteps();

        StepData stepData = executionData.get(FIRST_STEP_PATH);
        Assert.assertNotNull("step data is null", stepData);
    }

    @Test
    public void testFlowContextInStepPublishSection() throws Exception {
        URL resource = getClass().getResource("/yaml/binding_scope_flow_context_in_step_publish.sl");
        URI operation = getClass().getResource("/yaml/binding_scope_op.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation));

        // pre-validation - step expression uses flow var name
        SlangSource flowSource = SlangSource.fromFile(resource.toURI());
        Executable flowExecutable = slangCompiler.preCompile(flowSource);
        String flowVarName = "flow_var";
        Assert.assertEquals(
                "Input name should be: " + flowVarName,
                flowVarName,
                flowExecutable.getInputs().get(0).getName()
        );
        @SuppressWarnings("unchecked")
        List<Output> stepPublishValues = (List<Output>) ((Flow) flowExecutable)
                .getWorkflow()
                .getSteps()
                .getFirst()
                .getPostStepActionData()
                .get(SlangTextualKeys.PUBLISH_KEY);
        Assert.assertEquals(
                "Step expression should contain: " + flowVarName,
                flowVarName,
                StringUtils.trim(ExpressionUtils.extractExpression(stepPublishValues.get(0).getValue().get()))
        );

        CompilationArtifact compilationArtifact = slang.compile(flowSource, path);

        Map<String, Value> userInputs = Collections.emptyMap();
        Set<SystemProperty> systemProperties = Collections.emptySet();

        exception.expect(RuntimeException.class);
        exception.expectMessage("flow_var");
        exception.expectMessage("not defined");

        // trigger ExecutionPlan
        triggerWithData(compilationArtifact, userInputs, systemProperties);
    }

}
