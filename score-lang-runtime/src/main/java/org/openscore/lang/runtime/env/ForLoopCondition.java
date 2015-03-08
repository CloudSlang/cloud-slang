/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.openscore.lang.runtime.env;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

public class ForLoopCondition implements LoopCondition {

    private final Iterable<? extends Serializable> iterable;

    public ForLoopCondition(Iterable<? extends Serializable> iterable) {
        this.iterable = iterable;
    }

    public Serializable next() {
        return iterable.iterator().next();
    }

    @Override
    public boolean hasMore() {
        return iterable.iterator().hasNext();
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
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(iterable)
                .toHashCode();
    }
}
