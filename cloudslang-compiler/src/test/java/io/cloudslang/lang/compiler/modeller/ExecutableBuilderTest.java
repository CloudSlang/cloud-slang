/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller;

import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.compiler.modeller.model.Operation;
import io.cloudslang.lang.compiler.modeller.model.Step;
import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import io.cloudslang.lang.compiler.modeller.transformers.PublishTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.ResultsTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.Transformer;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import io.cloudslang.lang.compiler.validator.ExecutableValidator;
import io.cloudslang.lang.compiler.validator.ExecutableValidatorImpl;
import io.cloudslang.lang.compiler.validator.PreCompileValidator;
import io.cloudslang.lang.compiler.validator.PreCompileValidatorImpl;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidator;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidatorImpl;
import io.cloudslang.lang.entities.bindings.Result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ExecutableBuilderTest.Config.class})
public class ExecutableBuilderTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    private ExecutableBuilder executableBuilder;

    @Autowired
    private Transformer transformer;

    @Autowired
    private TransformersHandler transformersHandler;

    @Autowired
    private PreCompileValidator preCompileValidator;

    private static final String FILE_NAME = "filename";
    private static final String NAMESPACE = "io.cloudslang";

    @Before
    public void resetMocks() {
        Mockito.reset(transformer);
    }

    private ParsedSlang mockFlowSlangFile() {
        ParsedSlang parsedSlang = mock(ParsedSlang.class);
        when(parsedSlang.getType()).thenReturn(ParsedSlang.Type.FLOW);
        when(parsedSlang.getName()).thenReturn(FILE_NAME);
        Map<String, String> imports = new HashMap<>();
        imports.put("ops", "ops");
        when(parsedSlang.getImports()).thenReturn(imports);
        when(parsedSlang.getNamespace()).thenReturn(NAMESPACE);

        List<Result> results = new ArrayList<>();
        Map<String, Serializable> postExecutableActionData = new HashMap<>();
        postExecutableActionData.put(SlangTextualKeys.RESULTS_KEY, (Serializable) results);
        when(transformersHandler.runTransformers(anyMap(), anyList(), anyList(), anyString()))
                .thenReturn(postExecutableActionData);

        return parsedSlang;
    }

    private ParsedSlang mockOperationsSlangFile() {
        ParsedSlang parsedSlang = mock(ParsedSlang.class);
        when(parsedSlang.getType()).thenReturn(ParsedSlang.Type.OPERATION);
        when(parsedSlang.getName()).thenReturn(FILE_NAME);
        return parsedSlang;
    }

    @Test
    public void emptyExecutableDataThrowsException() throws Exception {
        final ParsedSlang mockParsedSlang = mockFlowSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();
        String flowName = "flow2";
        executableRawData.put(SlangTextualKeys.EXECUTABLE_NAME_KEY, flowName);

        exception.expect(RuntimeException.class);
        exception.expectMessage(flowName);

        transformToExecutable(mockParsedSlang, executableRawData);
    }

    @Test
    public void emptyWorkFlowThrowsException() throws Exception {
        final ParsedSlang mockParsedSlang = mockFlowSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();
        executableRawData.put(SlangTextualKeys.WORKFLOW_KEY, new LinkedHashMap<>());
        String flowName = "flow2";
        executableRawData.put(SlangTextualKeys.EXECUTABLE_NAME_KEY, flowName);

        exception.expect(RuntimeException.class);
        exception.expectMessage(flowName);

        transformToExecutable(mockParsedSlang, executableRawData);
    }

    @Test
    public void emptyStepThrowsException() throws Exception {
        final ParsedSlang mockParsedSlang = mockFlowSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();
        List<Map<String, Object>> workFlowData = new ArrayList<>();
        String stepName = "step1";
        Map<String, Object> step = new HashMap<>();
        step.put(stepName, new HashMap<>());
        workFlowData.add(step);
        executableRawData.put(SlangTextualKeys.WORKFLOW_KEY, workFlowData);
        executableRawData.put(SlangTextualKeys.EXECUTABLE_NAME_KEY, "flow1");

        exception.expect(RuntimeException.class);
        exception.expectMessage(stepName);

        transformToExecutable(mockParsedSlang, executableRawData);
    }

    @Test
    public void stepKeyThatHasNoTransformerThrowsException() throws Exception {
        final ParsedSlang mockParsedSlang = mockFlowSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();
        LinkedHashMap<Object, Object> workFlowData = new LinkedHashMap<>();
        String stepName = "step1";
        Map<String, Object> stepRawData = new HashMap<>();
        String keyword = "a";
        stepRawData.put(keyword, 'b');
        workFlowData.put(stepName, stepRawData);
        executableRawData.put(SlangTextualKeys.WORKFLOW_KEY, workFlowData);
        executableRawData.put(SlangTextualKeys.EXECUTABLE_NAME_KEY, "flow1");

        exception.expect(RuntimeException.class);
        exception.expectMessage(keyword);

        transformToExecutable(mockParsedSlang, executableRawData);
    }

    @Test
    public void stepKeyThatHasTransformerNotInScopeThrowsException() throws Exception {
        String keyword = "a";
        when(transformer.keyToTransform()).thenReturn(keyword);
        when(transformer.getScopes()).thenReturn(Collections.singletonList(Transformer.Scope.ACTION));
        final ParsedSlang mockParsedSlang = mockFlowSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();
        LinkedHashMap<Object, Object> workFlowData = new LinkedHashMap<>();
        String stepName = "step1";
        Map<String, Object> stepRawData = new HashMap<>();
        stepRawData.put(keyword, 'b');
        workFlowData.put(stepName, stepRawData);
        executableRawData.put(SlangTextualKeys.WORKFLOW_KEY, workFlowData);
        executableRawData.put(SlangTextualKeys.EXECUTABLE_NAME_KEY, "flow1");

        exception.expect(RuntimeException.class);
        exception.expectMessage(keyword);

        transformToExecutable(mockParsedSlang, executableRawData);
    }

    @Test
    public void transformerThatCantCastTheDataThrowsException() throws Exception {
        String keyword = "a";
        when(transformer.keyToTransform()).thenReturn(keyword);
        when(transformer.getScopes())
                .thenReturn(Collections.singletonList(Transformer.Scope.BEFORE_EXECUTABLE));
        when(transformer.transform(any())).thenThrow(ClassCastException.class);
        final ParsedSlang mockParsedSlang = mockFlowSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();
        LinkedHashMap<Object, Object> workFlowData = new LinkedHashMap<>();
        String stepName = "step1";
        Map<String, Object> stepRawData = new HashMap<>();
        stepRawData.put(keyword, 'b');
        workFlowData.put(stepName, stepRawData);
        executableRawData.put(SlangTextualKeys.WORKFLOW_KEY, workFlowData);
        executableRawData.put(SlangTextualKeys.EXECUTABLE_NAME_KEY, "flow1");

        exception.expect(RuntimeException.class);
        exception.expectMessage(keyword);

        transformToExecutable(mockParsedSlang, executableRawData);
    }

    @Test
    public void stepWithNoDoEntranceThrowsException() throws Exception {
        String keyword = "a";
        when(transformer.keyToTransform()).thenReturn(keyword);
        when(transformer.getScopes()).thenReturn(Collections.singletonList(Transformer.Scope.BEFORE_STEP));
        final ParsedSlang mockParsedSlang = mockFlowSlangFile();
        final Map<String, Object> executableRawData = new HashMap<>();
        List<Map<String, Object>> workFlowData = new ArrayList<>();
        String stepName = "step1";
        Map<String, Object> stepRawData = new HashMap<>();
        stepRawData.put(keyword, 'b');
        Map<String, Object> step = new HashMap<>();
        step.put(stepName, stepRawData);
        workFlowData.add(step);
        executableRawData.put(SlangTextualKeys.WORKFLOW_KEY, workFlowData);
        executableRawData.put(SlangTextualKeys.EXECUTABLE_NAME_KEY, "flow1");

        exception.expect(RuntimeException.class);
        exception.expectMessage(stepName);

        transformToExecutable(mockParsedSlang, executableRawData);
    }

    @Test
    public void stepWithEmptyDoEntranceThrowsException() throws Exception {
        final ParsedSlang mockParsedSlang = mockFlowSlangFile();
        final Map<String, Object> executableRawData = new HashMap<>();
        List<Map<String, Object>> workFlowData = new ArrayList<>();
        Map<String, Object> stepRawData = new HashMap<>();

        stepRawData.put(SlangTextualKeys.DO_KEY, new HashMap<>());
        String stepName = "step1";
        Map<String, Object> step = new HashMap<>();
        step.put(stepName, stepRawData);
        workFlowData.add(step);
        executableRawData.put(SlangTextualKeys.WORKFLOW_KEY, workFlowData);
        executableRawData.put(SlangTextualKeys.EXECUTABLE_NAME_KEY, "flow1");

        exception.expect(RuntimeException.class);
        exception.expectMessage(stepName);

        transformToExecutable(mockParsedSlang, executableRawData);
    }

    @Test
    public void simpleFlowDataIsValid() throws Exception {
        final ParsedSlang mockParsedSlang = mockFlowSlangFile();

        final Map<String, Object> executableRawData = new HashMap<>();
        final List<Map<String, Object>> workFlowData = new ArrayList<>();
        Map<String, Object> stepRawData = new HashMap<>();
        Map<String, Object> doRawData = new HashMap<>();

        String refId = "ops.print";
        doRawData.put(refId, new HashMap<>());
        stepRawData.put(SlangTextualKeys.DO_KEY, doRawData);
        String stepName = "step1";
        Map<String, Object> step = new HashMap<>();
        step.put(stepName, stepRawData);
        workFlowData.add(step);
        executableRawData.put(SlangTextualKeys.WORKFLOW_KEY, workFlowData);
        String flowName = "flow1";
        executableRawData.put(SlangTextualKeys.EXECUTABLE_NAME_KEY, flowName);

        Flow flow = (Flow) executableBuilder.transformToExecutable(mockParsedSlang, executableRawData).getExecutable();
        Assert.assertEquals(SlangTextualKeys.FLOW_TYPE, flow.getType());
        Assert.assertEquals(flowName, flow.getName());
        Deque<Step> steps = flow.getWorkflow().getSteps();
        Assert.assertEquals(1, steps.size());
        Assert.assertEquals(stepName, steps.getFirst().getName());
        Assert.assertEquals(refId, steps.getFirst().getRefId());

    }

    @Test
    public void stepWithImplicitAlias() throws Exception {
        final ParsedSlang mockParsedSlang = mockFlowSlangFile();

        final Map<String, Object> executableRawData = new HashMap<>();
        final List<Map<String, Object>> workFlowData = new ArrayList<>();
        Map<String, Object> stepRawData = new HashMap<>();
        Map<String, Object> doRawData = new HashMap<>();

        String refString = "print";
        doRawData.put(refString, new HashMap<>());
        stepRawData.put(SlangTextualKeys.DO_KEY, doRawData);
        String stepName = "step1";
        Map<String, Object> step = new HashMap<>();
        step.put(stepName, stepRawData);
        workFlowData.add(step);
        executableRawData.put(SlangTextualKeys.WORKFLOW_KEY, workFlowData);
        String flowName = "flow1";
        executableRawData.put(SlangTextualKeys.EXECUTABLE_NAME_KEY, flowName);

        Flow flow = (Flow) executableBuilder.transformToExecutable(mockParsedSlang, executableRawData).getExecutable();

        Assert.assertEquals(SlangTextualKeys.FLOW_TYPE, flow.getType());
        Assert.assertEquals(flowName, flow.getName());
        Deque<Step> steps = flow.getWorkflow().getSteps();
        Assert.assertEquals(1, steps.size());
        Assert.assertEquals(stepName, steps.getFirst().getName());
        Assert.assertEquals(NAMESPACE + "." + refString, steps.getFirst().getRefId());
    }

    @Test
    public void invalidKeyWordsInOperationThrowsException() throws Exception {
        final ParsedSlang mockParsedSlang = mockOperationsSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();
        String key = "a";
        executableRawData.put(key, "b");
        String operationName = "op1";
        executableRawData.put(SlangTextualKeys.EXECUTABLE_NAME_KEY, operationName);

        exception.expect(RuntimeException.class);
        exception.expectMessage(operationName);
        exception.expectMessage(key);

        Operation op = (Operation) transformToExecutable(mockParsedSlang, executableRawData);
        Assert.assertNotNull(op);
    }

    @Test
    public void operationWithEmptyActionDataThrowException() throws Exception {
        String keyword = "a";
        when(transformer.keyToTransform()).thenReturn(keyword);
        when(transformer.getScopes())
                .thenReturn(Collections.singletonList(Transformer.Scope.BEFORE_EXECUTABLE));

        final ParsedSlang mockParsedSlang = mockOperationsSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();
        executableRawData.put(keyword, "b");
        String operationName = "op1";
        executableRawData.put(SlangTextualKeys.EXECUTABLE_NAME_KEY, operationName);

        exception.expect(RuntimeException.class);
        exception.expectMessage(operationName);

        transformToExecutable(mockParsedSlang, executableRawData);
    }

    private Executable transformToExecutable(ParsedSlang mockParsedSlang, Map<String, Object> executableRawData) {
        ExecutableModellingResult modellingResult =
                executableBuilder.transformToExecutable(mockParsedSlang, executableRawData);
        if (modellingResult.getErrors().size() > 0) {
            throw modellingResult.getErrors().get(0);
        }
        return modellingResult.getExecutable();
    }

    static class Config {
        @Bean
        public ExecutableBuilder executableBuilder() {

            ExecutableBuilder executableBuilder = new ExecutableBuilder();
            executableBuilder.setTransformersHandler(transformersHandler());
            executableBuilder.setDependenciesHelper(dependenciesHelper());
            executableBuilder.setPreCompileValidator(preCompileValidator());
            executableBuilder.setResultsTransformer(resultsTransformer());
            executableBuilder.setExecutableValidator(executableValidator());

            executableBuilder.initScopedTransformersAndKeys();
            return executableBuilder;
        }

        @Bean
        public Transformer transformer() {
            return mock(Transformer.class);
        }

        @Bean
        public DependenciesHelper dependenciesHelper() {
            return mock(DependenciesHelper.class);
        }

        @Bean
        public PublishTransformer publishTransformer() {
            return mock(PublishTransformer.class);
        }

        @Bean
        public TransformersHandler transformersHandler() {
            return mock(TransformersHandler.class);
        }

        @Bean
        public PreCompileValidator preCompileValidator() {
            PreCompileValidatorImpl preCompileValidator = new PreCompileValidatorImpl();

            preCompileValidator.setExecutableValidator(executableValidator());
            return preCompileValidator;
        }

        @Bean
        public ResultsTransformer resultsTransformer() {
            return mock(ResultsTransformer.class);
        }

        @Bean
        public ExecutableValidator executableValidator() {
            return new ExecutableValidatorImpl();
        }

        @Bean
        public SystemPropertyValidator systemPropertyValidator() {
            return new SystemPropertyValidatorImpl();
        }
    }
}