/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.lang.cli;

import com.google.common.collect.Lists;
import org.openscore.lang.cli.services.ScoreServices;
import org.openscore.lang.cli.utils.CompilerHelper;
import org.openscore.lang.entities.CompilationArtifact;
import org.openscore.lang.entities.bindings.Input;
import org.openscore.api.ExecutionPlan;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
    private final static String FLOW_PATH_SLAH = "C:\\\\flow.yaml";
    private final static String FLOW_PATH_BACKSLASH = "C:\\flow.yaml";
    private final static String DEPENDENCIES_PATH_SLASH = "C:\\\\flowsdir\\\\";
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

        when(compilerHelperMock.compile(contains(FLOW_PATH_BACKSLASH), isNull(String.class), isNull(List.class))).thenReturn(compilationArtifact);
        when(ScoreServicesMock.triggerSync(eq(compilationArtifact), anyMapOf(String.class, Serializable.class))).thenReturn(executionID);

        CommandResult cr = shell.executeCommand("run --f " + FLOW_PATH_SLAH);

        // path may be processed as local in some environments
        // in this case the local directory path is prepended to the actual path
        verify(compilerHelperMock).compile(contains(FLOW_PATH_BACKSLASH), isNull(String.class), isNull(List.class));
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

        when(compilerHelperMock.compile(contains(FLOW_PATH_BACKSLASH), isNull(String.class), isNull(List.class))).thenReturn(compilationArtifact);
        when(ScoreServicesMock.trigger(eq(compilationArtifact), anyMapOf(String.class, Serializable.class))).thenReturn(executionID);

        CommandResult cr = shell.executeCommand("run --f " + FLOW_PATH_SLAH);

        verify(compilerHelperMock).compile(contains(FLOW_PATH_BACKSLASH), isNull(String.class), isNull(List.class));
        verify(ScoreServicesMock).trigger(eq(compilationArtifact), anyMapOf(String.class, Serializable.class));

        Assert.assertEquals("method result mismatch", SlangCLI.triggerAsyncMsg(executionID, compilationArtifact.getExecutionPlan().getName()), cr.getResult());
        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

    @Test (timeout = DEFAULT_TIMEOUT)
    public void testRunValidWithOtherPathForDependencies() throws URISyntaxException, IOException {
        CompilationArtifact compilationArtifact = new CompilationArtifact(new ExecutionPlan(), new HashMap<String, ExecutionPlan>(), new ArrayList<Input>());
        long executionID = 1;

        when(compilerHelperMock.compile(contains(FLOW_PATH_BACKSLASH), isNull(String.class), anyListOf(String.class))).thenReturn(compilationArtifact);
        when(ScoreServicesMock.triggerSync(eq(compilationArtifact), anyMapOf(String.class, Serializable.class))).thenReturn(executionID);

        CommandResult cr = shell.executeCommand("run --f " + FLOW_PATH_SLAH + " --cp " + DEPENDENCIES_PATH_SLASH);

        verify(compilerHelperMock).compile(contains(FLOW_PATH_BACKSLASH), isNull(String.class), anyListOf(String.class));
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

        when(compilerHelperMock.compile(contains(FLOW_PATH_BACKSLASH), isNull(String.class), isNull(List.class))).thenReturn(compilationArtifact);
        when(ScoreServicesMock.triggerSync(eq(compilationArtifact), eq(inputsMap))).thenReturn(executionID);

        CommandResult cr = shell.executeCommand("run --f " + FLOW_PATH_SLAH + " " + inputsString);

        verify(compilerHelperMock).compile(contains(FLOW_PATH_BACKSLASH), isNull(String.class), isNull(List.class));
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

        when(compilerHelperMock.compile(contains(FLOW_PATH_BACKSLASH), isNull(String.class), isNull(List.class))).thenReturn(compilationArtifact);
        when(ScoreServicesMock.trigger(eq(compilationArtifact), eq(inputsMap))).thenReturn(executionID);

        CommandResult cr = shell.executeCommand("run --f " + FLOW_PATH_SLAH + " " + inputsString);

        verify(compilerHelperMock).compile(contains(FLOW_PATH_BACKSLASH), isNull(String.class), isNull(List.class));
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

        when(compilerHelperMock.compile(contains(FLOW_PATH_BACKSLASH), isNull(String.class), isNull(List.class))).thenReturn(compilationArtifact);

        CommandResult cr = shell.executeCommand("inputs --f " + FLOW_PATH_SLAH);

        verify(compilerHelperMock).compile(contains(FLOW_PATH_BACKSLASH), isNull(String.class), isNull(List.class));

        Assert.assertEquals("input list mismatch", Lists.newArrayList("input1", "input2"), cr.getResult());
        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

    @Test (timeout = DEFAULT_TIMEOUT)
    public void testGetFlowInputsWithOverride() throws URISyntaxException, IOException {
        List<Input> inputsList = Lists.newArrayList(new Input("input1", "expression1"),new Input("input_override", "expression_override", false, true, true) , new Input("input2", "expression2"));
        CompilationArtifact compilationArtifact = new CompilationArtifact(new ExecutionPlan(), new HashMap<String, ExecutionPlan>(), inputsList);

        when(compilerHelperMock.compile(contains(FLOW_PATH_BACKSLASH), isNull(String.class), isNull(List.class))).thenReturn(compilationArtifact);

        CommandResult cr = shell.executeCommand("inputs --f " + FLOW_PATH_SLAH);

        verify(compilerHelperMock).compile(contains(FLOW_PATH_BACKSLASH), isNull(String.class), isNull(List.class));

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
