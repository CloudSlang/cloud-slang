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

import java.util.List;
import java.util.Map;

/**
 * Date: 4/8/2015
 *
 * @author Bonczidai Levente
 */
public class RuntimeInformation {
    private final Map<String, StepData> tasks;
    private final Map<String, List<StepData>> branchesByPath;
    private final Map<String, StepData> asyncTasks;

    public RuntimeInformation(Map<String, StepData> tasks, Map<String, List<StepData>> branchesByPath, Map<String, StepData> asyncTasks) {
        this.tasks = tasks;
        this.branchesByPath = branchesByPath;
        this.asyncTasks = asyncTasks;
    }

    public Map<String, StepData> getTasks() {
        return tasks;
    }

    public Map<String, List<StepData>> getBranchesByPath() {
        return branchesByPath;
    }

    public Map<String, StepData> getAsyncTasks() {
        return asyncTasks;
    }
}
