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

import io.cloudslang.lang.entities.WorkerGroupMetadata;

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

    private static final long serialVersionUID = -3596242783100345155L;

    private Deque<ParentFlowData> stack;

    public ParentFlowStack() {
        this.stack = new ArrayDeque<>();
    }

    public ParentFlowData peekFirstParentFlowData() {
        return stack.peekFirst();
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

    public WorkerGroupMetadata computeParentWorkerGroup() {
        WorkerGroupMetadata workerGroupVal = new WorkerGroupMetadata();

        Iterator iterator = stack.descendingIterator();
        while (iterator.hasNext()) {
            ParentFlowData parentFlowData = (ParentFlowData) iterator.next();
            WorkerGroupMetadata workerGroupTemp = parentFlowData.getWorkerGroup();
            if (workerGroupTemp.getValue() != null) {
                workerGroupVal = workerGroupTemp;
                if (workerGroupTemp.isOverride()) {
                    break;
                }
            }
        }
        return workerGroupVal;
    }
}
