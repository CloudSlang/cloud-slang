/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.systemtests;

import ch.lambdaj.group.Group;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;

import static ch.lambdaj.Lambda.by;
import static ch.lambdaj.Lambda.group;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.selectFirst;
import static org.hamcrest.Matchers.equalTo;

/*
 * Created by orius123 on 24/12/14.
 */
public class RunDataAggregatorListener extends AbstractAggregatorListener {

    public Map<String, StepData> aggregate() {

        Map<String, StepData> stepsData = new HashMap<>();

        Group<LanguageEventData> groups = group(getEvents(), by(on(LanguageEventData.class).getPath()));

        for (Group<LanguageEventData> subGroup : groups.subgroups()) {
            StepData stepData = buildStepData(subGroup.findAll());
            stepsData.put(stepData.getPath(), stepData);
        }

        return stepsData;
    }

    private StepData buildStepData(List<LanguageEventData> data) {
        List<LanguageEventData> stepEvents = selectByStepType(data, LanguageEventData.StepType.STEP);
        List<LanguageEventData> executableEvents =
                selectByStepType(data, LanguageEventData.StepType.getExecutableTypes());

        LanguageEventData inputsEvent;
        LanguageEventData outputsEvent;

        boolean isStep = CollectionUtils.isNotEmpty(stepEvents);
        if (isStep) {
            inputsEvent = selectByEventType(stepEvents, ScoreLangConstants.EVENT_ARGUMENT_END);
            outputsEvent = selectByEventType(stepEvents, ScoreLangConstants.EVENT_OUTPUT_END);
        } else {
            inputsEvent = selectByEventType(executableEvents, ScoreLangConstants.EVENT_INPUT_END);
            outputsEvent = selectByEventType(executableEvents, ScoreLangConstants.EVENT_OUTPUT_END);
        }
        String path = inputsEvent.getPath();
        String stepName = inputsEvent.getStepName();
        Map<String, Serializable> inputs;
        if (isStep) {
            inputs = inputsEvent.getArguments();
        } else {
            inputs = inputsEvent.getInputs();
        }

        Map<String, Serializable> outputs = outputsEvent == null ? null : outputsEvent.getOutputs();
        String result = outputsEvent == null ? null : (String) outputsEvent.get(LanguageEventData.RESULT);

        String executableName = executableEvents.get(0).getStepName();

        return new StepData(path, stepName, inputs, outputs, executableName, result);
    }

    private List<LanguageEventData> selectByStepType(List<LanguageEventData> data,
                                                     LanguageEventData.StepType... stepTypes) {
        List<LanguageEventData> result = new ArrayList<>();
        for (LanguageEventData element : data) {
            boolean match = false;
            for (LanguageEventData.StepType typeToCheck : stepTypes) {
                if (element.getStepType().equals(typeToCheck)) {
                    match = true;
                }
            }
            if (match) {
                result.add(element);
            }
        }
        return result;
    }

    private LanguageEventData selectByEventType(List<LanguageEventData> data, String eventType) {
        return selectFirst(data, having(on(LanguageEventData.class).getEventType(), equalTo(eventType)));
    }

}
