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
import java.util.Map;

/**
 * @author skrivorutchenko
 * @version $Id$
 * @since 02/10/2019
 */
public class NavigationOptions implements Serializable {

    private String name;
    private Map<String, Serializable> options;
    private long nextStepId;
    private long currStepId;

    public NavigationOptions(String name, Map<String, Serializable> options, long currStepId, long nextStepId) {
        this.name = name;
        this.options = options;
        this.currStepId = currStepId;
        this.nextStepId = nextStepId;
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

    public String getName() {
        return this.name;
    }

    public Map<String, Serializable> getOptions() {
        return this.options;
    }

}
