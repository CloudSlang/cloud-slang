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

import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.entities.RpaStep;
import io.cloudslang.lang.compiler.modeller.result.BasicTransformModellingResult;
import io.cloudslang.lang.compiler.modeller.result.TransformModellingResult;
import io.cloudslang.lang.entities.SensitivityLevel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static com.google.common.collect.Sets.newHashSet;
import static io.cloudslang.lang.compiler.CompilerConstants.DEFAULT_SENSITIVITY_LEVEL;
import static io.cloudslang.lang.entities.ScoreLangConstants.RPA_ASSIGNMENT_ACTION;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.isEmpty;

public class RpaStepsTransformer extends AbstractTransformer
        implements Transformer<List<Map<String, Map<String, String>>>, ArrayList<RpaStep>> {
    private static final String RPA_OPERATION_HAS_MISSING_TAGS = "Rpa operation step has the following missing tags:";
    private static final Pattern OUTPUT_ASSIGNMENT = Pattern.compile("Parameter\\(\"[^\"]+\"\\)");
    private static final Set<String> MANDATORY_KEY_SET = newHashSet(SlangTextualKeys.RPA_STEP_ID_KEY,
            SlangTextualKeys.RPA_STEP_PATH_KEY, SlangTextualKeys.RPA_STEP_ACTION_KEY);
    private static final Set<String> OPTIONAL_KEY_SET = newHashSet(SlangTextualKeys.RPA_STEP_ARGS_KEY,
            SlangTextualKeys.RPA_STEP_HIGHLIGHT_ID_KEY, SlangTextualKeys.RPA_STEP_SNAPSHOT_KEY);
    private static final String FOUND_DUPLICATE_STEP_WITH_ID =
            "Found duplicate step with id '%s' for rpa operation step.";
    private static final String INVALID_ASSIGNMENT_OPERATION =
            "Found invalid assignment operation for rpa operation step with id '%s'.";
    private static final String RPA_OPERATION_HAS_EMPTY_TAGS =
            "Rpa operation step has the following empty tags: ";
    private static final String RPA_OPERATION_ILLEGAL_TAGS =
            "Rpa operation step has the following illegal tags: ";

    @Override
    public TransformModellingResult<ArrayList<RpaStep>> transform(List<Map<String, Map<String, String>>> rawData) {
        return transform(rawData, DEFAULT_SENSITIVITY_LEVEL);
    }

    @Override
    public TransformModellingResult<ArrayList<RpaStep>> transform(List<Map<String, Map<String, String>>> rawData,
                                                                  SensitivityLevel sensitivityLevel) {
        List<RuntimeException> errors = new ArrayList<>();
        ArrayList<RpaStep> transformedData = new ArrayList<>();

        if (isNotEmpty(rawData)) {
            Set<String> ids = new HashSet<>();
            for (Map<String, Map<String, String>> mapStep : rawData) {
                Map<String, String> stepProps = mapStep.values().iterator().next();
                try {
                    validateNotEmptyValues(stepProps);
                    validateOnlySupportedKeys(stepProps);

                    RpaStep rpaStep = transformStep(stepProps);

                    validateUniqueIds(ids, rpaStep);
                    validateAssignmentAction(rpaStep);

                    transformedData.add(rpaStep);
                } catch (RuntimeException rex) {
                    errors.add(rex);
                }
            }
        }

        return new BasicTransformModellingResult<>(transformedData, errors);
    }

    private void validateAssignmentAction(RpaStep rpaStep) {
        if (rpaStep.getAction().equals(RPA_ASSIGNMENT_ACTION) &&
                (!OUTPUT_ASSIGNMENT.matcher(rpaStep.getObjectPath()).matches() ||
                isEmpty(rpaStep.getArgs()))) {
            throw new RuntimeException(String.format(INVALID_ASSIGNMENT_OPERATION, rpaStep.getId()));
        }
    }

    private void validateUniqueIds(Set<String> ids, RpaStep rpaStep) {
        if (!ids.add(rpaStep.getId())) {
            throw new RuntimeException(String.format(FOUND_DUPLICATE_STEP_WITH_ID, rpaStep.getId()));
        }
    }

    private RpaStep transformStep(Map<String, String> stepProps) {
        RpaStep rpaStep = new RpaStep();
        rpaStep.setId(stepProps.get(SlangTextualKeys.RPA_STEP_ID_KEY));
        rpaStep.setObjectPath(stepProps.get(SlangTextualKeys.RPA_STEP_PATH_KEY));
        rpaStep.setAction(stepProps.get(SlangTextualKeys.RPA_STEP_ACTION_KEY));
        rpaStep.setArgs(stepProps.get(SlangTextualKeys.RPA_STEP_ARGS_KEY));
        rpaStep.setName(stepProps.get(SlangTextualKeys.RPA_STEP_NAME_KEY));
        rpaStep.setSnapshot(stepProps.get(SlangTextualKeys.RPA_STEP_SNAPSHOT_KEY));
        rpaStep.setHighlightId(stepProps.get(SlangTextualKeys.RPA_STEP_HIGHLIGHT_ID_KEY));
        return rpaStep;
    }

    private void validateNotEmptyValues(Map<String, String> tMap) {
        Validate.notNull(tMap);
        Validate.notNull(MANDATORY_KEY_SET);
        Validate.notNull(OPTIONAL_KEY_SET);

        Set<String> missingKeys = new HashSet<>();
        Set<String> emptyValuesKeys = new HashSet<>();
        for (String reqKey : MANDATORY_KEY_SET) {
            String reqValue = tMap.get(reqKey);
            if (reqValue == null) {
                missingKeys.add(reqKey);
            } else if (reqValue.equals("")) {
                emptyValuesKeys.add(reqKey);
            }
        }
        if (CollectionUtils.isNotEmpty(missingKeys)) {
            throw new RuntimeException(RPA_OPERATION_HAS_MISSING_TAGS + missingKeys.toString());
        }

        if (CollectionUtils.isNotEmpty(emptyValuesKeys)) {
            throw new RuntimeException(RPA_OPERATION_HAS_EMPTY_TAGS + emptyValuesKeys.toString());
        }
    }

    private void validateOnlySupportedKeys(Map<String, String> tMap) {
        Set<String> invalidKeys = new HashSet<>(tMap.keySet());
        invalidKeys.removeAll(MANDATORY_KEY_SET);
        invalidKeys.removeAll(OPTIONAL_KEY_SET);
        if (CollectionUtils.isNotEmpty(invalidKeys)) {
            throw new RuntimeException(RPA_OPERATION_ILLEGAL_TAGS + invalidKeys.toString() +
                    INVALID_KEYS_ERROR_MESSAGE_SUFFIX);
        }
    }

    @Override
    public List<Scope> getScopes() {
        return Collections.emptyList();
    }

    @Override
    public String keyToTransform() {
        return null;
    }
}
