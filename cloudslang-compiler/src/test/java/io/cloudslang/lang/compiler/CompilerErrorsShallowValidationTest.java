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

import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.compiler.modeller.model.Executable;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Bonczidai Levente
 * @since 9/9/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CompilerErrorsShallowValidationTest {
    @Autowired
    private SlangCompiler compiler;
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testValidateSlangModelWithDependenciesBasic() throws Exception {
        URI flowUri = getClass().getResource("/basic_flow.yaml").toURI();
        Executable flow = compiler.preCompile(SlangSource.fromFile(flowUri));

        URI operationUri = getClass().getResource("/test_op.sl").toURI();
        Executable op = compiler.preCompile(SlangSource.fromFile(operationUri));

        Set<Executable> dependencies = new HashSet<>();
        dependencies.add(op);
        List<RuntimeException> errors = compiler.validateSlangModelWithDirectDependencies(flow, dependencies);

        Assert.assertEquals("", 0, errors.size());
    }

    @Test
    public void testValidFlowWithMissingDependencyRequiredInputInGrandchild() throws Exception {
        URI flowUri = getClass()
                .getResource("/corrupted/flow_missing_dependency_required_input_in_grandchild.sl").toURI();
        Executable flowModel = compiler.preCompile(SlangSource.fromFile(flowUri));

        URI operation2Uri = getClass().getResource("/check_op.sl").toURI();
        Executable operation2Model = compiler.preCompile(SlangSource.fromFile(operation2Uri));
        URI subFlowUri = getClass().getResource("/flow_implicit_alias_for_current_namespace.sl").toURI();
        Executable subFlowModel = compiler.preCompile(SlangSource.fromFile(subFlowUri));
        Set<Executable> dependencies = new HashSet<>();
        dependencies.add(subFlowModel);
        dependencies.add(operation2Model);
        List<RuntimeException> errors = compiler.validateSlangModelWithDirectDependencies(flowModel, dependencies);
        Assert.assertEquals(0, errors.size());
    }

    @Test
    public void testValidationOfFlowWithMissingNavigationFromOperationResult() throws Exception {
        URI flowUri = getClass()
                .getResource("/corrupted/step_with_missing_navigation_from_operation_result_flow.sl").toURI();
        final Executable flowModel = compiler.preCompile(SlangSource.fromFile(flowUri));

        URI operationUri = getClass().getResource("/java_op.sl").toURI();
        Executable operationModel = compiler.preCompile(SlangSource.fromFile(operationUri));
        Set<Executable> dependencies = new HashSet<>();
        dependencies.add(operationModel);

        exception.expect(RuntimeException.class);
        exception.expectMessage("Cannot compile flow 'step_with_missing_navigation_from_operation_result_flow' " +
                "since for step 'step1' the results [FAILURE] of its dependency 'user.ops.java_op' " +
                "have no matching navigation.");
        List<RuntimeException> errors = compiler.validateSlangModelWithDirectDependencies(flowModel, dependencies);
        Assert.assertEquals(1, errors.size());
        throw errors.get(0);
    }

    @Test
    public void testInputsNoDefaultNotInStep() throws Exception {
        final URI flowUri = getClass()
                .getResource("/shallow_validation/test_inputs_no_default_not_in_step.sl").toURI();
        final Executable flowModel = compiler.preCompile(SlangSource.fromFile(flowUri));

        final URI operation1Uri = getClass().getResource("/shallow_validation/test_op.sl").toURI();
        Executable operation1Model = compiler.preCompile(SlangSource.fromFile(operation1Uri));
        Set<Executable> dependencies = new HashSet<>();
        dependencies.add(operation1Model);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Cannot compile flow 'io.cloudslang.test_inputs_no_default_not_in_step'. " +
                "Step 'explicit_alias' does not declare all the mandatory inputs of its reference. " +
                "The following inputs of 'user.ops.test_op' are not private, " +
                "required and with no default value: alla.");
        List<RuntimeException> errors = compiler.validateSlangModelWithDirectDependencies(flowModel, dependencies);
        Assert.assertEquals(1, errors.size());
        throw errors.get(0);
    }

    @Test
    public void testInputsEmptyStringDefaultNotInStep() throws Exception {
        final URI flowUri = getClass()
                .getResource("/shallow_validation/test_inputs_empty_string_default_not_in_step.sl").toURI();
        final Executable flowModel = compiler.preCompile(SlangSource.fromFile(flowUri));

        final URI operation1Uri = getClass().getResource("/shallow_validation/check_op.sl").toURI();
        Executable operation1Model = compiler.preCompile(SlangSource.fromFile(operation1Uri));
        Set<Executable> dependencies = new HashSet<>();
        dependencies.add(operation1Model);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Cannot compile flow 'io.cloudslang.test_inputs_empty_string_default_not_in_step'." +
                " Step 'explicit_alias' does not declare all the mandatory inputs of its reference. " +
                "The following inputs of 'io.cloudslang.check_op' are not private, " +
                "required and with no default value: alla."
        );
        List<RuntimeException> errors = compiler.validateSlangModelWithDirectDependencies(flowModel, dependencies);
        Assert.assertEquals(1, errors.size());
        throw errors.get(0);
    }

    @Test
    public void testInputsNoDefaultInStep() throws Exception {
        final URI flowUri = getClass()
                .getResource("/shallow_validation/test_inputs_no_default_in_step.sl").toURI();
        final Executable flowModel = compiler.preCompile(SlangSource.fromFile(flowUri));

        final URI operation1Uri = getClass().getResource("/shallow_validation/test_op.sl").toURI();
        Executable operation1Model = compiler.preCompile(SlangSource.fromFile(operation1Uri));
        Set<Executable> dependencies = new HashSet<>();
        dependencies.add(operation1Model);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Cannot compile flow 'io.cloudslang.test_inputs_no_default_in_step'. " +
                "Step 'explicit_alias' does not declare all the mandatory inputs of its reference." +
                " The following inputs of 'user.ops.test_op' are not private, " +
                "required and with no default value: alla."
        );
        List<RuntimeException> errors = compiler.validateSlangModelWithDirectDependencies(flowModel, dependencies);
        Assert.assertEquals(1, errors.size());
        throw errors.get(0);
    }

    @Test
    public void testInputsEmptyStringDefaultInStep() throws Exception {
        final URI flowUri = getClass()
                .getResource("/shallow_validation/test_inputs_empty_string_default_in_step.sl").toURI();
        final Executable flowModel = compiler.preCompile(SlangSource.fromFile(flowUri));

        final URI operation1Uri = getClass().getResource("/shallow_validation/check_op.sl").toURI();
        Executable operation1Model = compiler.preCompile(SlangSource.fromFile(operation1Uri));
        Set<Executable> dependencies = new HashSet<>();
        dependencies.add(operation1Model);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Cannot compile flow 'io.cloudslang.test_inputs_empty_string_default_in_step'." +
                " Step 'explicit_alias' does not declare all the mandatory inputs of its reference." +
                " The following inputs of 'io.cloudslang.check_op' are not private, " +
                "required and with no default value: alla."
        );
        List<RuntimeException> errors = compiler.validateSlangModelWithDirectDependencies(flowModel, dependencies);
        Assert.assertEquals(1, errors.size());
        throw errors.get(0);
    }

    @Test
    public void testValidationOfFlowInputInStepWithSameNameAsDependencyOutput() throws Exception {
        final URI flowUri = getClass()
                .getResource("/corrupted/flow_input_in_step_same_name_as_dependency_output.sl").toURI();
        final Executable flowModel = compiler.preCompile(SlangSource.fromFile(flowUri));

        final URI operation1Uri = getClass().getResource("/test_op.sl").toURI();
        final Executable operation1Model = compiler.preCompile(SlangSource.fromFile(operation1Uri));
        URI operation2Uri = getClass().getResource("/check_op.sl").toURI();
        final Executable operation2Model = compiler.preCompile(SlangSource.fromFile(operation2Uri));
        Set<Executable> dependencies = new HashSet<>();
        dependencies.add(operation1Model);
        dependencies.add(operation2Model);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(
                "Cannot compile flow 'io.cloudslang.flow_input_in_step_same_name_as_dependency_output'. " +
                "Step 'explicit_alias' has input 'balla' with the same name as the " +
                "one of the outputs of 'user.ops.test_op'.");
        List<RuntimeException> errors = compiler.validateSlangModelWithDirectDependencies(flowModel, dependencies);
        Assert.assertEquals(1, errors.size());
        throw errors.get(0);
    }
}
