/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package com.hp.score.lang.cli;

import com.google.common.collect.Lists;
import com.hp.score.lang.cli.services.ScoreServices;
import com.hp.score.lang.cli.utils.CompilerHelper;
import com.hp.score.lang.entities.CompilationArtifact;
import com.hp.score.lang.entities.bindings.Input;

import org.eclipse.score.api.ExecutionPlan;
import org.junit.*;
import org.springframework.shell.Bootstrap;
import org.springframework.shell.core.CommandResult;
import org.springframework.shell.core.JLineShellComponent;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Date: 12/9/2014
 *
 * @author Bonczidai Levente
 */
public class SlangCLITest {

    private final static String[] CONTEXT_PATH = { "classpath*:/META-INF/spring/test-spring-shell-plugin.xml" };
    private final static String FLOW_PATH_SLAH = "C:\\\\Users\\\\bonczida\\\\Documents\\\\Score_related\\\\score-language\\\\score-lang-cli\\\\src\\\\test\\\\resources\\\\flow.yaml";
    private final static String FLOW_PATH_BACKSLASH = "C:\\Users\\bonczida\\Documents\\Score_related\\score-language\\score-lang-cli\\src\\test\\resources\\flow.yaml";
    private final static String DEPENDENCIES_PATH_SLASH = "C:\\\\flowsdir\\\\";
    private final static String DEPENDENCIES_PATH_BACKSLASH = "C:\\flowsdir";
    private static final long DEFAULT_TIMEOUT = 10000;
    private JLineShellComponent shell;
    private SlangCLI slangCLI;
    private ScoreServices ScoreServicesMock;
    private CompilerHelper compilerHelperMock;

    public SlangCLITest() {
        Bootstrap bootstrap = new Bootstrap(null, CONTEXT_PATH);
        shell = bootstrap.getJLineShellComponent();
        ScoreServicesMock = (ScoreServices) bootstrap.getApplicationContext().getBean("scoreServices");
        compilerHelperMock = (CompilerHelper) bootstrap.getApplicationContext().getBean("compilerHelper");
        slangCLI = bootstrap.getApplicationContext().getBean(SlangCLI.class);
    }

    @Before
    public void before() throws IOException {
        slangCLI.setEnvVar(false);
    }

