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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.compiler.modeller.model.Step;
import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.lang.entities.bindings.ScriptFunction;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import java.net.URI;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
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
 * @since 3/14/2017
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CompileStepsWithModifiers {

    private static final Set<ScriptFunction> EMPTY_SCRIPT_FUNCTIONS = Collections.emptySet();
    private static final Set<String> EMPTY_SYSTEM_PROPERTY_DEPENDENCIES = Collections.emptySet();
    private static final Set<ScriptFunction> SYSTEM_PROPERTY = Sets.newHashSet(ScriptFunction.GET_SYSTEM_PROPERTY);
    private static final Set<ScriptFunction> SYSTEM_PROPERTY_GET = Sets.newHashSet(
            ScriptFunction.GET_SYSTEM_PROPERTY,
            ScriptFunction.GET
    );
    private static final Set<String> SP_NAMES_01 = Sets.newHashSet("a.b.c.sp0");

    @Autowired
    private SlangCompiler compiler;

    @Test
    public void testPreCompile() throws Exception {
        URI executableUri = getClass().getResource("/steps/flow_steps_modifiers_01.sl").toURI();
        Executable flow = compiler.preCompile(SlangSource.fromFile(executableUri));

        Deque<Step> steps = ((Flow) flow).getWorkflow().getSteps();
        assertEquals(2, steps.size());

        Step firstStep = steps.removeFirst();
        List<Argument> expectedFirstStepInputs = createExpectedInputsFirstStep();
        assertEquals(expectedFirstStepInputs, firstStep.getArguments());

        Step secondStep = steps.removeFirst();
        List<Argument> expectedSecondStepInputs = createExpectedInputsSecondStep();
        assertEquals(expectedSecondStepInputs, secondStep.getArguments());
    }

    private List<Argument> createExpectedInputsFirstStep() {
        return Lists.newArrayList(
                new Argument("input_01", ValueFactory.create("input_01_value")),
                new Argument("input_02", ValueFactory.create("input_02_value")),
                new Argument(
                        "input_03",
                        ValueFactory.create(null),
                        false,
                        EMPTY_SCRIPT_FUNCTIONS,
                        EMPTY_SYSTEM_PROPERTY_DEPENDENCIES
                ),
                new Argument("input_04"),
                new Argument(
                        "input_05",
                        ValueFactory.create("input_05_value", false),
                        true,
                        EMPTY_SCRIPT_FUNCTIONS,
                        EMPTY_SYSTEM_PROPERTY_DEPENDENCIES
                ),
                new Argument(
                        "input_06",
                        ValueFactory.create(null, true),
                        false,
                        EMPTY_SCRIPT_FUNCTIONS,
                        EMPTY_SYSTEM_PROPERTY_DEPENDENCIES
                ),
                new Argument(
                        "input_07",
                        ValueFactory.create("input_07_value", true),
                        true,
                        EMPTY_SCRIPT_FUNCTIONS,
                        EMPTY_SYSTEM_PROPERTY_DEPENDENCIES
                ),
                new Argument(
                        "input_08",
                        ValueFactory.create("${get_sp('a.b.c.sp0')}", false),
                        true,
                        SYSTEM_PROPERTY, SP_NAMES_01
                ),
                new Argument(
                        "input_09",
                        ValueFactory.create("${get(get_sp('a.b.c.sp0'), 'default_value')}", true),
                        true,
                        SYSTEM_PROPERTY_GET,
                        SP_NAMES_01
                )
        );
    }

    private List<Argument> createExpectedInputsSecondStep() {
        return Lists.newArrayList(
                new Argument("input_01", ValueFactory.create("input_01_value"))
        );
    }

}
