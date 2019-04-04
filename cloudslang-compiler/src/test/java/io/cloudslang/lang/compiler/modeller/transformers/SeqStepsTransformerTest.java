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
import io.cloudslang.lang.compiler.modeller.result.TransformModellingResult;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.cloudslang.lang.compiler.SlangTextualKeys.SEQ_STEP_ACTION_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SEQ_STEP_ARGS_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SEQ_STEP_DEFAULT_ARGS_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SEQ_STEP_HIGHLIGHT_ID_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SEQ_STEP_ID_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SEQ_STEP_PATH_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.SEQ_STEP_SNAPSHOT_KEY;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SeqStepsTransformerTest.Config.class})
public class SeqStepsTransformerTest extends TransformersTestParent {
    @Autowired
    private SeqStepsTransformer seqStepsTransformer;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testTransformSimple() {
        List<Map<String, Map<String, String>>> steps = asList(
                newStep("1", "Browser", "Open", "www.google.com", "www.google.com", null, null),
                newStep("2", "Browser", "Open", "www.google.com", "www.google.com", "snapshot", "1234"),
                newStep("3", "AnotherBrowser", "Open", "www..com", "www..com", "snapshot", null),
                newStep("4", "Browser", "Close", "www.google.com", "www.google.com", null, "1234"));
        List<SeqStep> expectedSteps = asList(
                newSeqStep("1", "Browser", "Open", "www.google.com", "www.google.com", null, null),
                newSeqStep("2", "Browser", "Open", "www.google.com", "www.google.com", "snapshot", "1234"),
                newSeqStep("3", "AnotherBrowser", "Open", "www..com", "www..com", "snapshot", null),
                newSeqStep("4", "Browser", "Close", "www.google.com", "www.google.com", null, "1234"));
        TransformModellingResult<ArrayList<SeqStep>> transform = seqStepsTransformer.transform(steps);

        assertThat(transform.getErrors(), is(empty()));
        assertEquals(expectedSteps, transform.getTransformedData());
    }

    @Test
    public void testTransformStepWithMissingReqKeys() {
        List<Map<String, Map<String, String>>> steps = singletonList(newStep(null, null, null, null, null, "a", "b"));
        List<SeqStep> expectedSteps = new ArrayList<>();
        TransformModellingResult<ArrayList<SeqStep>> transform = seqStepsTransformer.transform(steps);

        assertThat(transform.getErrors(), hasSize(1));
        assertEquals(transform.getErrors().get(0).getMessage(),
                "Sequential operation step has the following missing tags: [object_path, action, id]");
        assertEquals(expectedSteps, transform.getTransformedData());
    }

    @Test
    public void testTransformStepWithEmptyReqKeys() {
        List<Map<String, Map<String, String>>> steps = singletonList(newStep("", "", "", "", "", "a", "b"));
        List<SeqStep> expectedSteps = new ArrayList<>();
        TransformModellingResult<ArrayList<SeqStep>> transform = seqStepsTransformer.transform(steps);

        assertThat(transform.getErrors(), hasSize(1));
        assertEquals(transform.getErrors().get(0).getMessage(),
                "Sequential operation step has the following empty tags: [object_path, action, id]");
        assertEquals(expectedSteps, transform.getTransformedData());
    }

    @Test
    public void testTransformStepWithIllegalKeys() {
        List<Map<String, Map<String, String>>> steps =
                singletonList(newStep("1", "Browser", "Open", "www.google.com", "www.google.com", null, null));
        steps.get(0).get("step").put("illegal-key", "value-for-illegal-key");
        List<SeqStep> expectedSteps = new ArrayList<>();
        TransformModellingResult<ArrayList<SeqStep>> transform = seqStepsTransformer.transform(steps);

        assertThat(transform.getErrors(), hasSize(1));
        assertEquals(transform.getErrors().get(0).getMessage(),
                "Sequential operation step has the following illegal tags: [illegal-key]. " +
                        "Please take a look at the supported features per versions link");
        assertEquals(expectedSteps, transform.getTransformedData());
    }

