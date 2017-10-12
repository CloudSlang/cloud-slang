/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller.model;

import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.Result;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Bonczidai Levente
 * @since 7/7/2016
 */
public class Decision extends Executable {

    public Decision(
            Map<String, Serializable> preExecActionData,
            Map<String, Serializable> postExecActionData,
            String namespace,
            String name,
            List<Input> inputs,
            List<Output> outputs,
            List<Result> results,
            Set<String> executableDependencies,
            Set<String> systemPropertyDependencies) {
        super(preExecActionData, postExecActionData, namespace, name, inputs,
                outputs, results, executableDependencies, systemPropertyDependencies);
    }

    @Override
    public String getType() {
        return SlangTextualKeys.DECISION_TYPE;
    }

}
