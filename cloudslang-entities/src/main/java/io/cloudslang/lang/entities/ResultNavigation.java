/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.entities;

import java.io.Serializable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author moradi
 * @version $Id$
 * @since 26/11/2014
 */
public class ResultNavigation implements Serializable {

    private long nextStepId;
    private String presetResult;

    public ResultNavigation(long nextStepId, String presetResult) {
        this.nextStepId = nextStepId;
        this.presetResult = presetResult;
    }

    /**
     * only here to satisfy serialization libraries
     */
    private ResultNavigation() {
    }

    public long getNextStepId() {
        return this.nextStepId;
    }

    public String getPresetResult() {
        return this.presetResult;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("nextStepId", nextStepId)
                .append("presetResult", presetResult)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ResultNavigation that = (ResultNavigation) o;

        return new EqualsBuilder()
                .append(nextStepId, that.nextStepId)
                .append(presetResult, that.presetResult)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(nextStepId)
                .append(presetResult)
                .toHashCode();
    }

}
