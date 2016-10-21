/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.systemtests.systemproperties;

import com.google.common.collect.Sets;
import io.cloudslang.lang.api.Slang;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ExecutableType;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.systemtests.RuntimeInformation;
import io.cloudslang.lang.systemtests.StepData;
import io.cloudslang.lang.systemtests.ValueSyntaxParent;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Bonczidai Levente
 * @since 11/6/2015
 */
public class SensitiveSystemPropertiesTest extends ValueSyntaxParent {

    private static final Set<SlangSource> EMPTY_SET = Collections.emptySet();

    @Autowired
    private Slang slang;

    @Test
    public void testSystemPropertyDependencies() throws Exception {
        URL resource = getClass().getResource("/yaml/functions/sensitive_system_properties_flow.sl");
        URI operation = getClass().getResource("/yaml/functions/sensitive_system_properties_op.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation));
        CompilationArtifact compilationArtifact = slang.compile(SlangSource.fromFile(resource.toURI()), path);

        Assert.assertEquals(
                "system property dependencies not as expected",
                prepareSystemPropertiesForDependencyTest(),
                compilationArtifact.getSystemProperties()
        );
    }

    @Test
    public void testValidSystemPropertiesFlow() throws Exception {
        URL executable = getClass().getResource("/yaml/functions/sensitive_system_properties_flow.sl");
        URI operation = getClass().getResource("/yaml/functions/sensitive_system_properties_op.sl").toURI();
        URI propertiesFileUri = getClass().getResource("/yaml/properties/a/b/sensitive.prop.sl").toURI();
        Set<SlangSource> dependencies = Sets.newHashSet(SlangSource.fromFile(operation));
        testExecutable(
                SlangSource.fromFile(executable.toURI()),
                dependencies,
                SlangSource.fromFile(propertiesFileUri),
                ExecutableType.FLOW
        );
    }

    @Test
    public void testValidSystemPropertiesOp() throws Exception {
        URL executable = getClass().getResource("/yaml/functions/sensitive_system_properties_op.sl");
        URI propertiesFileUri = getClass().getResource("/yaml/properties/a/b/sensitive.prop.sl").toURI();
        testExecutable(
                SlangSource.fromFile(executable.toURI()),
                EMPTY_SET,
                SlangSource.fromFile(propertiesFileUri),
                ExecutableType.OPERATION
        );
    }

    @Test
    public void testInvalidKey() throws Exception {
        URI propertiesFileUri = getClass().getResource("/yaml/properties/a/b/sensitive_invalid_key.prop.sl").toURI();

        exception.expect(RuntimeException.class);
        exception.expectMessage(
                "Artifact {flow.var3_sensitive} has unrecognized tag {invalid_key}." +
                        " Please take a look at the supported features per versions link"
        );

        slang.loadSystemProperties(SlangSource.fromFile(propertiesFileUri));
    }

    @Test
    public void testKeyNotString() throws Exception {
        URI propertiesFileUri = getClass().getResource("/yaml/properties/a/b/sensitive_key_not_string.prop.sl").toURI();

        exception.expect(RuntimeException.class);
        exception.expectMessage("Artifact {flow.var3_sensitive} has invalid tag {123}: Value cannot be cast to String");

        slang.loadSystemProperties(SlangSource.fromFile(propertiesFileUri));
    }

    @Test
    public void testKeyNotStringd() throws Exception {
        final URI propertiesFileUri = getClass()
                .getResource("/yaml/properties/a/b/sensitive_value_not_serializable.prop.sl").toURI();

        exception.expect(RuntimeException.class);
        exception.expectMessage("Artifact {flow.var3_sensitive} has invalid value {java.lang.Object");
        exception.expectMessage("}: Value cannot be cast to Serializable");

        slang.loadSystemProperties(SlangSource.fromFile(propertiesFileUri));
    }

    private void testExecutable(
            SlangSource executable,
            Set<SlangSource> dependencies,
            SlangSource propertiesFile,
            ExecutableType executableType) throws URISyntaxException {
        CompilationArtifact compilationArtifact = slang.compile(executable, dependencies);

        Map<String, Value> userInputs = new HashMap<>();
        Set<SystemProperty> systemProperties = slang.loadSystemProperties(propertiesFile);

        // trigger ExecutionPlan
        RuntimeInformation runtimeInformation = triggerWithData(compilationArtifact, userInputs, systemProperties);

        Map<String, StepData> executionData = runtimeInformation.getSteps();

        StepData flowData = executionData.get(EXEC_START_PATH);
        Assert.assertNotNull("flow data is null", flowData);

        verifyInOutParams(flowData.getInputs());
        verifyInOutParams(flowData.getOutputs());

        if (ExecutableType.FLOW.equals(executableType)) {
            StepData stepData = executionData.get(FIRST_STEP_PATH);
            Assert.assertNotNull("step data is null", stepData);
            verifyInOutParams(stepData.getInputs());
            verifyInOutParams(stepData.getOutputs());
        }

        Assert.assertEquals("SUCCESS", flowData.getResult());
    }

    private Set<String> prepareSystemPropertiesForDependencyTest() {
        return Sets.newHashSet(
                "a.b.op.var1",
                "a.b.op.var2",
                "a.b.op.var3_sensitive",
                "a.b.flow.var1",
                "a.b.flow.var2",
                "a.b.flow.var3_sensitive"
        );
    }

}
