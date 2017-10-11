package io.cloudslang.lang.compiler;

import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.compiler.scorecompiler.ScoreCompiler;
import java.util.ArrayList;
import java.util.HashSet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig.class})
public class ExternalSlangCompilerIntegrationTest {
    @Autowired
    private SlangCompiler slangCompiler;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testFlowWithExternalStep() throws Exception {
        final URL resource = getClass().getResource("/flow_with_external_steps.yaml");
        final SlangSource slangSource = SlangSource.fromFile(new File(resource.toURI()));
        final Flow slangExecutable = (Flow) slangCompiler.preCompile(slangSource);

        assertTrue(slangExecutable.getExternalExecutableDependencies()
                .contains("/Library/Utility Operations/Flow Variable Manipulation/Do Nothing"));
        assertEquals(slangExecutable.getWorkflow().getSteps(), 4);
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
