package io.cloudslang.lang.compiler;

import io.cloudslang.lang.compiler.modeller.model.Executable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.net.URL;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig.class})
public class ExternalSlangCompilerIntegrationTest {
    @Autowired
    private SlangCompiler slangCompiler;

    @Test
    public void testExternalStepDraciLaciPLM() throws Exception {
        final URL resource = getClass().getResource("/flow_with_external_steps.yaml");
        final SlangSource slangSource = SlangSource.fromFile(new File(resource.toURI()));
        final Executable slangExecutable = slangCompiler.preCompile(slangSource);

        System.out.println(slangExecutable.toString());

    }
}
