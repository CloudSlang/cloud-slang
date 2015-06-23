/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

package io.cloudslang.lang.systemtests;

import ch.lambdaj.Lambda;
import ch.lambdaj.function.convert.Converter;
import ch.lambdaj.group.Group;
import io.cloudslang.lang.entities.ScoreLangConstants;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import org.apache.commons.collections4.CollectionUtils;
import org.hamcrest.Matchers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.lambdaj.Lambda.*;
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
        List<LanguageEventData> taskEvents = selectByStepType(data, LanguageEventData.StepType.TASK);
        List<LanguageEventData> executableEvents = selectByStepType(data, LanguageEventData.StepType.EXECUTABLE);

        LanguageEventData inputsEvent;
        LanguageEventData outputsEvent;

        if (CollectionUtils.isNotEmpty(taskEvents)) {
            inputsEvent = selectByEventType(taskEvents, ScoreLangConstants.EVENT_INPUT_END);
            outputsEvent = selectByEventType(taskEvents, ScoreLangConstants.EVENT_OUTPUT_END);
        } else {
            inputsEvent = selectByEventType(executableEvents, ScoreLangConstants.EVENT_INPUT_END);
            outputsEvent = selectByEventType(executableEvents, ScoreLangConstants.EVENT_OUTPUT_END);
        }
            String path = inputsEvent.getPath();
            String stepName = inputsEvent.getStepName();
            Map<String, Serializable> inputs = inputsEvent.getInputs();

            Map<String, Serializable> outputs = outputsEvent == null ? null : outputsEvent.getOutputs();
            String result = outputsEvent == null ? null : (String) outputsEvent.get(LanguageEventData.RESULT);

            String executableName = executableEvents.get(0).getStepName();

        return new StepData(path, stepName, inputs, outputs, executableName, result);
    }

    private List<LanguageEventData> selectByStepType(List<LanguageEventData> data, LanguageEventData.StepType stepType) {
        return select(data, having(on(LanguageEventData.class).getStepType(), equalTo(stepType)));
    }

    private LanguageEventData selectByEventType(List<LanguageEventData> data, String eventType) {
        return selectFirst(data, having(on(LanguageEventData.class).getEventType(), equalTo(eventType)));
    }

}
