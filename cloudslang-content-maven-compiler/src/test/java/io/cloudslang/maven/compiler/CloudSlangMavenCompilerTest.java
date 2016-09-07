package io.cloudslang.maven.compiler;

import java.util.ArrayList;
import org.codehaus.plexus.compiler.AbstractCompilerTest;
import org.codehaus.plexus.compiler.Compiler;
import org.codehaus.plexus.compiler.CompilerConfiguration;

/**
 * Created by hanael on 10/07/2016.
 */

public class CloudSlangMavenCompilerTest extends AbstractCompilerTest {


    public CloudSlangMavenCompilerTest() {
        super();
    }

    @Override
    protected String getRoleHint() {
        return "cloudslang";
    }

    @Override
    protected int expectedErrors() {
        return 3;
    }

    @Override
    public void testCompilingSources() throws Exception {
        ArrayList messages = new ArrayList();
        CompilerConfiguration compilerConfiguration = getCompilerConfiguration();

        Compiler compiler = (Compiler) this.lookup(Compiler.ROLE, this.getRoleHint());
        messages.addAll(compiler.performCompile(compilerConfiguration).getCompilerMessages());

        int numCompilerErrors = compilerErrorCount(messages);

        assertEquals("Wrong number of compilation errors.", expectedErrors(), numCompilerErrors);
    }


    private CompilerConfiguration getCompilerConfiguration() throws Exception {
        String sourceDir = getBasedir() + "/src/test/resources/content";

        CompilerConfiguration compilerConfig = new CompilerConfiguration();
        compilerConfig.setClasspathEntries(this.getClasspath());
        compilerConfig.addSourceLocation(sourceDir);

        return compilerConfig;
    }
}
