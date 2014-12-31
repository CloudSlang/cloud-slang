/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/
package org.openscore.lang.entities;

import java.io.Serializable;

/**
 * @author moradi
 * @since 26/11/2014
 * @version $Id$
 */
public class ResultNavigation implements Serializable {

	private final long nextStepId;
	private final String presetResult;

    public ResultNavigation(long nextStepId, String presetResult) {
        this.nextStepId = nextStepId;
        this.presetResult = presetResult;
    }

    public long getNextStepId() {
		return this.nextStepId;
	}

	public String getPresetResult() {
		return this.presetResult;
	}
}
