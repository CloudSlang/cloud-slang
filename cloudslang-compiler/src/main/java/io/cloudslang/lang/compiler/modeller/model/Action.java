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



/*
 * Created by orius123 on 05/11/14.
 */

import java.io.Serializable;
import java.util.Map;

public class Action {

    private final Map<String, Serializable> actionData;

    public Action(Map<String, Serializable> actionData) {
        this.actionData = actionData;
    }

    public Map<String, Serializable> getActionData() {
        return actionData;
    }
}