    @Test (timeout = DEFAULT_TIMEOUT)
    public void testRunValidFilePathSync() throws URISyntaxException, IOException {
        CompilationArtifact compilationArtifact = new CompilationArtifact(new ExecutionPlan(), new HashMap<String, ExecutionPlan>(), new ArrayList<Input>());
        long executionID = 1;

        when(compilerHelperMock.compile(FLOW_PATH_BACKSLASH, null, null)).thenReturn(compilationArtifact);
        when(ScoreServicesMock.triggerSync(eq(compilationArtifact), anyMapOf(String.class, Serializable.class))).thenReturn(executionID);

        CommandResult cr = shell.executeCommand("run --f " + FLOW_PATH_SLAH);

        verify(compilerHelperMock).compile(FLOW_PATH_BACKSLASH, null, null);
        verify(ScoreServicesMock).triggerSync(eq(compilationArtifact), anyMapOf(String.class, Serializable.class));

        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

    @Test (timeout = DEFAULT_TIMEOUT)
    public void testRunValidFilePathAsync() throws URISyntaxException, IOException {
        //set async mode
        slangCLI.setEnvVar(true);

        CompilationArtifact compilationArtifact = new CompilationArtifact(new ExecutionPlan(), new HashMap<String, ExecutionPlan>(), new ArrayList<Input>());
        long executionID = 1;

        when(compilerHelperMock.compile(FLOW_PATH_BACKSLASH, null, null)).thenReturn(compilationArtifact);
        when(ScoreServicesMock.trigger(eq(compilationArtifact), anyMapOf(String.class, Serializable.class))).thenReturn(executionID);

        CommandResult cr = shell.executeCommand("run --f " + FLOW_PATH_SLAH);

        verify(compilerHelperMock).compile(FLOW_PATH_BACKSLASH, null, null);
        verify(ScoreServicesMock).trigger(eq(compilationArtifact), anyMapOf(String.class, Serializable.class));

        Assert.assertEquals("method result mismatch", SlangCLI.triggerAsyncMsg(executionID, compilationArtifact.getExecutionPlan().getName()), cr.getResult());
        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

    @Test (timeout = DEFAULT_TIMEOUT)
    public void testRunValidWithOtherPathForDependencies() throws URISyntaxException, IOException {
        CompilationArtifact compilationArtifact = new CompilationArtifact(new ExecutionPlan(), new HashMap<String, ExecutionPlan>(), new ArrayList<Input>());
        long executionID = 1;

        when(compilerHelperMock.compile(FLOW_PATH_BACKSLASH, null, DEPENDENCIES_PATH_BACKSLASH)).thenReturn(compilationArtifact);
        when(ScoreServicesMock.triggerSync(eq(compilationArtifact), anyMapOf(String.class, Serializable.class))).thenReturn(executionID);

        CommandResult cr = shell.executeCommand("run --f " + FLOW_PATH_SLAH + " --cp " + DEPENDENCIES_PATH_SLASH);

        verify(compilerHelperMock).compile(FLOW_PATH_BACKSLASH, null, DEPENDENCIES_PATH_BACKSLASH);
        verify(ScoreServicesMock).triggerSync(eq(compilationArtifact), anyMapOf(String.class, Serializable.class));

        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

    @Test (timeout = DEFAULT_TIMEOUT)
    public void testRunSyncWithInputs() throws URISyntaxException, IOException {
        CompilationArtifact compilationArtifact = new CompilationArtifact(new ExecutionPlan(), new HashMap<String, ExecutionPlan>(), new ArrayList<Input>());
        long executionID = 1;
        String inputsString = "--i input1=value1,input2=value2";
        Map<String, Serializable> inputsMap = new HashMap<>();
        inputsMap.put("input1", "value1");
        inputsMap.put("input2", "value2");

        when(compilerHelperMock.compile(FLOW_PATH_BACKSLASH, null, null)).thenReturn(compilationArtifact);
        when(ScoreServicesMock.triggerSync(eq(compilationArtifact), eq(inputsMap))).thenReturn(executionID);

        CommandResult cr = shell.executeCommand("run --f " + FLOW_PATH_SLAH + " " + inputsString);

        verify(compilerHelperMock).compile(FLOW_PATH_BACKSLASH, null, null);
        verify(ScoreServicesMock).triggerSync(eq(compilationArtifact), eq(inputsMap));

        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

    @Test (timeout = DEFAULT_TIMEOUT)
    public void testRunAsyncWithInputs() throws URISyntaxException, IOException {
        //set async mode
        slangCLI.setEnvVar(true);

        CompilationArtifact compilationArtifact = new CompilationArtifact(new ExecutionPlan(), new HashMap<String, ExecutionPlan>(), new ArrayList<Input>());
        long executionID = 1;
        String inputsString = "--i input1=value1,input2=value2";
        Map<String, Serializable> inputsMap = new HashMap<>();
        inputsMap.put("input1", "value1");
        inputsMap.put("input2", "value2");

        when(compilerHelperMock.compile(FLOW_PATH_BACKSLASH, null, null)).thenReturn(compilationArtifact);
        when(ScoreServicesMock.trigger(eq(compilationArtifact), eq(inputsMap))).thenReturn(executionID);

        CommandResult cr = shell.executeCommand("run --f " + FLOW_PATH_SLAH + " " + inputsString);

        verify(compilerHelperMock).compile(FLOW_PATH_BACKSLASH, null, null);
        verify(ScoreServicesMock).trigger(eq(compilationArtifact), eq(inputsMap));

        Assert.assertEquals("method result mismatch", SlangCLI.triggerAsyncMsg(executionID, compilationArtifact.getExecutionPlan().getName()), cr.getResult());
        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

    @Test (timeout = DEFAULT_TIMEOUT)
    public void testSetEnvVarTrue() {
        CommandResult cr = shell.executeCommand("env --setAsync true");

        Assert.assertEquals("method result mismatch", SlangCLI.setEnvMessage(true), cr.getResult());
        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

    @Test (timeout = DEFAULT_TIMEOUT)
    public void testSetEnvVarFalse() {
        CommandResult cr = shell.executeCommand("env --setAsync false");

        Assert.assertEquals("method result mismatch", SlangCLI.setEnvMessage(false), cr.getResult());
        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

    @Test (timeout = DEFAULT_TIMEOUT)
    public void testGetFlowInputs() throws URISyntaxException, IOException {
        List<Input> inputsList = Lists.newArrayList(new Input("input1", "expression1"), new Input("input2", "expression2"));
        CompilationArtifact compilationArtifact = new CompilationArtifact(new ExecutionPlan(), new HashMap<String, ExecutionPlan>(), inputsList);

        when(compilerHelperMock.compile(FLOW_PATH_BACKSLASH, null, null)).thenReturn(compilationArtifact);

        CommandResult cr = shell.executeCommand("inputs --f " + FLOW_PATH_SLAH);

        verify(compilerHelperMock).compile(FLOW_PATH_BACKSLASH, null, null);

        Assert.assertEquals("input list mismatch", Lists.newArrayList("input1", "input2"), cr.getResult());
        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

    @Test (timeout = DEFAULT_TIMEOUT)
    public void testGetFlowInputsWithOverride() throws URISyntaxException, IOException {
        List<Input> inputsList = Lists.newArrayList(new Input("input1", "expression1"),new Input("input_override", "expression_override", false, true, true) , new Input("input2", "expression2"));
        CompilationArtifact compilationArtifact = new CompilationArtifact(new ExecutionPlan(), new HashMap<String, ExecutionPlan>(), inputsList);

        when(compilerHelperMock.compile(FLOW_PATH_BACKSLASH, null, null)).thenReturn(compilationArtifact);

        CommandResult cr = shell.executeCommand("inputs --f " + FLOW_PATH_SLAH);

        verify(compilerHelperMock).compile(FLOW_PATH_BACKSLASH, null, null);

        Assert.assertEquals("input list mismatch", Lists.newArrayList("input1", "input2"), cr.getResult());
        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

    @Test (timeout = DEFAULT_TIMEOUT)
    public void testGetVersion() throws URISyntaxException, IOException {
        CommandResult cr = shell.executeCommand("slang --version");

        Assert.assertEquals("method result mismatch", slangCLI.version(), cr.getResult());
        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }
}
