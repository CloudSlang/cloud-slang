/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.maven.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
    public void testCompilingSources() throws Exception {
        ArrayList messages = new ArrayList();

        Compiler compiler = (Compiler) this.lookup(Compiler.ROLE, this.getRoleHint());
        messages.addAll(compiler.performCompile(getCompilerConfigurationShallowCompile()).getCompilerMessages());

        int numCompilerErrors = compilerErrorCount(messages);

        assertEquals("Wrong number of compilation errors.", 4, numCompilerErrors);

        messages.clear();
        messages.addAll(compiler.performCompile(getCompilerConfigurationNoShallowCompile()).getCompilerMessages());

        numCompilerErrors = compilerErrorCount(messages);

        assertEquals("Wrong number of compilation errors.", 3, numCompilerErrors);

        messages.clear();
        messages.addAll(compiler.performCompile(getCompilerConfigurationDontFailOnErrors()).getCompilerMessages());

        numCompilerErrors = compilerErrorCount(messages);

        assertEquals("Wrong number of compilation errors.", 0, numCompilerErrors);
    }

    private CompilerConfiguration getCompilerConfigurationShallowCompile() throws Exception {
        String sourceDir = getBasedir() + "/src/test/resources/content";
        Map<String, String> customCompilerArguments = new HashMap<>();

        CompilerConfiguration compilerConfig = new CompilerConfiguration();
        compilerConfig.setClasspathEntries(this.getClasspath());
        compilerConfig.addSourceLocation(sourceDir);
        compilerConfig.setCustomCompilerArgumentsAsMap(customCompilerArguments);

        return compilerConfig;
    }

    private CompilerConfiguration getCompilerConfigurationNoShallowCompile() throws Exception {
        String sourceDir = getBasedir() + "/src/test/resources/content";
        Map<String, String> customCompilerArguments = new HashMap<>();
        customCompilerArguments.put("ignore-dependencies", "");


        CompilerConfiguration compilerConfig = new CompilerConfiguration();
        compilerConfig.setClasspathEntries(this.getClasspath());
        compilerConfig.addSourceLocation(sourceDir);
        compilerConfig.setCustomCompilerArgumentsAsMap(customCompilerArguments);

        return compilerConfig;
    }

    private CompilerConfiguration getCompilerConfigurationDontFailOnErrors() throws Exception {
        String sourceDir = getBasedir() + "/src/test/resources/content";
        Map<String, String> customCompilerArguments = new HashMap<>();
        customCompilerArguments.put("ignore-errors", "");

        CompilerConfiguration compilerConfig = new CompilerConfiguration();
        compilerConfig.setClasspathEntries(this.getClasspath());
        compilerConfig.addSourceLocation(sourceDir);
        compilerConfig.setCustomCompilerArgumentsAsMap(customCompilerArguments);

        return compilerConfig;
    }
}
