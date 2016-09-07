package io.cloudslang.lang.compiler.modeller.model;/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/


import java.util.Deque;

/*
 * Created by orius123 on 06/11/14.
 */
public class Workflow {

    private final Deque<Step> steps;

    public Workflow(Deque<Step> steps) {
        this.steps = steps;
    }

    public Deque<Step> getSteps() {
        return steps;
    }
}
