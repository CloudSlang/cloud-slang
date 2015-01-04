/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

package org.openscore.lang.systemtests;

import ch.lambdaj.function.convert.Converter;
import ch.lambdaj.group.Group;
import org.openscore.events.ScoreEvent;
import org.openscore.events.ScoreEventListener;
import org.openscore.lang.runtime.events.LanguageEventData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.lambdaj.Lambda.*;
import static org.openscore.lang.entities.ScoreLangConstants.EVENT_INPUT_END;
import static org.openscore.lang.entities.ScoreLangConstants.EVENT_OUTPUT_END;

/*
 * Created by orius123 on 24/12/14.
 */
public class RunDataAggregatorListener implements ScoreEventListener {

    private static final String TASK_NAME = LanguageEventData.levelName.TASK_NAME.name();
    private static final String EXECUTABLE_NAME = LanguageEventData.levelName.EXECUTABLE_NAME.name();

    private final List<LanguageEventData> events = new ArrayList<>();

    @Override
    public void onEvent(ScoreEvent event) throws InterruptedException {
        LanguageEventData languageEvent = (LanguageEventData) event.getData();
        events.add(languageEvent);
    }

    public Map<String, StepData> aggregate() {

        Map<String, StepData> stepsData = new HashMap<>();

        Group<LanguageEventData> groups = group(events, by(on(LanguageEventData.class).getPath()));

        for (Group<LanguageEventData> subGroup : groups.subgroups()) {
            StepData stepData = buildStepData(subGroup.findAll());
            stepsData.put(stepData.getPath(), stepData);
        }

        return stepsData;
    }

    private StepData buildStepData(List<LanguageEventData> data) {

        Map<String, LanguageEventData> stepEvents = map(data, new Converter<LanguageEventData, String>() {
            @Override
            public String convert(LanguageEventData from) {
                return from.getEventType();
            }
        });

        LanguageEventData inputsEvent = stepEvents.get(EVENT_INPUT_END);
        LanguageEventData outputsEvent = stepEvents.get(EVENT_OUTPUT_END);

        String path = inputsEvent.getPath();
        String stepName = inputsEvent.get(TASK_NAME) != null ? (String) inputsEvent.get(TASK_NAME)
                : (String) inputsEvent.get(EXECUTABLE_NAME);
        Map<String, Serializable> inputs = inputsEvent.getInputs();

        Map<String, Serializable> outputs = outputsEvent == null ? null : outputsEvent.getOutputs();
        String result = outputsEvent == null ? null : (String) outputsEvent.get(LanguageEventData.RESULT);

        return new StepData(path, stepName, inputs, outputs, result);
    }

}
