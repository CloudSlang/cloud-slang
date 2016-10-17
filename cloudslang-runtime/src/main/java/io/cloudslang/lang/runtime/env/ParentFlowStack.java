/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.env;

import java.io.Serializable;
import java.util.Stack;

/**
 * User: stoneo
 * Date: 22/10/2014
 * Time: 15:37
 */
public class ParentFlowStack implements Serializable {

    private Stack<ParentFlowData> stack = new Stack<>();

    public void pushParentFlowData(ParentFlowData newContext) {
        stack.push(newContext);
    }

    public ParentFlowData popParentFlowData() {
        if (stack.empty()) {
            return null;
        }
        return stack.pop();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }
}
