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

import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

public class ForLoopCondition implements LoopCondition {

    private final ArrayList<? extends Serializable> iterable;
    private int index;

    public ForLoopCondition(Iterable<? extends Serializable> param) {
        if (param instanceof ArrayList) {
            ArrayList<? extends Serializable> localList;
            try {
                localList = (ArrayList<? extends Serializable>) param;
            } catch (Exception exc) {
                localList = copyIterable(param);
            }
            this.iterable = localList;
        } else {
            this.iterable = copyIterable(param);
        }
        this.index = 0;
    }

    private ArrayList<Serializable> copyIterable(Iterable<? extends Serializable> param) {
        ArrayList<Serializable> list = new ArrayList<>();
        for (Serializable next : param) {
            list.add(next);
        }
        return list;
    }

    private ListIterator<? extends Serializable> loopToCurrentObject() {
        return iterable.listIterator(index);
    }

    public Value next() {
        Serializable serializable = loopToCurrentObject().next();
        Value next = serializable instanceof Value ? (Value) serializable : ValueFactory.create(serializable);
        index++;
        return next;
    }

    @Override
    public boolean hasMore() {
        return loopToCurrentObject().hasNext();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

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
    public int getIndex() {
        return index;
    }
}
