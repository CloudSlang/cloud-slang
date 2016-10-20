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
import com.google.common.collect.Lists;
import io.cloudslang.lang.runtime.RuntimeConstants;
import io.cloudslang.lang.runtime.env.ReturnValues;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.lambdaj.Lambda.by;
import static ch.lambdaj.Lambda.group;
import static ch.lambdaj.Lambda.on;

/**
 * Date: 4/8/2015
 *
 * @author Bonczidai Levente
 */
public class BranchAggregatorListener extends AbstractAggregatorListener {

    public Map<String, List<StepData>> aggregate() {

        Map<String, List<StepData>> branchesDataByPath = new HashMap<>();

        Group<LanguageEventData> groups = group(getEvents(), by(on(LanguageEventData.class).getPath()));

        for (Group<LanguageEventData> subGroup : groups.subgroups()) {
            List<StepData> branchesData = buildBranchesData(subGroup.findAll());
            branchesDataByPath.put(branchesData.get(0).getPath(), branchesData);
        }

        return branchesDataByPath;
    }

    private List<StepData> buildBranchesData(List<LanguageEventData> data) {
        List<StepData> branches = Lists.newArrayList();

        for (LanguageEventData branchData : data) {
            String path = branchData.getPath();
            String stepName = branchData.getStepName();
            ReturnValues returnValues = (ReturnValues) branchData.get(RuntimeConstants.BRANCH_RETURN_VALUES_KEY);
            branches.add(
                    new StepData(
                            path,
                            stepName,
                            new HashMap<String, Serializable>(),
                            LanguageEventData.maskSensitiveValues(returnValues.getOutputs()),
                            null, returnValues.getResult()
                    )
            );
        }

        return branches;
    }

}
