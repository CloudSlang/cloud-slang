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
import java.util.List;
import java.util.Map;

/**
 * @author skrivorutchenko
 * @version $Id$
 * @since 02/10/2019
 */
public class NavigationOptions implements Serializable {

    private long nextStepId;
    private long currStepId;
    private List<Map<String, Serializable>> options;

    public NavigationOptions(long currStepId, long nextStepId, List<Map<String, Serializable>> options) {
        this.currStepId = currStepId;
        this.nextStepId = nextStepId;
        this.options = options;
    }

    /**
     * only here to satisfy serialization libraries
     */
    private NavigationOptions() {
    }

    public long getCurrStepId() {
        return this.currStepId;
    }

    public long getNextStepId() {
        return this.nextStepId;
    }

    public List<Map<String, Serializable>> getOptions() {
        return this.options;
    }

}