    @Test
    public void testTransformStepWithDuplicateIds() {
        List<Map<String, Map<String, String>>> steps =
                asList(newStep("1", "Browser", "Open", "www.google.com", "www.google.com", null, null),
                newStep("1", "Tab", "Close", null, null, null, null));
        List<SeqStep> expectedSteps =
                singletonList(newSeqStep("1", "Browser", "Open", "www.google.com", "www.google.com", null, null));
        TransformModellingResult<ArrayList<SeqStep>> transform = seqStepsTransformer.transform(steps);

        assertThat(transform.getErrors(), hasSize(1));
        assertEquals(transform.getErrors().get(0).getMessage(),
                "Found duplicate step with id '1' for sequential operation step.");
        assertEquals(expectedSteps, transform.getTransformedData());
    }

    @Test
    public void testTransformStepWithAssignmentAction() {
        List<Map<String, Map<String, String>>> steps =
                singletonList(newStep("1", "Parameter(\"param1\")", "=", "12", "12", null, null));
        List<SeqStep> expectedSteps =
                singletonList(newSeqStep("1", "Parameter(\"param1\")", "=", "12", "12", null, null));
        TransformModellingResult<ArrayList<SeqStep>> transform = seqStepsTransformer.transform(steps);

        assertThat(transform.getErrors(), is(empty()));
        assertEquals(expectedSteps, transform.getTransformedData());
    }

    @Test
    public void testTransformStepWithAssignmentActionMissingArg() {
        List<Map<String, Map<String, String>>> steps =
                singletonList(newStep("1", "Parameter(\"param1\")", "=", null, null, null, null));
        List<SeqStep> expectedSteps = new ArrayList<>();
        TransformModellingResult<ArrayList<SeqStep>> transform = seqStepsTransformer.transform(steps);

        assertThat(transform.getErrors(), hasSize(1));
        assertEquals(transform.getErrors().get(0).getMessage(),
                "Found invalid assignment operation for sequential operation step with id '1'.");
        assertEquals(expectedSteps, transform.getTransformedData());
    }

    private Map<String, Map<String, String>> newStep(String id,
                                                     String objPath,
                                                     String action,
                                                     String args,
                                                     String defaultArgs,
                                                     String snapshot,
                                                     String highlightId) {
        Map<String, String> stepDetails = new HashMap<>();

        putIfValueNotNull(stepDetails, SEQ_STEP_ID_KEY, id);
        putIfValueNotNull(stepDetails, SEQ_STEP_PATH_KEY, objPath);
        putIfValueNotNull(stepDetails, SEQ_STEP_ACTION_KEY, action);
        putIfValueNotNull(stepDetails, SEQ_STEP_ARGS_KEY, args);
        putIfValueNotNull(stepDetails, SEQ_STEP_DEFAULT_ARGS_KEY, defaultArgs);
        putIfValueNotNull(stepDetails, SEQ_STEP_SNAPSHOT_KEY, snapshot);
        putIfValueNotNull(stepDetails, SEQ_STEP_HIGHLIGHT_ID_KEY, highlightId);

        Map<String, Map<String, String>> step = new HashMap<>();
        step.put("step", stepDetails);

        return step;
    }

    private SeqStep newSeqStep(String id,
                               String objPath,
                               String action,
                               String args,
                               String defaultArgs,
                               String snapshot,
                               String highlightId) {
        SeqStep seqStep = new SeqStep();

        seqStep.setId(id);
        seqStep.setObjectPath(objPath);
        seqStep.setAction(action);
        seqStep.setArgs(args);
        seqStep.setDefaultArgs(defaultArgs);
        seqStep.setSnapshot(snapshot);
        seqStep.setHighlightId(highlightId);

        return seqStep;
    }

    private void putIfValueNotNull(Map<String, String> stepDetails, String key, String value) {
        if (value != null) {
            stepDetails.put(key, value);
        }
    }

    public static class Config {
        @Bean
        public SeqStepsTransformer seqStepsTransformer() {
            return new SeqStepsTransformer();
        }
    }
}
