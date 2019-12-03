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

import io.cloudslang.lang.compiler.modeller.model.SeqStep;
import io.cloudslang.lang.compiler.modeller.result.BasicTransformModellingResult;
import io.cloudslang.lang.compiler.modeller.result.TransformModellingResult;
import io.cloudslang.lang.entities.SensitivityLevel;
import org.apache.commons.lang.StringUtils;
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
import static io.cloudslang.lang.compiler.SlangTextualKeys.SEQ_STEP_ACTION_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SEQ_STEP_ARGS_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SEQ_STEP_DEFAULT_ARGS_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SEQ_STEP_HIGHLIGHT_ID_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SEQ_STEP_ID_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SEQ_STEP_NAME_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SEQ_STEP_PATH_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SEQ_STEP_SNAPSHOT_KEY;
import static io.cloudslang.lang.compiler.modeller.transformers.AbstractTransformer.INVALID_KEYS_ERROR_MESSAGE_SUFFIX;
import static io.cloudslang.lang.entities.ScoreLangConstants.SEQ_ASSIGNMENT_ACTION;
import static java.util.regex.Pattern.compile;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.isEmpty;

public class SeqStepsTransformer extends AbstractInOutForTransformer
        implements Transformer<List<Map<String, Map<String, String>>>, ArrayList<SeqStep>> {
    private static final String SEQ_OPERATION_HAS_MISSING_TAGS =
            "Sequential operation step has the following missing tags: ";
    private static final Pattern OUTPUT_ASSIGNMENT = compile("Parameter\\(\"[^\"]+\"\\)");

    private static final Set<String> MANDATORY_KEY_SET = newHashSet(SEQ_STEP_ID_KEY,
            SEQ_STEP_PATH_KEY, SEQ_STEP_ACTION_KEY);
    private static final Set<String> WAIT_MANDATORY_KEYS = newHashSet(SEQ_STEP_ID_KEY, SEQ_STEP_ACTION_KEY,
            SEQ_STEP_DEFAULT_ARGS_KEY);
    private static final Set<String> WAIT_OPTIONAL_KEY_SET = newHashSet(SEQ_STEP_ARGS_KEY);
    private static final Set<String> OPTIONAL_KEY_SET = newHashSet(SEQ_STEP_ARGS_KEY,
            SEQ_STEP_DEFAULT_ARGS_KEY, SEQ_STEP_HIGHLIGHT_ID_KEY,
            SEQ_STEP_SNAPSHOT_KEY, SEQ_STEP_NAME_KEY);

    private static final String FOUND_DUPLICATE_STEP_WITH_ID =
            "Found duplicate step with id '%s' for sequential operation step.";
    private static final String INVALID_ASSIGNMENT_OPERATION =
            "Found invalid assignment operation for sequential operation step with id '%s'.";
    private static final String SEQ_OPERATION_HAS_EMPTY_TAGS =
            "Sequential operation step has the following empty tags: ";
    private static final String SEQ_OPERATION_ILLEGAL_TAGS =
            "Sequential operation step has the following illegal tags: ";

    private static final String WAIT = "Wait";
    private static final String WAIT_PARAM_REQUIRED = "Parameter required for 'Wait'.";
    private static final String WAIT_PARAM_INVALID = "'Wait' parameter is invalid. It should be between %.0f and %.0f.";
    private static final double WAIT_MIN_VALUE = 1.0;
    private static final double WAIT_MAX_VALUE = 86400.0;


    @Override
    public TransformModellingResult<ArrayList<SeqStep>> transform(List<Map<String, Map<String, String>>> rawData) {
        return transform(rawData, DEFAULT_SENSITIVITY_LEVEL);
    }

    @Override
    public TransformModellingResult<ArrayList<SeqStep>> transform(List<Map<String, Map<String, String>>> rawData,
                                                                  SensitivityLevel sensitivityLevel) {
        List<RuntimeException> errors = new ArrayList<>();
        ArrayList<SeqStep> transformedData = new ArrayList<>();

        if (isNotEmpty(rawData)) {
            Set<String> ids = new HashSet<>();
            for (Map<String, Map<String, String>> mapStep : rawData) {
                Map<String, String> stepProps = mapStep.values().iterator().next();

                try {
                    boolean isWaitStep = isWaitStep(stepProps);
                    if (isWaitStep) {
                        validateWaitStep(stepProps);
                    }

                    Set<String> mandatoryKeySet = isWaitStep ? WAIT_MANDATORY_KEYS : MANDATORY_KEY_SET;
                    Set<String> optionalKeySet = isWaitStep ? WAIT_OPTIONAL_KEY_SET : OPTIONAL_KEY_SET;

                    validateNotEmptyValues(stepProps, mandatoryKeySet, optionalKeySet);
                    validateOnlySupportedKeys(stepProps, mandatoryKeySet, optionalKeySet);

                    SeqStep seqStep = transformStep(stepProps);

                    validateUniqueIds(ids, seqStep);

                    if (!isWaitStep) {
                        validateAssignmentAction(seqStep);
                    }

                    transformedData.add(seqStep);

                } catch (RuntimeException rex) {
                    errors.add(rex);
                }
            }
        }

        return new BasicTransformModellingResult<>(transformedData, errors);
    }

    private void validateWaitStep(Map<String, String> stepProps) {
        String args = stepProps.getOrDefault(SEQ_STEP_ARGS_KEY, stepProps.get(SEQ_STEP_DEFAULT_ARGS_KEY));
        if (StringUtils.isEmpty(args) || StringUtils.equals("\"\"", args)) {
            throw new RuntimeException(WAIT_PARAM_REQUIRED);
        }
        validateWaitArgRange(args);
    }

    private void validateWaitArgRange(String args) {
        if (StringUtils.startsWith(args,"\"") && StringUtils.endsWith(args,"\"")) {
            String constValue = StringUtils.substring(args, 1, args.length() - 1);
            try {
                double value = Double.parseDouble(constValue);
                if (value < WAIT_MIN_VALUE || value > WAIT_MAX_VALUE) {
                    throw new RuntimeException(String.format(WAIT_PARAM_INVALID, WAIT_MIN_VALUE, WAIT_MAX_VALUE));
                }
            } catch (NumberFormatException ignore) {
               // not a numeric input is valid
            }
        }
    }

    private boolean isWaitStep(Map<String, String> stepProps) {
        return StringUtils.equalsIgnoreCase(WAIT, stepProps.get(SEQ_STEP_ACTION_KEY));
    }

    private void validateAssignmentAction(SeqStep seqStep) {
        if (seqStep.getAction().equals(SEQ_ASSIGNMENT_ACTION) &&
                (!OUTPUT_ASSIGNMENT.matcher(seqStep.getObjectPath()).matches() ||
                isEmpty(seqStep.getArgs()))) {
            throw new RuntimeException(String.format(INVALID_ASSIGNMENT_OPERATION, seqStep.getId()));
        }
    }

    private void validateUniqueIds(Set<String> ids, SeqStep seqStep) {
        if (!ids.add(seqStep.getId())) {
            throw new RuntimeException(String.format(FOUND_DUPLICATE_STEP_WITH_ID, seqStep.getId()));
        }
    }

    private SeqStep transformStep(Map<String, String> stepProps) {
        SeqStep seqStep = new SeqStep();
        seqStep.setId(stepProps.get(SEQ_STEP_ID_KEY));
        seqStep.setObjectPath(stepProps.get(SEQ_STEP_PATH_KEY));
        seqStep.setAction(stepProps.get(SEQ_STEP_ACTION_KEY));
        seqStep.setArgs(stepProps.get(SEQ_STEP_ARGS_KEY));
        seqStep.setDefaultArgs(stepProps.get(SEQ_STEP_DEFAULT_ARGS_KEY));
        seqStep.setName(stepProps.get(SEQ_STEP_NAME_KEY));
        seqStep.setSnapshot(stepProps.get(SEQ_STEP_SNAPSHOT_KEY));
        seqStep.setHighlightId(stepProps.get(SEQ_STEP_HIGHLIGHT_ID_KEY));

        Accumulator accumulator = extractFunctionData(stepProps.get(SEQ_STEP_ARGS_KEY));
        seqStep.setSystemPropertyDependencies(accumulator.getSystemPropertyDependencies());

        return seqStep;
    }

    private void validateNotEmptyValues(Map<String, String> tMap, Set<String> mandatoryKeySet,
                                        Set<String> optionalKeySet) {
        Validate.notNull(tMap);
        Validate.notNull(mandatoryKeySet);
        Validate.notNull(optionalKeySet);

        Set<String> missingKeys = new HashSet<>();
        Set<String> emptyValuesKeys = new HashSet<>();
        for (String reqKey : mandatoryKeySet) {
            String reqValue = tMap.get(reqKey);
            if (reqValue == null) {
                missingKeys.add(reqKey);
            } else if (reqValue.equals("")) {
                emptyValuesKeys.add(reqKey);
            }
        }
        if (isNotEmpty(missingKeys)) {
            throw new RuntimeException(SEQ_OPERATION_HAS_MISSING_TAGS + missingKeys.toString());
        }

        if (isNotEmpty(emptyValuesKeys)) {
            throw new RuntimeException(SEQ_OPERATION_HAS_EMPTY_TAGS + emptyValuesKeys.toString());
        }
    }

    private void validateOnlySupportedKeys(Map<String, String> tMap, Set<String> mandatoryKeys,
                                           Set<String> optionalKeys) {
        Set<String> invalidKeys = new HashSet<>(tMap.keySet());
        invalidKeys.removeAll(mandatoryKeys);
        invalidKeys.removeAll(optionalKeys);
        if (isNotEmpty(invalidKeys)) {
            throw new RuntimeException(SEQ_OPERATION_ILLEGAL_TAGS + invalidKeys.toString() +
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
