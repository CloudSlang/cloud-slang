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
import io.cloudslang.lang.entities.bindings.InOutParam;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;


public abstract class AbstractOutputsTransformer extends InOutTransformer {

    private PreCompileValidator preCompileValidator;

    private ExecutableValidator executableValidator;

    public TransformModellingResult<List<Output>> transform(List<Object> rawData) {
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
                        handleOutputProperties(transformedData, entry, errors);
                    } else {
                        // - some_output: some_expression
                        addOutput(transformedData, createOutput(entry.getKey(), entryValue, false), errors);
                    }
                } else {
                    //- some_output
                    //this is our default behavior that if the user specifies only a key,
                    // the key is also the ref we look for
                    addOutput(transformedData, createRefOutput((String) rawOutput, false), errors);
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
                                         Map.Entry<String, ?> entry, List<RuntimeException> errors);

    void addOutput(List<Output> outputs, Output element, List<RuntimeException> errors) {
        List<RuntimeException> validationErrors = preCompileValidator.validateNoDuplicateInOutParams(outputs, element);
        if (CollectionUtils.isEmpty(validationErrors)) {
            outputs.add(element);
        } else {
            errors.addAll(validationErrors);
        }
    }

    Output createOutput(String outputName, Serializable outputExpression, boolean sensitive) {
        executableValidator.validateOutputName(outputName);
        preCompileValidator.validateStringValue(outputName, outputExpression, this);
        Accumulator accumulator = extractFunctionData(outputExpression);
        return new Output(
                outputName,
                ValueFactory.create(outputExpression, sensitive),
                accumulator.getFunctionDependencies(),
                accumulator.getSystemPropertyDependencies()
        );
    }

    Output createRefOutput(String rawOutput, boolean sensitive) {
        return createOutput(rawOutput, transformNameToExpression(rawOutput), sensitive);
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
}
