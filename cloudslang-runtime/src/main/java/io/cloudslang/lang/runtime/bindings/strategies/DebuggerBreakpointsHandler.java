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

import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import io.cloudslang.score.lang.SystemContext;


public interface DebuggerBreakpointsHandler extends InputHandler {

    boolean handleBreakpoints(
            SystemContext systemContext,
            RunEnvironment runEnv,
            ExecutionRuntimeServices runtimeServices,
            LanguageEventData.StepType stepType,
            String stepName,
            String stepId);

}
