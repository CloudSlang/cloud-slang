/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build.tester.parallel.testcaseevents;

import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import java.util.EventObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class SlangTestCaseEvent extends EventObject {

    private SlangTestCase slangTestCase;

    public SlangTestCaseEvent(SlangTestCase slangTestCase) {
        super(slangTestCase.getName());
        this.slangTestCase = slangTestCase;
    }

    public SlangTestCase getSlangTestCase() {
        return slangTestCase;
    }

    public void setSlangTestCase(SlangTestCase slangTestCase) {
        this.slangTestCase = slangTestCase;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        SlangTestCaseEvent rhs = (SlangTestCaseEvent) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.slangTestCase, rhs.slangTestCase)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(slangTestCase)
                .toHashCode();
    }
}
