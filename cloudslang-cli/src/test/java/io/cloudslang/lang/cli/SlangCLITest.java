/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.cli;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.cloudslang.lang.cli.services.ScoreServices;
import io.cloudslang.lang.cli.utils.CompilerHelper;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.score.api.ExecutionPlan;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.shell.Bootstrap;
import org.springframework.shell.core.CommandResult;
import org.springframework.shell.core.JLineShellComponent;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Date: 12/9/2014
 *
 * @author Bonczidai Levente
 */
public class SlangCLITest {

    static final CompilationArtifact emptyCompilationArtifact = new CompilationArtifact(new ExecutionPlan(), new HashMap<String, ExecutionPlan>(), new ArrayList<Input>(), new HashSet<String>());
    private final static String[] CONTEXT_PATH = {"classpath*:/META-INF/spring/test-spring-shell-plugin.xml"};
    private final static String FLOW_PATH_BACKSLASH_INPUT = "C:\\\\basic_flow.yaml";
    private final static String FLOW_PATH_BACKSLASH = "C:\\basic_flow.yaml";
    private final static String DEPENDENCIES_PATH_BACKSLASH = "C:\\\\executables.dir1\\\\";
    private static final long DEFAULT_TIMEOUT = 10000;
    public static final String INPUT_FILE_PATH = "/inputs/inputs.yaml";

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
    public void before() throws Exception {
        slangCLI.setEnvVar(false);
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testRunQuietlyValidFilePathAsync() throws Exception {
        slangCLI.setEnvVar(true);

        long executionID = 1;

        when(compilerHelperMock.compile(contains(FLOW_PATH_BACKSLASH), isNull(List.class))).thenReturn(emptyCompilationArtifact);
        when(ScoreServicesMock.trigger(eq(emptyCompilationArtifact), anyMapOf(String.class, Value.class), anySetOf(SystemProperty.class))).thenReturn(executionID);

        CommandResult cr = shell.executeCommand("run --f " + FLOW_PATH_BACKSLASH_INPUT + " --v quiet");

        verify(compilerHelperMock).compile(contains(FLOW_PATH_BACKSLASH), isNull(List.class));
        verify(ScoreServicesMock).trigger(eq(emptyCompilationArtifact), anyMapOf(String.class, Value.class), anySetOf(SystemProperty.class));

        Assert.assertEquals("method result mismatch", StringUtils.EMPTY, cr.getResult());
        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testRunQuietlyValidFilePathSync() throws Exception {
        long executionID = 1;

        when(compilerHelperMock.compile(contains(FLOW_PATH_BACKSLASH), isNull(List.class))).thenReturn(emptyCompilationArtifact);
        when(ScoreServicesMock.triggerSync(eq(emptyCompilationArtifact), anyMapOf(String.class, Value.class), anySetOf(SystemProperty.class), eq(true), eq(false))).thenReturn(executionID);

        CommandResult cr = shell.executeCommand("run --f " + FLOW_PATH_BACKSLASH_INPUT + " --v quiet");

        verify(compilerHelperMock).compile(contains(FLOW_PATH_BACKSLASH), isNull(List.class));
        verify(ScoreServicesMock).triggerSync(eq(emptyCompilationArtifact), anyMapOf(String.class, Value.class), anySetOf(SystemProperty.class), eq(true), eq(false));

        Assert.assertEquals("method result mismatch", StringUtils.EMPTY, cr.getResult());
        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testRunDebugValidFilePathSync() throws Exception {
        long executionID = 1;

        when(compilerHelperMock.compile(contains(FLOW_PATH_BACKSLASH), isNull(List.class))).thenReturn(emptyCompilationArtifact);
        when(ScoreServicesMock.triggerSync(eq(emptyCompilationArtifact), anyMapOf(String.class, Value.class), anySetOf(SystemProperty.class), eq(true), eq(false))).thenReturn(executionID);

        CommandResult cr = shell.executeCommand("run --f " + FLOW_PATH_BACKSLASH_INPUT + " --v debug");

        verify(compilerHelperMock).compile(contains(FLOW_PATH_BACKSLASH), isNull(List.class));
        verify(ScoreServicesMock).triggerSync(eq(emptyCompilationArtifact), anyMapOf(String.class, Value.class), anySetOf(SystemProperty.class), eq(false), eq(true));

        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testRunInvalidVerboseArgumentValidFilePathSync() throws Exception {
        CommandResult cr = shell.executeCommand("run --f " + FLOW_PATH_BACKSLASH_INPUT + " --v invalidArgument");

        Assert.assertEquals("method threw exception", "Verbose argument is invalid.", cr.getException().getMessage());
        Assert.assertEquals("success should be true", false, cr.isSuccess());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testRunValidFilePathSync() throws Exception {
        long executionID = 1;

        when(compilerHelperMock.compile(contains(FLOW_PATH_BACKSLASH), isNull(List.class))).thenReturn(emptyCompilationArtifact);
        when(ScoreServicesMock.triggerSync(eq(emptyCompilationArtifact), anyMapOf(String.class, Value.class), anySetOf(SystemProperty.class), eq(false), eq(false))).thenReturn(executionID);

        CommandResult cr = shell.executeCommand("run --f " + FLOW_PATH_BACKSLASH_INPUT);

        // path may be processed as local in some environments
        // in this case the local directory path is prepended to the actual path
        verify(compilerHelperMock).compile(contains(FLOW_PATH_BACKSLASH), isNull(List.class));
        verify(ScoreServicesMock).triggerSync(eq(emptyCompilationArtifact), anyMapOf(String.class, Value.class), anySetOf(SystemProperty.class), eq(false), eq(false));

        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testRunValidFilePathAsync() throws Exception {
        //set async mode
        slangCLI.setEnvVar(true);

        long executionID = 1;

        when(compilerHelperMock.compile(contains(FLOW_PATH_BACKSLASH), isNull(List.class))).thenReturn(emptyCompilationArtifact);
        when(ScoreServicesMock.trigger(eq(emptyCompilationArtifact), anyMapOf(String.class, Value.class), anySetOf(SystemProperty.class))).thenReturn(executionID);

        CommandResult cr = shell.executeCommand("run --f " + FLOW_PATH_BACKSLASH_INPUT);

        verify(compilerHelperMock).compile(contains(FLOW_PATH_BACKSLASH), isNull(List.class));
        verify(ScoreServicesMock).trigger(eq(emptyCompilationArtifact), anyMapOf(String.class, Value.class), anySetOf(SystemProperty.class));

        Assert.assertEquals("method result mismatch", SlangCLI.triggerAsyncMsg(executionID, emptyCompilationArtifact.getExecutionPlan().getName()), cr.getResult());
        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testRunValidWithOtherPathForDependencies() throws Exception {
        long executionID = 1;

        when(compilerHelperMock.compile(contains(FLOW_PATH_BACKSLASH), anyListOf(String.class))).thenReturn(emptyCompilationArtifact);
        when(ScoreServicesMock.triggerSync(eq(emptyCompilationArtifact), anyMapOf(String.class, Value.class), anySetOf(SystemProperty.class), eq(false), eq(false))).thenReturn(executionID);

        CommandResult cr = shell.executeCommand("run --f " + FLOW_PATH_BACKSLASH_INPUT + " --cp " + DEPENDENCIES_PATH_BACKSLASH);

        verify(compilerHelperMock).compile(contains(FLOW_PATH_BACKSLASH), anyListOf(String.class));
        verify(ScoreServicesMock).triggerSync(eq(emptyCompilationArtifact), anyMapOf(String.class, Value.class), anySetOf(SystemProperty.class), eq(false), eq(false));

        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testRunSyncWithInputsAndFileInputs() throws Exception {
        long executionID = 1;
        String inputsString = "--i input1=value1,input2=value2";

        Map<String, Value> inputsMap = new HashMap<>();
        inputsMap.put("input1", ValueFactory.create("value1"));
        inputsMap.put("input2", ValueFactory.create("value2"));

        Map fileInputsMap = new HashMap<>();
        fileInputsMap.put("host", "localhost");
        fileInputsMap.put("port", "22");

        inputsMap.putAll(fileInputsMap);

        when(compilerHelperMock.compile(contains(FLOW_PATH_BACKSLASH), isNull(List.class))).thenReturn(emptyCompilationArtifact);
        when(compilerHelperMock.loadInputsFromFile(anyList())).thenReturn(fileInputsMap);
        when(ScoreServicesMock.triggerSync(eq(emptyCompilationArtifact), eq(inputsMap), anySetOf(SystemProperty.class), eq(false), eq(false))).thenReturn(executionID);

        CommandResult cr = shell.executeCommand("run --f " + FLOW_PATH_BACKSLASH_INPUT + " " + inputsString);

        verify(compilerHelperMock).compile(contains(FLOW_PATH_BACKSLASH), isNull(List.class));
        verify(ScoreServicesMock).triggerSync(eq(emptyCompilationArtifact), eq(inputsMap), anySetOf(SystemProperty.class), eq(false), eq(false));

        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testRunAsyncWithInputsAndFileInputs() throws Exception {
        slangCLI.setEnvVar(true);

        long executionID = 1;
        String inputsString = "--i input1=value1,input2=value2";

        Map<String, Value> inputsMap = new HashMap<>();
        inputsMap.put("input1", ValueFactory.create("value1"));
        inputsMap.put("input2", ValueFactory.create("value2"));

        Map fileInputsMap = new HashMap<>();
        fileInputsMap.put("host", "localhost");
        fileInputsMap.put("port", "22");

        inputsMap.putAll(fileInputsMap);

        when(compilerHelperMock.compile(contains(FLOW_PATH_BACKSLASH), isNull(List.class))).thenReturn(emptyCompilationArtifact);
        when(compilerHelperMock.loadInputsFromFile(anyList())).thenReturn(fileInputsMap);
        when(ScoreServicesMock.trigger(eq(emptyCompilationArtifact), eq(inputsMap), anySetOf(SystemProperty.class))).thenReturn(executionID);

        CommandResult cr = shell.executeCommand("run --f " + FLOW_PATH_BACKSLASH_INPUT + " " + inputsString);

        verify(compilerHelperMock).compile(contains(FLOW_PATH_BACKSLASH), isNull(List.class));
        verify(ScoreServicesMock).trigger(eq(emptyCompilationArtifact), eq(inputsMap), anySetOf(SystemProperty.class));

        Assert.assertEquals("method result mismatch", SlangCLI.triggerAsyncMsg(executionID, emptyCompilationArtifact.getExecutionPlan().getName()), cr.getResult());
        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testRunSyncWithInputs() throws Exception {
        long executionID = 1;
        String inputsString = "--i input1=value1,input2=value2";
        Map<String, Value> inputsMap = new HashMap<>();
        inputsMap.put("input1", ValueFactory.create("value1"));
        inputsMap.put("input2", ValueFactory.create("value2"));

        when(compilerHelperMock.compile(contains(FLOW_PATH_BACKSLASH), isNull(List.class))).thenReturn(emptyCompilationArtifact);
        when(ScoreServicesMock.triggerSync(eq(emptyCompilationArtifact), eq(inputsMap), anySetOf(SystemProperty.class), eq(false), eq(false))).thenReturn(executionID);

        CommandResult cr = shell.executeCommand("run --f " + FLOW_PATH_BACKSLASH_INPUT + " " + inputsString);

        verify(compilerHelperMock).compile(contains(FLOW_PATH_BACKSLASH), isNull(List.class));
        verify(ScoreServicesMock).triggerSync(eq(emptyCompilationArtifact), eq(inputsMap), anySetOf(SystemProperty.class), eq(false), eq(false));

        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testRunException() throws IOException {
        RuntimeException exception = new RuntimeException("exception message");

        when(compilerHelperMock.compile(contains(FLOW_PATH_BACKSLASH), isNull(List.class))).thenReturn(emptyCompilationArtifact);
        when(ScoreServicesMock.
                triggerSync(eq(emptyCompilationArtifact), anyMapOf(String.class, Value.class), anySetOf(SystemProperty.class), eq(false), eq(false))).
                thenThrow(exception);

        CommandResult cr = shell.executeCommand("run --f " + FLOW_PATH_BACKSLASH_INPUT);

        // path may be processed as local in some environments
        // in this case the local directory path is prepended to the actual path
        verify(compilerHelperMock).compile(contains(FLOW_PATH_BACKSLASH), isNull(List.class));
        verify(ScoreServicesMock).triggerSync(eq(emptyCompilationArtifact), anyMapOf(String.class, Value.class), anySetOf(SystemProperty.class), eq(false), eq(false));

        Assert.assertEquals("exception not as expected", exception, cr.getException());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testRunAsyncWithInputs() throws Exception {
        //set async mode
        slangCLI.setEnvVar(true);

        long executionID = 1;
        String inputsString = "--i input1=value1,input2=value2";
        Map<String, Value> inputsMap = new HashMap<>();
        inputsMap.put("input1", ValueFactory.create("value1"));
        inputsMap.put("input2", ValueFactory.create("value2"));

        when(compilerHelperMock.compile(contains(FLOW_PATH_BACKSLASH), isNull(List.class))).thenReturn(emptyCompilationArtifact);
        when(ScoreServicesMock.trigger(eq(emptyCompilationArtifact), eq(inputsMap), anySetOf(SystemProperty.class))).thenReturn(executionID);

        CommandResult cr = shell.executeCommand("run --f " + FLOW_PATH_BACKSLASH_INPUT + " " + inputsString);

        verify(compilerHelperMock).compile(contains(FLOW_PATH_BACKSLASH), isNull(List.class));
        verify(ScoreServicesMock).trigger(eq(emptyCompilationArtifact), eq(inputsMap), anySetOf(SystemProperty.class));

        Assert.assertEquals("method result mismatch", SlangCLI.triggerAsyncMsg(executionID, emptyCompilationArtifact.getExecutionPlan().getName()), cr.getResult());
        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testRunSyncWithInputFiles() throws Exception {
        long executionID = 1;

        when(compilerHelperMock.compile(contains(FLOW_PATH_BACKSLASH), isNull(List.class))).thenReturn(emptyCompilationArtifact);
        when(ScoreServicesMock.triggerSync(eq(emptyCompilationArtifact), anyMapOf(String.class, Value.class), anySetOf(SystemProperty.class), eq(false), eq(false))).thenReturn(executionID);

        CommandResult cr = shell.executeCommand("run --f " + FLOW_PATH_BACKSLASH_INPUT + " --if " + FLOW_PATH_BACKSLASH_INPUT);

        verify(compilerHelperMock).compile(contains(FLOW_PATH_BACKSLASH), isNull(List.class));
        verify(compilerHelperMock).loadInputsFromFile(Collections.singletonList(FLOW_PATH_BACKSLASH));
        verify(ScoreServicesMock).triggerSync(eq(emptyCompilationArtifact), anyMapOf(String.class, Value.class), anySetOf(SystemProperty.class), eq(false), eq(false));

        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testRunSyncWithSystemProperties() throws Exception {
        long executionID = 1;

        when(compilerHelperMock.compile(contains(FLOW_PATH_BACKSLASH), isNull(List.class))).thenReturn(emptyCompilationArtifact);
        when(ScoreServicesMock.triggerSync(eq(emptyCompilationArtifact), anyMapOf(String.class, Value.class), anySetOf(SystemProperty.class), eq(false), eq(false))).thenReturn(executionID);

        CommandResult cr = shell.executeCommand("run --f " + FLOW_PATH_BACKSLASH_INPUT + " --spf " + FLOW_PATH_BACKSLASH_INPUT);

        verify(compilerHelperMock).compile(contains(FLOW_PATH_BACKSLASH), isNull(List.class));
        verify(compilerHelperMock).loadSystemProperties(Collections.singletonList(FLOW_PATH_BACKSLASH));
        verify(ScoreServicesMock).triggerSync(eq(emptyCompilationArtifact), anyMapOf(String.class, Value.class), anySetOf(SystemProperty.class), eq(false), eq(false));

        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testSetEnvVarTrue() throws Exception {
        CommandResult cr = shell.executeCommand("env --setAsync true");

        Assert.assertEquals("method result mismatch", SlangCLI.setEnvMessage(true), cr.getResult());
        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testSetEnvVarFalse() {
        CommandResult cr = shell.executeCommand("env --setAsync false");

        Assert.assertEquals("method result mismatch", SlangCLI.setEnvMessage(false), cr.getResult());
        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testGetFlowInputs() throws Exception {
        List<Input> inputsList = Lists.newArrayList(
                new Input.InputBuilder("input1", "expression1").build(),
                new Input.InputBuilder("input2", "expression2").build()
        );
        CompilationArtifact compilationArtifact = new CompilationArtifact(new ExecutionPlan(), new HashMap<String, ExecutionPlan>(), inputsList, null);

        when(compilerHelperMock.compile(contains(FLOW_PATH_BACKSLASH), isNull(List.class))).thenReturn(compilationArtifact);

        CommandResult cr = shell.executeCommand("inputs --f " + FLOW_PATH_BACKSLASH_INPUT);

        verify(compilerHelperMock).compile(contains(FLOW_PATH_BACKSLASH), isNull(List.class));

        Assert.assertEquals("input list mismatch", Lists.newArrayList("input1", "input2"), cr.getResult());
        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testGetFlowInputsWithOverride() throws Exception {
        List<Input> inputsList = Lists.newArrayList(
                new Input.InputBuilder("input1", "expression1").build(),
                new Input.InputBuilder("input_override", "expression_override", false)
                        .withRequired(true)
                        .withPrivateInput(true)
                        .build(),
                new Input.InputBuilder("input2", "expression2").build()
        );
        CompilationArtifact compilationArtifact = new CompilationArtifact(new ExecutionPlan(), new HashMap<String, ExecutionPlan>(), inputsList, null);

        when(compilerHelperMock.compile(contains(FLOW_PATH_BACKSLASH), isNull(List.class))).thenReturn(compilationArtifact);

        CommandResult cr = shell.executeCommand("inputs --f " + FLOW_PATH_BACKSLASH_INPUT);

        verify(compilerHelperMock).compile(contains(FLOW_PATH_BACKSLASH), isNull(List.class));

        Assert.assertEquals("input list mismatch", Lists.newArrayList("input1", "input2"), cr.getResult());
        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testGetVersion() throws Exception {
        CommandResult cr = shell.executeCommand("cslang --version");

        Assert.assertEquals("method result mismatch", slangCLI.version(), cr.getResult());
        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

    @Test(timeout = DEFAULT_TIMEOUT)
    public void testListSystemProperties() throws Exception {
        when(compilerHelperMock.loadSystemProperties(Lists.newArrayList("system_properties.prop.sl")))
                .thenReturn(Sets.newLinkedHashSet(Lists.newArrayList(new SystemProperty("namespace1", "key1", "value1"),
                        new SystemProperty("namespace2", "key2", "value2"),
                        new SystemProperty("namespace3", "key3", "value3"))));

        CommandResult cr = shell.executeCommand("list --f system_properties.prop.sl");

        Assert.assertEquals("Following system properties were loaded:\n" +
                "\tnamespace1.key1: value1\n" +
                "\tnamespace2.key2: value2\n" +
                "\tnamespace3.key3: value3", cr.getResult());
        Assert.assertEquals("method threw exception", null, cr.getException());
        Assert.assertEquals("success should be true", true, cr.isSuccess());
    }

}
