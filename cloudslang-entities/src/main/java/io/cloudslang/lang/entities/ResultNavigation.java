/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/
package io.cloudslang.lang.entities;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * @author moradi
 * @since 26/11/2014
 * @version $Id$
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
    private ResultNavigation(){}

    public long getNextStepId() {
		return this.nextStepId;
	}

	public String getPresetResult() {
		return this.presetResult;
	}

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
