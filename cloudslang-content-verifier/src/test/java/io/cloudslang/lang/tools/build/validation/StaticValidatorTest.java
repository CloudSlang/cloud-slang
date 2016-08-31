package io.cloudslang.lang.tools.build.validation;

import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.compiler.modeller.model.Metadata;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.lang.entities.bindings.ScriptFunction;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by bancl on 8/31/2016.
 */
public class StaticValidatorTest {

    private static final Set<String> SYSTEM_PROPERTY_DEPENDENCIES = Collections.emptySet();

    private StaticValidator staticValidator;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() {
        staticValidator = new StaticValidatorImpl();
    }

    @Test
    public void missingDescriptionForInput() throws URISyntaxException {
        List<Input> inputList = new ArrayList<>();
        inputList.add(new Input.InputBuilder("input1", "value1").build());
        inputList.add(new Input.InputBuilder("input2", "value2").build());
        inputList.add(new Input.InputBuilder("input3", "value3").build());
        Flow newExecutable = new Flow(null, null, null, "no_dependencies", "empty_flow", inputList, null, null, new HashSet<String>(), SYSTEM_PROPERTY_DEPENDENCIES);
        Metadata metadata = new Metadata();
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("input1", "description1");
        inputMap.put("input2", "description2");
        metadata.setInputs(inputMap);

        exception.expect(RuntimeException.class);
        exception.expectMessage("Input 'input3' is missing description.");
        staticValidator.validateSlangFile(new File(getClass().getResource("/no_dependencies/empty_flow.sl").toURI()), newExecutable, metadata);
    }

    @Test
    public void missingDescriptionForOutput() throws URISyntaxException {
        List<Output> outputList = new ArrayList<>();
        outputList.add(new Output("output1", ValueFactory.create("value1"), Collections.<ScriptFunction>emptySet(), Collections.<String>emptySet()));
        outputList.add(new Output("output2", ValueFactory.create("value2"), Collections.<ScriptFunction>emptySet(), Collections.<String>emptySet()));
        outputList.add(new Output("output3", ValueFactory.create("value3"), Collections.<ScriptFunction>emptySet(), Collections.<String>emptySet()));
        Flow newExecutable = new Flow(null, null, null, "no_dependencies", "empty_flow", null, outputList, null,
                new HashSet<String>(), SYSTEM_PROPERTY_DEPENDENCIES);
        Metadata metadata = new Metadata();
        Map<String, String> outputMap = new HashMap<>();
        outputMap.put("output1", "description1");
        outputMap.put("output2", "description2");
        metadata.setOutputs(outputMap);

        exception.expect(RuntimeException.class);
        exception.expectMessage("Output 'output3' is missing description.");
        staticValidator.validateSlangFile(new File(getClass().getResource("/no_dependencies/empty_flow.sl").toURI()),
                newExecutable, metadata);
    }

    @Test
    public void missingDescriptionForResult() throws URISyntaxException {
        List<Result> resultList = new ArrayList<>();
        resultList.add(new Result("result1", ValueFactory.create("value1")));
        resultList.add(new Result("result2", ValueFactory.create("value2")));
        resultList.add(new Result("result3", ValueFactory.create("value3")));
        Flow newExecutable = new Flow(null, null, null, "no_dependencies", "empty_flow", null, null, resultList,
                new HashSet<String>(), SYSTEM_PROPERTY_DEPENDENCIES);
        Metadata metadata = new Metadata();
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("result1", "description1");
        inputMap.put("result2", "description2");
        metadata.setResults(inputMap);

        exception.expect(RuntimeException.class);
        exception.expectMessage("Result 'result3' is missing description.");
        staticValidator.validateSlangFile(new File(getClass().getResource("/no_dependencies/empty_flow.sl").toURI()),
                newExecutable, metadata);
    }

    @Test
    public void missingDescriptionEntirelyForResult() throws URISyntaxException {
        List<Result> resultList = new ArrayList<>();
        resultList.add(new Result("result1", ValueFactory.create("value1")));
        resultList.add(new Result("result2", ValueFactory.create("value2")));
        resultList.add(new Result("result3", ValueFactory.create("value3")));
        Flow newExecutable = new Flow(null, null, null, "no_dependencies", "empty_flow", null, null, resultList,
                new HashSet<String>(), SYSTEM_PROPERTY_DEPENDENCIES);
        Metadata metadata = new Metadata();

        exception.expect(RuntimeException.class);
        exception.expectMessage("Results are missing description entirely.");
        staticValidator.validateSlangFile(new File(getClass().getResource("/no_dependencies/empty_flow.sl").toURI()),
                newExecutable, metadata);
    }

}
