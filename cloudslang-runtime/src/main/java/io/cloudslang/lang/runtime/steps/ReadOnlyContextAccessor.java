/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.steps;

import io.cloudslang.lang.entities.bindings.values.Value;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static java.util.Collections.addAll;

public class ReadOnlyContextAccessor implements Serializable {

    private List<Map<String, Value>> holder;

    public ReadOnlyContextAccessor() {
        holder = new ArrayList<>();
    }

    public ReadOnlyContextAccessor(Map<String, Value>... publishContexts) {
        this();
        addAll(holder, publishContexts);
    }

    public ReadOnlyContextAccessor(ReadOnlyContextAccessor accessor) {
        this.holder = accessor.holder;
    }

    public List<Map<String, Value>> getContextHolder() {
        return holder;
    }

    public Value getValue(String key) {
        // Reversed order traversal to simulate putAll
        ListIterator<Map<String, Value>> listIterator = this.holder.listIterator(this.holder.size());
        while (listIterator.hasPrevious()) {
            Map<String, Value> element = listIterator.previous();
            if (element.containsKey(key)) {
                return element.get(key);
            }
        }
        return null;
    }

    public Map<String, Value> getMergedContexts() {
        Map<String, Value> context = new HashMap<>();
        for (Map<String, Value> map : holder) {
            context.putAll(map);
        }
        return context;
    }
}
