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
 * Date: 07/10/2014
 * Time: 12:53
 */
public class ContextStack implements Serializable {

    private Stack<Context> stack = new Stack<>();

    public void pushContext(Context newContext) {
        stack.push(newContext);
    }

    public Context popContext() {
        if (stack.empty()) {
            return null;
        }
        return stack.pop();
    }

}
