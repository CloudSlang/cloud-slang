/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.functions;

import com.google.common.collect.Sets;
import io.cloudslang.lang.compiler.SlangCompiler;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.entities.CompilationArtifact;
import java.net.URI;
import java.net.URL;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

/**
 * @author Bonczidai Levente
 * @since 3/15/2017
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FunctionDependenciesCompileTest {

    @Autowired
    private SlangCompiler slangCompiler;

    @Test
    public void testSystemPropertyDependencies() throws Exception {
        URL resource = getClass().getResource("/functions/system_property_dependencies_flow.sl");
        URI operation = getClass().getResource("/functions/system_property_dependencies_op.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation));
        CompilationArtifact compilationArtifact = slangCompiler.compile(SlangSource.fromFile(resource.toURI()), path);

        assertEquals(
                "system property dependencies not as expected",
                prepareSystemPropertiesForDependencyTest(),
                compilationArtifact.getSystemProperties()
        );
    }

    @Test
    public void testSystemPropertyDependenciesStepInputModifiers() throws Exception {
        URL resource = getClass().getResource("/functions/system_property_dependencies_step_input_modifiers.sl");
        URI operation = getClass().getResource("/functions/system_property_dependencies_op.sl").toURI();
        Set<SlangSource> path = Sets.newHashSet(SlangSource.fromFile(operation));
        CompilationArtifact compilationArtifact = slangCompiler.compile(SlangSource.fromFile(resource.toURI()), path);

        assertEquals(
                "system property dependencies not as expected",
                prepareSystemPropertiesForDependencyTest(),
                compilationArtifact.getSystemProperties()
        );
    }

    private Set<String> prepareSystemPropertiesForDependencyTest() {
        return Sets.newHashSet(
                "flow.input.prop1",
                "flow.input.prop2",
                "flow.input.prop3",
                "flow.input.prop4",
                "flow.input.prop5",
                "flow.output.prop1",
                "step.input.prop1",
                "step.input.prop2",
                "step.input.prop3",
                "step.input.prop4",
                "step.publish.prop1",
                "step.publish.prop2",
                "step.publish.prop3",
                "step.publish.prop4",
                "op.input.prop1",
                "op.input.prop2",
                "op.input.prop3",
                "op.input.prop4",
                "op.input.prop5",
                "op.output.prop1",
                "op.result.prop1",
                "parallel_loop.publish.prop1",
                "parallel_loop.publish.prop2",
                "for.input.prop1",
                "for.input.prop2",
                "for.publish.prop1",
                "for.publish.prop2"
        );
    }

}
