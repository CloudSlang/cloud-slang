package io.cloudslang.lang.compiler.modeller;

import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import io.cloudslang.lang.compiler.modeller.transformers.AggregateTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.PublishTransformer;
import io.cloudslang.lang.compiler.modeller.transformers.Transformer;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import io.cloudslang.lang.entities.bindings.Result;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.compiler.modeller.model.Operation;
import io.cloudslang.lang.compiler.modeller.model.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.*;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
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

    @Autowired
    private TransformersHandler transformersHandler;

    private static final String FILE_NAME = "filename";
    private static final String NAMESPACE = "io.cloudslang";

    @Before
    public void resetMocks() {
        Mockito.reset(transformer);
    }

    private ParsedSlang mockFlowSlangFile() {
        ParsedSlang parsedSlang = Mockito.mock(ParsedSlang.class);
        Mockito.when(parsedSlang.getType()).thenReturn(ParsedSlang.Type.FLOW);
        Mockito.when(parsedSlang.getName()).thenReturn(FILE_NAME);
        Map<String, String> imports = new HashMap<>();
        imports.put("ops", "ops");
        Mockito.when(parsedSlang.getImports()).thenReturn(imports);
        Mockito.when(parsedSlang.getNamespace()).thenReturn(NAMESPACE);

        List<Result> results = new ArrayList<>();
        Map<String, Serializable> postExecutableActionData = new HashMap<>();
        postExecutableActionData.put(SlangTextualKeys.RESULTS_KEY, (Serializable) results);
        Mockito.when(transformersHandler.runTransformers(anyMap(), anyList(), anyList(), anyString())).thenReturn(postExecutableActionData);
        Mockito.when(transformersHandler.checkKeyWords(eq("action data"), anyMap(), anyList(), anyList(), anyList())).thenCallRealMethod();

        return parsedSlang;
    }

    private ParsedSlang mockOperationsSlangFile() {
        ParsedSlang parsedSlang = Mockito.mock(ParsedSlang.class);
        Mockito.when(parsedSlang.getType()).thenReturn(ParsedSlang.Type.OPERATION);
        Mockito.when(parsedSlang.getName()).thenReturn(FILE_NAME);
        return parsedSlang;
    }

    @Test
    public void emptyExecutableDataThrowsException() throws Exception {
        ParsedSlang mockParsedSlang = mockFlowSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();

        String flowName = "flow2";
        exception.expect(RuntimeException.class);
        exception.expectMessage(flowName);

        transformToExecutable(mockParsedSlang, flowName, executableRawData);
    }

    @Test
    public void emptyWorkFlowThrowsException() throws Exception {
        ParsedSlang mockParsedSlang = mockFlowSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();
        executableRawData.put(SlangTextualKeys.WORKFLOW_KEY, new LinkedHashMap<>());

        String flowName = "flow2";
        exception.expect(RuntimeException.class);
        exception.expectMessage(flowName);

        transformToExecutable(mockParsedSlang, flowName, executableRawData);
    }

    @Test
    public void emptyStepThrowsException() throws Exception {
        ParsedSlang mockParsedSlang = mockFlowSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();
        List<Map<String, Object>> workFlowData = new ArrayList<>();
        String stepName = "step1";
        Map<String, Object> step = new HashMap<>();
        step.put(stepName, new HashMap<>());
        workFlowData.add(step);
        executableRawData.put(SlangTextualKeys.WORKFLOW_KEY, workFlowData);

        exception.expect(RuntimeException.class);
        exception.expectMessage(stepName);

        transformToExecutable(mockParsedSlang, "flow1", executableRawData);
    }

    @Test
    public void stepKeyThatHasNoTransformerThrowsException() throws Exception {
        ParsedSlang mockParsedSlang = mockFlowSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();
        LinkedHashMap<Object, Object> workFlowData = new LinkedHashMap<>();
        String stepName = "step1";
        Map<String, Object> stepRawData = new HashMap<>();
        String keyword = "a";
        stepRawData.put(keyword, 'b');
        workFlowData.put(stepName, stepRawData);
        executableRawData.put(SlangTextualKeys.WORKFLOW_KEY, workFlowData);

        exception.expect(RuntimeException.class);
        exception.expectMessage(keyword);

        transformToExecutable(mockParsedSlang, "flow1", executableRawData);
    }

    @Test
    public void stepKeyThatHasTransformerNotInScopeThrowsException() throws Exception {
        String keyword = "a";
        Mockito.when(transformer.keyToTransform()).thenReturn(keyword);
        Mockito.when(transformer.getScopes()).thenReturn(Arrays.asList(Transformer.Scope.ACTION));
        ParsedSlang mockParsedSlang = mockFlowSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();
        LinkedHashMap<Object, Object> workFlowData = new LinkedHashMap<>();
        String stepName = "step1";
        Map<String, Object> stepRawData = new HashMap<>();
        stepRawData.put(keyword, 'b');
        workFlowData.put(stepName, stepRawData);
        executableRawData.put(SlangTextualKeys.WORKFLOW_KEY, workFlowData);

        exception.expect(RuntimeException.class);
        exception.expectMessage(keyword);

        transformToExecutable(mockParsedSlang, "flow1", executableRawData);
    }

    @Test
    public void transformerThatCantCastTheDataThrowsException() throws Exception {
        String keyword = "a";
        Mockito.when(transformer.keyToTransform()).thenReturn(keyword);
        Mockito.when(transformer.getScopes()).thenReturn(Arrays.asList(Transformer.Scope.BEFORE_EXECUTABLE));
        Mockito.when(transformer.transform(any())).thenThrow(ClassCastException.class);
        ParsedSlang mockParsedSlang = mockFlowSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();
        LinkedHashMap<Object, Object> workFlowData = new LinkedHashMap<>();
        String stepName = "step1";
        Map<String, Object> stepRawData = new HashMap<>();
        stepRawData.put(keyword, 'b');
        workFlowData.put(stepName, stepRawData);
        executableRawData.put(SlangTextualKeys.WORKFLOW_KEY, workFlowData);

        exception.expect(RuntimeException.class);
        exception.expectMessage(keyword);

        transformToExecutable(mockParsedSlang, "flow1", executableRawData);
    }

    @Test
    public void stepWithNoDoEntranceThrowsException() throws Exception {
        String keyword = "a";
        Mockito.when(transformer.keyToTransform()).thenReturn(keyword);
        Mockito.when(transformer.getScopes()).thenReturn(Arrays.asList(Transformer.Scope.BEFORE_STEP));
        ParsedSlang mockParsedSlang = mockFlowSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();
        List<Map<String, Object>> workFlowData = new ArrayList<>();
        String stepName = "step1";
        Map<String, Object> stepRawData = new HashMap<>();
        stepRawData.put(keyword, 'b');
        Map<String, Object> step = new HashMap<>();
        step.put(stepName, stepRawData);
        workFlowData.add(step);
        executableRawData.put(SlangTextualKeys.WORKFLOW_KEY, workFlowData);

        exception.expect(RuntimeException.class);
        exception.expectMessage(stepName);

        transformToExecutable(mockParsedSlang, "flow1", executableRawData);
    }

    @Test
    public void stepWithEmptyDoEntranceThrowsException() throws Exception {
        ParsedSlang mockParsedSlang = mockFlowSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();
        List<Map<String, Object>> workFlowData = new ArrayList<>();
        Map<String, Object> stepRawData = new HashMap<>();

        stepRawData.put(SlangTextualKeys.DO_KEY, new HashMap<>());
        String stepName = "step1";
        Map<String, Object> step = new HashMap<>();
        step.put(stepName, stepRawData);
        workFlowData.add(step);
        executableRawData.put(SlangTextualKeys.WORKFLOW_KEY, workFlowData);

        exception.expect(RuntimeException.class);
        exception.expectMessage(stepName);

        transformToExecutable(mockParsedSlang, "flow1", executableRawData);
    }

    @Test
    public void simpleFlowDataIsValid() throws Exception {
        ParsedSlang mockParsedSlang = mockFlowSlangFile();

        Map<String, Object> executableRawData = new HashMap<>();
        List<Map<String, Object>> workFlowData = new ArrayList<>();
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
        Flow flow = (Flow) executableBuilder.transformToExecutable(mockParsedSlang, flowName, executableRawData).getExecutable();
        Assert.assertEquals(SlangTextualKeys.FLOW_TYPE, flow.getType());
        Assert.assertEquals(flowName, flow.getName());
        Deque<Step> steps = flow.getWorkflow().getSteps();
        Assert.assertEquals(1, steps.size());
        Assert.assertEquals(stepName, steps.getFirst().getName());
        Assert.assertEquals(refId, steps.getFirst().getRefId());

    }

    @Test
    public void stepWithImplicitAlias() throws Exception {
        ParsedSlang mockParsedSlang = mockFlowSlangFile();

        Map<String, Object> executableRawData = new HashMap<>();
        List<Map<String, Object>> workFlowData = new ArrayList<>();
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
        Flow flow = (Flow) executableBuilder.transformToExecutable(mockParsedSlang, flowName, executableRawData).getExecutable();

        Assert.assertEquals(SlangTextualKeys.FLOW_TYPE, flow.getType());
        Assert.assertEquals(flowName, flow.getName());
        Deque<Step> steps = flow.getWorkflow().getSteps();
        Assert.assertEquals(1, steps.size());
        Assert.assertEquals(stepName, steps.getFirst().getName());
        Assert.assertEquals(NAMESPACE + "." + refString, steps.getFirst().getRefId());
    }

    @Test
    public void invalidKeyWordsInOperationThrowsException() throws Exception {
        ParsedSlang mockParsedSlang = mockOperationsSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();
        String key = "a";
        executableRawData.put(key, "b");

        String operationName = "op1";
        exception.expect(RuntimeException.class);
        exception.expectMessage(operationName);
        exception.expectMessage(key);

        Operation op = (Operation) transformToExecutable(mockParsedSlang, operationName, executableRawData);
        Assert.assertNotNull(op);
    }

    @Test
    public void operationWithEmptyActionDataThrowException() throws Exception {
        String keyword = "a";
        Mockito.when(transformer.keyToTransform()).thenReturn(keyword);
        Mockito.when(transformer.getScopes()).thenReturn(Arrays.asList(Transformer.Scope.BEFORE_EXECUTABLE));

        ParsedSlang mockParsedSlang = mockOperationsSlangFile();
        Map<String, Object> executableRawData = new HashMap<>();
        executableRawData.put(keyword, "b");

        String operationName = "op1";
        exception.expect(RuntimeException.class);
        exception.expectMessage(operationName);

        transformToExecutable(mockParsedSlang, operationName, executableRawData);
    }

    private Executable transformToExecutable(ParsedSlang mockParsedSlang, String flowName, Map<String, Object> executableRawData) {
        ExecutableModellingResult modellingResult = executableBuilder.transformToExecutable(mockParsedSlang, flowName, executableRawData);
        if (modellingResult.getErrors().size() > 0) {
            throw modellingResult.getErrors().get(0);
        }
        return modellingResult.getExecutable();
    }

    static class Config {
        @Bean
        public ExecutableBuilder executableBuilder() {
            return new ExecutableBuilder();
        }

        @Bean
        public Transformer transformer() {
            return Mockito.mock(Transformer.class);
        }

        @Bean
        public DependenciesHelper dependenciesHelper() {
            return Mockito.mock(DependenciesHelper.class);
        }

        @Bean
        public PublishTransformer publishTransformer() {
            return Mockito.mock(PublishTransformer.class);
        }

        @Bean
        public AggregateTransformer aggregateTransformer() {
            return Mockito.mock(AggregateTransformer.class);
        }

        @Bean
        public TransformersHandler transformersHandler(){
            return Mockito.mock(TransformersHandler.class);
        }
    }
}