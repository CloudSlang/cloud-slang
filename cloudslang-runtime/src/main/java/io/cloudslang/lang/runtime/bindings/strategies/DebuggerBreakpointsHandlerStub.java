package io.cloudslang.lang.runtime.bindings.strategies;

import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import io.cloudslang.score.lang.SystemContext;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DebuggerBreakpointsHandlerStub implements DebuggerBreakpointsHandler {

    @Override
    public boolean resolveInputs(List<Input> newInputs, SystemContext systemContext, RunEnvironment runEnv, ExecutionRuntimeServices runtimeServices, LanguageEventData.StepType stepType, String stepName) {
        return false;
    }

    @Override
    public Map<String, ? extends Value> applyValues(SystemContext systemContext, Collection<Input> inputs) {
        return null;
    }

    @Override
    public boolean handleBreakpoints(SystemContext context, String stepId) {
        return false;
    }
}
