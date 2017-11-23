/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler;

import io.cloudslang.lang.compiler.modeller.model.ExternalStep;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.compiler.modeller.model.Step;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig.class})
public class ExternalSlangCompilerIntegrationTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Autowired
    private SlangCompiler slangCompiler;

    @Test
    public void testFlowWithExternalStep() throws Exception {
        final URL resource = getClass().getResource("/flow_with_external_steps.yaml");
        final SlangSource slangSource = SlangSource.fromFile(new File(resource.toURI()));
        final Flow slangExecutable = (Flow) slangCompiler.preCompile(slangSource);

        assertTrue(slangExecutable.getExternalExecutableDependencies()
                                  .contains("/Library/Utility Operations/Flow Variable Manipulation/Do Nothing"));
        assertEquals(4, slangExecutable.getWorkflow().getSteps().size());
        final List<Class<?>> classes = slangExecutable.getWorkflow().getSteps()
                                                      .stream()
                                                      .map(Object::getClass)
                                                      .collect(Collectors.toList());
        assertThat(classes, Matchers.hasItems(ExternalStep.class, Step.class, Step.class, Step.class));
    }

    @Test
    @Ignore
    public void testFlowWithExternalStepAndErrors() throws Exception {
        final URL resource = getClass().getResource("/flow_with_external_steps_and_errors.sl");
        final SlangSource slangSource = SlangSource.fromFile(new File(resource.toURI()));
        final Flow slangExecutable = (Flow) slangCompiler.preCompile(slangSource);

        assertTrue(slangExecutable.getExternalExecutableDependencies()
                                  .contains("/Library/Utility Operations/Flow Variable Manipulation/Do Nothing"));
        assertEquals(4, slangExecutable.getWorkflow().getSteps().size());
        final List<Class<?>> classes = slangExecutable.getWorkflow().getSteps()
                                                      .stream()
                                                      .map(Object::getClass)
                                                      .collect(Collectors.toList());
        assertThat(classes, Matchers.hasItems(ExternalStep.class, Step.class, Step.class, Step.class));
    }

    @Test
    public void testCompileFlowWithUuidExternalStep() throws Exception {
        final URL resource = getClass().getResource("/external/flow_with_external_step_by_uuid.sl");
        final SlangSource slangSource = SlangSource.fromFile(new File(resource.toURI()));

        expectedException.expect(UnsupportedOperationException.class);
        expectedException.expectMessage("CloudSlang does not support compiling external " +
                "steps. To provide this functionality, you must extend all necessary classes.");
        slangCompiler.compile(slangSource, new HashSet<>());
    }

    @Test
    public void testCompileFlowWithPathExternalStep() throws Exception {
        final URL resource = getClass().getResource("/external/flow_with_external_step_by_path.sl");
        final SlangSource slangSource = SlangSource.fromFile(new File(resource.toURI()));

        expectedException.expect(UnsupportedOperationException.class);
        expectedException.expectMessage("CloudSlang does not support compiling external " +
                "steps. To provide this functionality, you must extend all necessary classes.");
        slangCompiler.compile(slangSource, new HashSet<>());
    }
}
