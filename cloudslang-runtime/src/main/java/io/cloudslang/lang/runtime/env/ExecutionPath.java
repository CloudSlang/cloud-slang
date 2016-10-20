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

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import org.apache.commons.lang3.StringUtils;

import static org.apache.commons.lang3.StringUtils.join;

/**
 * @author moradi
 * @since 06/11/2014
 */
public class ExecutionPath implements Serializable {

    public static final String PATH_SEPARATOR = ".";

    private Deque<Integer> parentPositions;
    private int position;

    public ExecutionPath() {
        parentPositions = new ArrayDeque<>();
    }

    public void forward() {
        position++;
    }

    public void down() {
        parentPositions.push(position);
        position = 0;
    }

    public void up() {
        position = parentPositions.pop();
    }

    public String getParentPath() {
        return join(parentPositions.descendingIterator(), PATH_SEPARATOR);
    }

    public String getCurrentPath() {
        return getCurrentPath(position);
    }

    private String getCurrentPath(int position) {
        String parents = getParentPath();
        return StringUtils.isEmpty(parents) ? position + "" : parents + PATH_SEPARATOR + position;
    }



}
