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

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;


/**
 * User: stoneo
 * Date: 22/10/2014
 * Time: 15:37
 */
public class ParentFlowStack implements Serializable {

    private Deque<ParentFlowData> stack;

    public ParentFlowStack() {
        this.stack = new ArrayDeque<>();
    }

    public void pushParentFlowData(ParentFlowData newContext) {
        stack.push(newContext);
    }

    public ParentFlowData popParentFlowData() {
        if (stack.isEmpty()) {
            return null;
        } else {
            return stack.pop();
        }
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public int size() {
        return stack.size();
    }

    public Iterator<ParentFlowData> descendingIteratorParentStackData() {
        return stack.descendingIterator();
    }
}
