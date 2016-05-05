/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.env;

import io.cloudslang.lang.entities.bindings.values.Value;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Iterator;

public class ForLoopCondition implements LoopCondition {

    private final Iterable<Value> iterable;
    private int index = 0;

    public ForLoopCondition(Iterable<Value> iterable) {
        this.iterable = iterable;
    }

    private Iterator<? extends Value> loopToCurrentObject() {
        Iterator<? extends Value> iterator = iterable.iterator();
        for (int i = 0; i < index; i++) {
            iterator.next();
        }
        return iterator;
    }

    public Value next() {
        Value next = loopToCurrentObject().next();
        index++;
        return next;
    }

    @Override
    public boolean hasMore() {
        return loopToCurrentObject().hasNext();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ForLoopCondition that = (ForLoopCondition) o;

        return new EqualsBuilder()
                .append(this.iterable, that.iterable)
                .append(this.index, that.index)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(iterable)
                .append(index)
                .toHashCode();
    }

    @Override
    public Iterator<Value> iterator() {
        return iterable.iterator();
    }
}
