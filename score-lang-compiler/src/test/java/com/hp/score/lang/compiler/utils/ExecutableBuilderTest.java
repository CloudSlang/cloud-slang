package com.hp.score.lang.compiler.utils;

import com.hp.score.lang.compiler.SlangTextualKeys;
import com.hp.score.lang.compiler.domain.CompiledFlow;
import com.hp.score.lang.compiler.domain.CompiledOperation;
import com.hp.score.lang.compiler.domain.CompiledTask;
import com.hp.score.lang.compiler.domain.SlangFile;
import com.hp.score.lang.compiler.transformers.Transformer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Matchers.any;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ExecutableBuilderTest.Config.class)
public class ExecutableBuilderTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    private ExecutableBuilder executableBuilder;

    @Autowired
    private Transformer transformer;
    private static final String FILE_NAME = "filename";

    @Before
    public void resetMocks() {
        Mockito.reset(transformer);
    }

    private SlangFile mockFlowSlangFile() {
        SlangFile slangFile = Mockito.mock(SlangFile.class);
        Mockito.when(slangFile.getType()).thenReturn(SlangFile.Type.FLOW);
        Mockito.when(slangFile.getFileName()).thenReturn(FILE_NAME);
        Map<String, String> imports = new HashMap<>();
        imports.put("ops", "ops");
        Mockito.when(slangFile.getImports()).thenReturn(imports);
        return slangFile;
    }

    private SlangFile mockOperationsSlangFile() {
        SlangFile slangFile = Mockito.mock(SlangFile.class);
        Mockito.when(slangFile.getType()).thenReturn(SlangFile.Type.OPERATIONS);
        Mockito.when(slangFile.getFileName()).thenReturn(FILE_NAME);
        return slangFile;
    }

    @Test
    public void emptyExecutableDataThrowsException() throws Exception {
        SlangFile mockSlangFile = mockFlowSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();

        String flowName = "flow2";
        exception.expect(RuntimeException.class);
        exception.expectMessage(flowName);

        executableBuilder.transformToExecutable(mockSlangFile, flowName, executableRawData);
    }

    @Test
    public void emptyWorkFlowThrowsException() throws Exception {
        SlangFile mockSlangFile = mockFlowSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();
        executableRawData.put(SlangTextualKeys.WORKFLOW_KEY, new LinkedHashMap<>());

        String flowName = "flow2";
        exception.expect(RuntimeException.class);
        exception.expectMessage(flowName);

        executableBuilder.transformToExecutable(mockSlangFile, flowName, executableRawData);
    }

    @Test
    public void emptyTaskThrowsException() throws Exception {
        SlangFile mockSlangFile = mockFlowSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();
        LinkedHashMap<Object, Object> workFlowData = new LinkedHashMap<>();
        String taskName = "task1";
        workFlowData.put(taskName, new HashMap<>());
        executableRawData.put(SlangTextualKeys.WORKFLOW_KEY, workFlowData);

        exception.expect(RuntimeException.class);
        exception.expectMessage(taskName);

        executableBuilder.transformToExecutable(mockSlangFile, "flow1", executableRawData);
    }

    @Test
    public void taskKeyThatHasNoTransformerThrowsException() throws Exception {
        SlangFile mockSlangFile = mockFlowSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();
        LinkedHashMap<Object, Object> workFlowData = new LinkedHashMap<>();
        String taskName = "task1";
        Map<String, Object> taskRawData = new HashMap<>();
        String keyword = "a";
        taskRawData.put(keyword, 'b');
        workFlowData.put(taskName, taskRawData);
        executableRawData.put(SlangTextualKeys.WORKFLOW_KEY, workFlowData);

        exception.expect(RuntimeException.class);
        exception.expectMessage(keyword);

        executableBuilder.transformToExecutable(mockSlangFile, "flow1", executableRawData);
    }

    @Test
    public void taskKeyThatHasTransformerNotInScopeThrowsException() throws Exception {
        String keyword = "a";
        Mockito.when(transformer.keyToTransform()).thenReturn(keyword);
        Mockito.when(transformer.getScopes()).thenReturn(Arrays.asList(Transformer.Scope.ACTION));
        SlangFile mockSlangFile = mockFlowSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();
        LinkedHashMap<Object, Object> workFlowData = new LinkedHashMap<>();
        String taskName = "task1";
        Map<String, Object> taskRawData = new HashMap<>();
        taskRawData.put(keyword, 'b');
        workFlowData.put(taskName, taskRawData);
        executableRawData.put(SlangTextualKeys.WORKFLOW_KEY, workFlowData);

        exception.expect(RuntimeException.class);
        exception.expectMessage(keyword);

        executableBuilder.transformToExecutable(mockSlangFile, "flow1", executableRawData);
    }

    @Test
    public void transformerThatCantCastTheDataThrowsException() throws Exception {
        String keyword = "a";
        Mockito.when(transformer.keyToTransform()).thenReturn(keyword);
        Mockito.when(transformer.getScopes()).thenReturn(Arrays.asList(Transformer.Scope.BEFORE_EXECUTABLE));
        Mockito.when(transformer.transform(any())).thenThrow(ClassCastException.class);
        SlangFile mockSlangFile = mockFlowSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();
        LinkedHashMap<Object, Object> workFlowData = new LinkedHashMap<>();
        String taskName = "task1";
        Map<String, Object> taskRawData = new HashMap<>();
        taskRawData.put(keyword, 'b');
        workFlowData.put(taskName, taskRawData);
        executableRawData.put(SlangTextualKeys.WORKFLOW_KEY, workFlowData);

        exception.expect(RuntimeException.class);
        exception.expectMessage(keyword);

        executableBuilder.transformToExecutable(mockSlangFile, "flow1", executableRawData);
    }

    @Test
    public void taskWithNoDoEntranceThrowsException() throws Exception {
        String keyword = "a";
        Mockito.when(transformer.keyToTransform()).thenReturn(keyword);
        Mockito.when(transformer.getScopes()).thenReturn(Arrays.asList(Transformer.Scope.BEFORE_TASK));
        SlangFile mockSlangFile = mockFlowSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();
        LinkedHashMap<Object, Object> workFlowData = new LinkedHashMap<>();
        String taskName = "task1";
        Map<String, Object> taskRawData = new HashMap<>();
        taskRawData.put(keyword, 'b');
        workFlowData.put(taskName, taskRawData);
        executableRawData.put(SlangTextualKeys.WORKFLOW_KEY, workFlowData);

        exception.expect(RuntimeException.class);
        exception.expectMessage(taskName);

        executableBuilder.transformToExecutable(mockSlangFile, "flow1", executableRawData);
    }

    @Test
    public void taskWithEmptyDoEntranceThrowsException() throws Exception {
        SlangFile mockSlangFile = mockFlowSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();
        LinkedHashMap<Object, Object> workFlowData = new LinkedHashMap<>();
        Map<String, Object> taskRawData = new HashMap<>();

        taskRawData.put(SlangTextualKeys.DO_KEY, new HashMap<>());
        String taskName = "task1";
        workFlowData.put(taskName, taskRawData);
        executableRawData.put(SlangTextualKeys.WORKFLOW_KEY, workFlowData);

        exception.expect(RuntimeException.class);
        exception.expectMessage(taskName);

        executableBuilder.transformToExecutable(mockSlangFile, "flow1", executableRawData);
    }

    @Test
    public void simpleFlowDataIsValid() throws Exception {
        SlangFile mockSlangFile = mockFlowSlangFile();

        Map<String, Object> executableRawData = new HashMap<>();
        LinkedHashMap<Object, Object> workFlowData = new LinkedHashMap<>();
        Map<String, Object> taskRawData = new HashMap<>();
        Map<String, Object> doRawData = new HashMap<>();

        String refId = "ops.print";
        doRawData.put(refId, new HashMap<>());
        taskRawData.put(SlangTextualKeys.DO_KEY, doRawData);
        String taskName = "task1";
        workFlowData.put(taskName, taskRawData);
        executableRawData.put(SlangTextualKeys.WORKFLOW_KEY, workFlowData);

        String flowName = "flow1";
        CompiledFlow flow = (CompiledFlow) executableBuilder.transformToExecutable(mockSlangFile, flowName, executableRawData);
        Assert.assertEquals(SlangTextualKeys.FLOW_TYPE, flow.getType());
        Assert.assertEquals(flowName, flow.getName());
        Deque<CompiledTask> compiledTasks = flow.getCompiledWorkflow().getCompiledTasks();
        Assert.assertEquals(1, compiledTasks.size());
        Assert.assertEquals(taskName, compiledTasks.getFirst().getName());
        Assert.assertEquals(refId, compiledTasks.getFirst().getRefId());

    }

    @Test
    public void invalidKeyWordsInOperationThrowsException() throws Exception {
        SlangFile mockSlangFile = mockOperationsSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();
        String key = "a";
        executableRawData.put(key, "b");

        String operationName = "op1";
        exception.expect(RuntimeException.class);
        exception.expectMessage(operationName);
        exception.expectMessage(key);

        CompiledOperation op = (CompiledOperation) executableBuilder.transformToExecutable(mockSlangFile, operationName, executableRawData);
        Assert.assertNotNull(op);
    }

    @Test
    public void operationWithEmptyActionDataThrowException() throws Exception {
        String keyword = "a";
        Mockito.when(transformer.keyToTransform()).thenReturn(keyword);
        Mockito.when(transformer.getScopes()).thenReturn(Arrays.asList(Transformer.Scope.BEFORE_EXECUTABLE));

        SlangFile mockSlangFile = mockOperationsSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();
        executableRawData.put(keyword, "b");

        String operationName = "op1";
        exception.expect(RuntimeException.class);
        exception.expectMessage(operationName);

        executableBuilder.transformToExecutable(mockSlangFile, operationName, executableRawData);
    }

    @Test
    public void invalidKeysInActionThrowsException() throws Exception {
        SlangFile mockSlangFile = mockOperationsSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();
        Map<String, Object> actionRawData = new HashMap<>();


        String invalidKey = "a";
        actionRawData.put(invalidKey, "b");
        executableRawData.put(SlangTextualKeys.ACTION_KEY, actionRawData);
        exception.expect(RuntimeException.class);
        exception.expectMessage(invalidKey);

        executableBuilder.transformToExecutable(mockSlangFile, "op1", executableRawData);
    }

    @Ignore("problem with the post construct no taking the mocks")
    @Test
    public void simpleOp() throws Exception {
        String keyword = "a";
        Mockito.when(transformer.keyToTransform()).thenReturn(keyword);
        Mockito.when(transformer.getScopes()).thenReturn(Arrays.asList(Transformer.Scope.ACTION));

        SlangFile mockSlangFile = mockOperationsSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();
        Map<String, Object> actionRawData = new HashMap<>();

        actionRawData.put(keyword, "b");
        executableRawData.put(SlangTextualKeys.ACTION_KEY, actionRawData);

        String operationName = "op1";
        CompiledOperation op = (CompiledOperation) executableBuilder.transformToExecutable(mockSlangFile, operationName, executableRawData);
        Assert.assertNotNull(op);
        Assert.assertEquals(operationName, op.getName());
        Assert.assertNotNull(operationName, op.getCompiledDoAction().getActionData());
    }

    @Configuration
    static class Config {
        @Bean
        public ExecutableBuilder executableBuilder() {
            return new ExecutableBuilder();
        }

        @Bean
        public Transformer transformer() {
            return Mockito.mock(Transformer.class);
        }
    }
}