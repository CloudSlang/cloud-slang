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
import io.cloudslang.lang.runtime.events.LanguageEventData.StepType;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import io.cloudslang.score.lang.SystemContext;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Service to be implemented to customize behavior when some of step inputs is missing
 */
public interface MissingInputHandler {

    /**
     * @param missingInputs           the list of inputs for
     * @param systemContext           the system context
     * @param runEnv                  the run environment
     * @param runtimeServices         the runtime services
     * @param stepType                the step type
     * @param stepName                the step name
     * @param emptyValuesForPrompts   the flag to force empty value for prompts
     * @return                  boolean flag to indicate if engine can continue
     */
    boolean resolveMissingInputs(List<Input> missingInputs,
                                 SystemContext systemContext,
                                 RunEnvironment runEnv,
                                 ExecutionRuntimeServices runtimeServices,
                                 StepType stepType,
                                 String stepName,
                                 boolean emptyValuesForPrompts);

    /**
     * @param systemContext the system context
     * @param callArguments list of call arguments that could get enhanced in this call
     */
    default void applyPromptInputValues(SystemContext systemContext, Map<String, Value> callArguments,
                                        Collection<Input> inputs) {

    }
}
