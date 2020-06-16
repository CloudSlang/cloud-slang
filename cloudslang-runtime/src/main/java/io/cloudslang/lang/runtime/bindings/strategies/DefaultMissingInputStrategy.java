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
import io.cloudslang.lang.runtime.env.RunEnvironment;
import io.cloudslang.lang.runtime.events.LanguageEventData;
import io.cloudslang.score.lang.ExecutionRuntimeServices;
import io.cloudslang.score.lang.SystemContext;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Default strategy to keep existing behavior
 */
@Component
public class DefaultMissingInputStrategy implements MissingInputStrategy {

    @Override
    public boolean resolveMissingInputs(List<Input> missingInputs,
                                        SystemContext systemContext,
                                        RunEnvironment runEnv,
                                        ExecutionRuntimeServices runtimeServices,
                                        LanguageEventData.StepType stepType,
                                        String stepName) {
        missingInputs
                .stream()
                .findFirst()
                .map(Input::getName)
                .ifPresent(inputName -> {
                    throw new RuntimeException("Input with name: '" + inputName + "' is Required, but value is empty");
                });

        return false;
    }

}
