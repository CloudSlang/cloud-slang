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
import io.cloudslang.lang.compiler.modeller.model.Decision;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.lang.entities.bindings.ScriptFunction;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.api.ExecutionStep;

import java.io.Serializable;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;
import org.apache.commons.collections4.MapUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.google.common.collect.Sets.newHashSet;
import static io.cloudslang.lang.compiler.SlangSource.fromFile;

/**
 * @author Bonczidai Levente
 * @since 7/7/2016
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SlangCompilerSpringConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CompileDecisionTest {
    private static final HashSet<ScriptFunction> SP_SCRIPT_FUNCTIONS_SET =
            newHashSet(ScriptFunction.GET_SYSTEM_PROPERTY);
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    private SlangCompiler compiler;

    private Set<SlangSource> emptySetSlangSource = Collections.emptySet();
    private Set<String> emptySetSystemProperties = Collections.emptySet();
    private List<Output> emptyListOutputs = Collections.emptyList();
    private Map<String, Serializable> emptyActionData = Collections.emptyMap();
    private List<Input> inputs1 = Lists.newArrayList(
            new Input.InputBuilder("x", null).build(),
            new Input.InputBuilder("y", null).build()
    );
    private List<Input> inputs2 = Lists.newArrayList(
            new Input.InputBuilder("x", null).build(),
            new Input.InputBuilder("y", null).build(),
            new Input.InputBuilder("z", "default_value").withRequired(false).build()
    );
    private List<Input> inputs3 = Lists.newArrayList(
            new Input.InputBuilder("x", "${get_sp('user.sys.prop1')}")
                    .withSystemPropertyDependencies(newHashSet("user.sys.prop1"))
                    .withFunctionDependencies(SP_SCRIPT_FUNCTIONS_SET)
                    .build(),
            new Input.InputBuilder("y", "${get_sp('user.sys.prop2')}")
                    .withRequired(false)
                    .withSystemPropertyDependencies(newHashSet("user.sys.prop2"))
                    .withFunctionDependencies(SP_SCRIPT_FUNCTIONS_SET)
                    .build()
    );
    private List<Output> outputs1 = Lists.newArrayList(
            new Output("sum", ValueFactory.create("${x+y}"))
    );
    private List<Output> outputs2 = Lists.newArrayList(
            new Output(
                    "sum",
                    ValueFactory.create("${get_sp('user.sys.prop3')}"),
                    SP_SCRIPT_FUNCTIONS_SET,
                    newHashSet("user.sys.prop3")
            )
    );
    private Set<String> spSet1 = newHashSet(
            "user.sys.prop1",
            "user.sys.prop2",
            "user.sys.prop3",
            "user.sys.prop4"
    );
    List<Result> results1 = Lists.newArrayList(
            new Result("EQUAL", ValueFactory.create("${x == y}")),
            new Result("LESS_THAN", ValueFactory.create("${x < y}")),
            new Result("GREATER_THAN", null)
    );
    List<Result> results2 = Lists.newArrayList(
            new Result(
                    "EQUAL",
                    ValueFactory.create("${x == get_sp('user.sys.prop4')}"),
                    SP_SCRIPT_FUNCTIONS_SET,
                    newHashSet("user.sys.prop4")
            ),
            new Result("LESS_THAN", ValueFactory.create("${x < y}")),
            new Result("GREATER_THAN", null)
    );

    @Test
    public void testDecision1PreCompile() throws Exception {
        URL decision = getClass().getResource("/decision/decision_1.sl");

        Executable executable = compiler.preCompile(fromFile(decision.toURI()));

        Assert.assertNotNull(executable);
        Assert.assertTrue(executable instanceof Decision);
        Decision expectedDecision = new Decision(
                emptyActionData,
                emptyActionData,
                "user.decisions",
                "decision_1",
                inputs1,
                outputs1,
                results1,
                Collections.<String>emptySet(),
                emptySetSystemProperties
        );
        Assert.assertEquals(expectedDecision, executable);
    }

    @Test
    public void testDecision1() throws Exception {
        URL decision = getClass().getResource("/decision/decision_1.sl");

        CompilationArtifact compilationArtifact = compiler.compile(fromFile(decision.toURI()), emptySetSlangSource);

        validateCompilationArtifact(compilationArtifact, inputs1, outputs1, results1, emptySetSystemProperties);
    }

    @Test
    public void testDecision2() throws Exception {
        URL decision = getClass().getResource("/decision/decision_2.sl");

        CompilationArtifact compilationArtifact = compiler.compile(fromFile(decision.toURI()), emptySetSlangSource);

        validateCompilationArtifact(compilationArtifact, inputs2, emptyListOutputs, results1, emptySetSystemProperties);
    }

    @Test
    public void testDecisionSystemPropertyDependencies() throws Exception {
        URL decision = getClass().getResource("/decision/decision_3_sp.sl");

        CompilationArtifact compilationArtifact = compiler.compile(fromFile(decision.toURI()), emptySetSlangSource);

        validateCompilationArtifact(compilationArtifact, inputs3, outputs2, results2, spSet1);
    }

    @Test
    public void testDecisionWrongKey() throws Exception {
        URL decision = getClass().getResource("/decision/decision_4_unrecognized_key.sl");

        exception.expect(RuntimeException.class);
        exception.expectMessage(
                "Artifact {decision_4_py_action_key} has unrecognized tag {wrong_key}." +
                        " Please take a look at the supported features per versions link"
        );
        compiler.compile(fromFile(decision.toURI()), emptySetSlangSource);
    }

    @Test
    public void testDecisionPyActionKey() throws Exception {
        URL decision = getClass().getResource("/decision/decision_4_py_action_key.sl");

        exception.expect(RuntimeException.class);
        exception.expectMessage(
                "Artifact {decision_4_py_action_key} has unrecognized tag {python_action}." +
                        " Please take a look at the supported features per versions link"
        );
        compiler.compile(fromFile(decision.toURI()), emptySetSlangSource);
    }

    @Test
    public void testDecisionMissingResults() throws Exception {
        URL decision = getClass().getResource("/decision/decision_wo_results.sl");

        exception.expect(RuntimeException.class);
        exception.expectMessage(
                "Artifact {decision_wo_results} syntax is invalid:" +
                        " 'results' section cannot be empty for executable type 'decision'"
        );
        compiler.compile(fromFile(decision.toURI()), emptySetSlangSource);
    }

    private void validateCompilationArtifact(
            CompilationArtifact compilationArtifact,
            List<Input> expectedInputs,
            List<Output> expectedOutputs,
            List<Result> expectedResults,
            Set<String> expectedSystemProperties) {
        Assert.assertNotNull(compilationArtifact);

        validateExecutionPlan(compilationArtifact.getExecutionPlan(), expectedInputs, expectedOutputs, expectedResults);

        Assert.assertTrue(MapUtils.isEmpty(compilationArtifact.getDependencies()));
        Assert.assertEquals(expectedInputs, compilationArtifact.getInputs());
        Assert.assertEquals(expectedSystemProperties, compilationArtifact.getSystemProperties());
    }

    private void validateExecutionPlan(
            ExecutionPlan executionPlan,
            Object expectedInputs,
            Object expectedOutputs,
            Object expectedResults) {
        Assert.assertNotNull(executionPlan);
        Map<Long, ExecutionStep> steps = executionPlan.getSteps();
        Assert.assertNotNull(steps);
        Assert.assertEquals(2, steps.size());

        ExecutionStep firstStep = steps.get(1L);
        Assert.assertNotNull(firstStep);
        Map<String, ?> actionData1 = firstStep.getActionData();
        Assert.assertNotNull(actionData1);
        //inputs
        Object actualInputs = actionData1.get(ScoreLangConstants.EXECUTABLE_INPUTS_KEY);
        Assert.assertEquals(expectedInputs, actualInputs);

        ExecutionStep secondStep = steps.get(2L);
        Assert.assertNotNull(secondStep);
        Map<String, ?> actionData2 = secondStep.getActionData();
        Assert.assertNotNull(actionData2);
        //outputs
        Object actualOutputs = actionData2.get(ScoreLangConstants.EXECUTABLE_OUTPUTS_KEY);
        Assert.assertEquals(expectedOutputs, actualOutputs);
        //results
        Object actualResults = actionData2.get(ScoreLangConstants.EXECUTABLE_RESULTS_KEY);
        Assert.assertEquals(expectedResults, actualResults);
    }

}
