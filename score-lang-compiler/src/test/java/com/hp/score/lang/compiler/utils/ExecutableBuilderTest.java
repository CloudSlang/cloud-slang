package com.hp.score.lang.compiler.utils;

import com.hp.score.lang.compiler.SlangTextualKeys;
import com.hp.score.lang.compiler.domain.CompiledFlow;
import com.hp.score.lang.compiler.domain.CompiledOperation;
import com.hp.score.lang.compiler.domain.SlangFile;
import com.hp.score.lang.compiler.transformers.Transformer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ExecutableBuilderTest.Config.class)
public class ExecutableBuilderTest {

    @Autowired
    private ExecutableBuilder executableBuilder;

    @Autowired
    private Transformer transformer;

    @Before
    public void resetMocks(){
        Mockito.reset(transformer);
    }

    @Test
    public void testCompileSimpleFlow() throws Exception {
        String opId = "some_op";
        String opNamespace = "ops";
        TreeMap<String, List<SlangFile>> dependenciesByNamespace = new TreeMap<>();
        SlangFile mockSlangFile = Mockito.mock(SlangFile.class);
        Map<String, Map<String, Object>> refOperationData = new HashMap<>();
        refOperationData.put(opId, new HashMap<String, Object>());
        List<Map<String, Map<String, Object>>> operations = new ArrayList<>();
        operations.add(refOperationData);
        Mockito.when(mockSlangFile.getOperations()).thenReturn(operations);
        Mockito.when(mockSlangFile.getType()).thenReturn(SlangFile.Type.OPERATIONS);
        dependenciesByNamespace.put(opNamespace, Arrays.asList(mockSlangFile));
        Map<String, Object> tasksMap = new LinkedHashMap<>();
        String taskName = "task1";
        Map<String, Object> taskRawData = new HashMap<>();
        Map<String, Object> referenceData = new HashMap<>();
        referenceData.put(opNamespace + "." + opId, new LinkedHashMap<>());
        taskRawData.put(SlangTextualKeys.DO_KEY, referenceData);
        tasksMap.put(taskName, taskRawData);
        Map<String, Object> executableRawData = new HashMap<>();
        executableRawData.put(SlangTextualKeys.WORKFLOW_KEY, tasksMap);
        CompiledFlow op1 = (CompiledFlow) executableBuilder.compileExecutable(
                "flow1", executableRawData, dependenciesByNamespace, SlangFile.Type.FLOW);
        Assert.assertNotNull(op1);
    }

    @Test
    public void testCompileSimpleOperation() throws Exception {
        String action_key = "python_script";
        Mockito.when(transformer.getScopes()).thenReturn(Arrays.asList(Transformer.Scope.ACTION));
        Mockito.when(transformer.keyToTransform()).thenReturn(action_key);
        Map<String, Object> actionRawData = new HashMap<>();
        actionRawData.put(action_key, "some_script");
        Map<String, Object> executableRawData = new HashMap<>();
        executableRawData.put(SlangTextualKeys.ACTION_KEY, actionRawData);
        CompiledOperation op1 = (CompiledOperation) executableBuilder.compileExecutable(
                "op1", executableRawData, new TreeMap<String, List<SlangFile>>(), SlangFile.Type.OPERATIONS);
        Assert.assertNotNull(op1);
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