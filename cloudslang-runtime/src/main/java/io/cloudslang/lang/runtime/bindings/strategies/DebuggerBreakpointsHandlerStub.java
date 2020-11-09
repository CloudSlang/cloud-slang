/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.bindings.strategies;

import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import io.cloudslang.score.lang.SystemContext;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
public class DebuggerBreakpointsHandlerStub implements DebuggerBreakpointsHandler {

    @Override
    public boolean resolveInputs(List<Input> newInputs, SystemContext systemContext,
                                 RunEnvironment runEnv, ExecutionRuntimeServices runtimeServices,
                                 LanguageEventData.StepType stepType, String stepName) {
        return false;
    }

    @Override
    public Map<String,Value> resolveInputs(SystemContext systemContext) {
        return null;
    }

    @Override
    public Map<String, ? extends Value> applyValues(SystemContext systemContext, Collection<Input> inputs) {
        return null;
    }

    @Override
    public boolean handleBreakpoints(SystemContext systemContext, RunEnvironment runEnv,
                                     ExecutionRuntimeServices runtimeServices, LanguageEventData.StepType stepType,
                                     String stepName, String stepId) {
        return false;
    }
}
