/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller.transformers;


import io.cloudslang.lang.compiler.modeller.result.BasicTransformModellingResult;
import io.cloudslang.lang.compiler.modeller.result.TransformModellingResult;
import io.cloudslang.lang.compiler.validator.ExecutableValidator;
import io.cloudslang.lang.compiler.validator.PreCompileValidator;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.entities.SensitivityLevel;
import io.cloudslang.lang.entities.bindings.InOutParam;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;

import static io.cloudslang.lang.compiler.SlangTextualKeys.SENSITIVE_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.VALUE_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SEQ_OUTPUT_ROBOT_KEY;
import static java.lang.String.format;


public abstract class AbstractOutputsTransformer extends InOutTransformer {

    private PreCompileValidator preCompileValidator;

    private ExecutableValidator executableValidator;

    private static final List<String> KNOWN_KEYS = Arrays.asList(SENSITIVE_KEY, VALUE_KEY);

    public TransformModellingResult<List<Output>> transform(List<Object> rawData, SensitivityLevel sensitivityLevel) {
        List<Output> transformedData = new ArrayList<>();
        List<RuntimeException> errors = new ArrayList<>();

        if (CollectionUtils.isEmpty(rawData)) {
            return new BasicTransformModellingResult<>(transformedData, errors);
        }

        for (Object rawOutput : rawData) {
            try {
                if (rawOutput instanceof Map) {

                    @SuppressWarnings("unchecked")
                    Map.Entry<String, ?> entry = ((Map<String, ?>) rawOutput).entrySet().iterator().next();
                    Serializable entryValue = (Serializable) entry.getValue();
                    if (entryValue == null) {
                        throw new RuntimeException("Could not transform Output : " + rawOutput +
                                " since it has a null value.\n\n" +
                                "Make sure a value is specified or that indentation is properly done.");
                    }
                    if (entryValue instanceof Map) {
                        // - some_output:
                        //     property1: value1
                        //     property2: value2
                        // this is the verbose way of defining outputs with all of the properties available
                        handleOutputProperties(transformedData, entry, errors, sensitivityLevel);
                    } else {
                        // - some_output: some_expression
                        addOutput(transformedData, createOutput(entry.getKey(), entryValue, false,
                                sensitivityLevel), errors);
                    }
                } else {
                    //- some_output
                    //this is our default behavior that if the user specifies only a key,
                    // the key is also the ref we look for
                    addOutput(transformedData, createRefOutput((String) rawOutput, false, sensitivityLevel), errors);
                }
            } catch (RuntimeException rex) {
                errors.add(rex);
            }
        }
        return new BasicTransformModellingResult<>(transformedData, errors);
    }

    @Override
    public Class<? extends InOutParam> getTransformedObjectsClass() {
        return Output.class;
    }

    abstract void handleOutputProperties(List<Output> transformedData,
                                         Map.Entry<String, ?> entry, List<RuntimeException> errors,
                                         SensitivityLevel sensitivityLevel);

    void addOutput(List<Output> outputs, Output element, List<RuntimeException> errors) {
        List<RuntimeException> validationErrors = preCompileValidator.validateNoDuplicateInOutParams(outputs, element);
        if (CollectionUtils.isEmpty(validationErrors)) {
            outputs.add(element);
        } else {
            errors.addAll(validationErrors);
        }
    }

    Output createOutput(String outputName, Serializable outputExpression, boolean sensitive,
                        SensitivityLevel sensitivityLevel) {
        executableValidator.validateOutputName(outputName);
        preCompileValidator.validateStringValue(outputName, outputExpression, this);
        Accumulator accumulator = extractFunctionData(outputExpression);
        return new Output(
                outputName,
                ValueFactory.create(outputExpression, sensitive, sensitivityLevel),
                accumulator.getFunctionDependencies(),
                accumulator.getSystemPropertyDependencies()
        );
    }

    Output createRefOutput(String rawOutput, boolean sensitive, SensitivityLevel sensitivityLevel) {
        return createOutput(rawOutput, transformNameToExpression(rawOutput), sensitive, sensitivityLevel);
    }

    private String transformNameToExpression(String name) {
        return ScoreLangConstants.EXPRESSION_START_DELIMITER + name + ScoreLangConstants.EXPRESSION_END_DELIMITER;
    }

    public void setPreCompileValidator(PreCompileValidator preCompileValidator) {
        this.preCompileValidator = preCompileValidator;
    }

    public void setExecutableValidator(ExecutableValidator executableValidator) {
        this.executableValidator = executableValidator;
    }

    protected Output createPropOutput(Map.Entry<String, Map<String, Serializable>> entry,
                                      SensitivityLevel sensitivityLevel) {
        Map<String, Serializable> props = entry.getValue();
        validateKeys(entry, props);
        // default is sensitive=false
        String outputName = entry.getKey();
        boolean sensitive = props.containsKey(SENSITIVE_KEY) && (boolean) props.get(SENSITIVE_KEY);
        Serializable value = props.get(VALUE_KEY);

        Output output = value == null ?
                createRefOutput(outputName, sensitive, sensitivityLevel) :
                createOutput(outputName, value, sensitive, sensitivityLevel);

        if (props.containsKey(SEQ_OUTPUT_ROBOT_KEY)) {
            output.setRobot((boolean) props.get(SEQ_OUTPUT_ROBOT_KEY));
        }

        return output;
    }

    protected List<String> getKnownKeys() {
        return KNOWN_KEYS;
    }

    private void validateKeys(Map.Entry<String, Map<String, Serializable>> entry, Map<String, Serializable> props) {
        for (String key : props.keySet()) {
            if (!getKnownKeys().contains(key)) {
                throw new RuntimeException(format("Key: %s in output: %s is not a known property",
                        key, entry.getKey()));
            }
        }
    }
}
