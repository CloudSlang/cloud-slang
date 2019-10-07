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



/*
 * Created by orius123 on 05/11/14.
 */

import io.cloudslang.lang.compiler.CompilerConstants;
import io.cloudslang.lang.compiler.modeller.result.BasicTransformModellingResult;
import io.cloudslang.lang.compiler.modeller.result.TransformModellingResult;
import io.cloudslang.lang.compiler.validator.ExecutableValidator;
import io.cloudslang.lang.entities.SensitivityLevel;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.cloudslang.lang.compiler.utils.SlangSourceUtils.containsNavigationNextStep;

public class NavigateTransformer implements Transformer<List<Object>, List<Map<String, Serializable>>> {

    private ExecutableValidator executableValidator;
    private Type type = Type.INTERNAL;


    @Override
    public TransformModellingResult<List<Map<String, Serializable>>> transform(List<Object> rawData) {
        return transform(rawData, CompilerConstants.DEFAULT_SENSITIVITY_LEVEL);
    }

    @Override
    public TransformModellingResult<List<Map<String, Serializable>>> transform(List<Object> rawData,
                                                                         SensitivityLevel sensitivityLevel) {
        List<Map<String, Serializable>> transformedData = new ArrayList<>();
        List<RuntimeException> errors = new ArrayList<>();

        if (CollectionUtils.isEmpty(rawData)) {
            return new BasicTransformModellingResult<>(transformedData, errors);
        }

        for (Object elementAsObject : rawData) {
            try {
                if (elementAsObject instanceof Map) {
                    Map elementAsMap = (Map) elementAsObject;
                    if (elementAsMap.size() != 1) {
                        throw new RuntimeException("Each list item in the navigate " +
                                "section should contain exactly one key:value pair.");
                    }
                    // - SUCCESS: some_step
                    Map.Entry navigationEntry = (Map.Entry) elementAsMap.entrySet().iterator().next();
                    Object navigationKey = navigationEntry.getKey();
                    Serializable navigationValue = (Serializable) navigationEntry.getValue();
                    if (!(navigationKey instanceof String)) {
                        throw new RuntimeException("Each key in the navigate section should be a string.");
                    }

                    if (navigationValue instanceof Map) {
                        // - SUCCESS:
                        //     next_step: some_step
                        //     option_1: value_1
                        //     option_2: value_2
                        //     ...
                        if (!containsNavigationNextStep(navigationValue)) {
                            throw new RuntimeException("Expected single 'next_step' option in first position.");
                        }
                    }
                    else {
                        // - SUCCESS: some_step
                        if (!(navigationValue instanceof String)) {
                            throw new RuntimeException("Each value in the navigate section should be a string.");
                        }
                    }
                    @SuppressWarnings("unchecked")
                    Map<String, Serializable> elementAsStringMap = elementAsMap;
                    transformedData.add(elementAsStringMap);
                } else {
                    throw new RuntimeException(
                            "Navigation rule should be a Map. Actual type is " +
                                    elementAsObject.getClass().getName() + ": " + elementAsObject
                    );
                }
            } catch (RuntimeException rex) {
                errors.add(rex);
            }
        }

        try {
            List<Map<String, Serializable>> navigationStrings = (List<Map<String, Serializable>>) (List) rawData;
            if (!navigationStrings.isEmpty() && errors.isEmpty()) {
                executableValidator.validateNavigationStrings(navigationStrings);
            }
        } catch (RuntimeException e) {
            errors.add(e);
        }
        return new BasicTransformModellingResult<>(transformedData, errors);
    }

    @Override
    public List<Scope> getScopes() {
        return Collections.singletonList(Scope.AFTER_STEP);
    }

    @Override
    public String keyToTransform() {
        return null;
    }

    @Override
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public ExecutableValidator getExecutableValidator() {
        return executableValidator;
    }

    public void setExecutableValidator(ExecutableValidator executableValidator) {
        this.executableValidator = executableValidator;
    }
}
