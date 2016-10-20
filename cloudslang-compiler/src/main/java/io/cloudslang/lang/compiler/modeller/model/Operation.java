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

/*
 * Created by orius123 on 09/11/14.
 */
public class Operation extends Executable {

    private final Action action;

    public Operation(Map<String, Serializable> preOpActionData,
                     Map<String, Serializable> postOpActionData,
                     Action action,
                     String namespace,
                     String name,
                     List<Input> inputs,
                     List<Output> outputs,
                     List<Result> results,
                     Set<String> executableDependencies,
                     Set<String> systemPropertyDependencies) {
        super(preOpActionData, postOpActionData, namespace, name, inputs,
                outputs, results, executableDependencies, systemPropertyDependencies);
        this.action = action;
    }

    public Action getAction() {
        return action;
    }

    @Override
    public String getType() {
        return SlangTextualKeys.OPERATION_TYPE;
    }

}
