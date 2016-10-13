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
import io.cloudslang.lang.runtime.events.LanguageEventData;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static ch.lambdaj.Lambda.by;
import static ch.lambdaj.Lambda.group;
import static ch.lambdaj.Lambda.on;

/**
 * Date: 4/8/2015
 *
 * @author Bonczidai Levente
 */
public class JoinAggregatorListener extends AbstractAggregatorListener {

    public Map<String, StepData> aggregate() {

        Map<String, StepData> joinDataByPath = new HashMap<>();

        Group<LanguageEventData> groups = group(getEvents(), by(on(LanguageEventData.class).getPath()));

        for (Group<LanguageEventData> subGroup : groups.subgroups()) {
            StepData joinData = buildPublishAggregateData(subGroup.first());
            joinDataByPath.put(joinData.getPath(), joinData);
        }

        return joinDataByPath;
    }

    private StepData buildPublishAggregateData(LanguageEventData data) {
        String path = data.getPath();
        String stepName = data.getStepName();
        Map<String, Serializable> outputs = data.getOutputs();
        String result = (String) data.get(LanguageEventData.RESULT);
        return new StepData(
                path,
                stepName,
                new HashMap<String, Serializable>(),
                outputs,
                null, result
        );
    }

}
