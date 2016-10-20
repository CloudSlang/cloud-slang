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

import java.util.NoSuchElementException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author moradi
 * @version $Id$
 * @since 09/11/2014
 */
public class ExecutionPathTest {

    /**
     * Test method for {@link ExecutionPath#getCurrentPath()}.
     */
    @Test
    @SuppressWarnings("static-method")
    public void testCurrentPath() {
        ExecutionPath executionPath = new ExecutionPath();
        StringBuilder expectedPath = new StringBuilder("0");
        doAssert(expectedPath, executionPath);

        executionPath.down(); // 0/0
        executionPath.forward(); // 0/1
        expectedPath.append(ExecutionPath.PATH_SEPARATOR).append("1");
        doAssert(expectedPath, executionPath);

        executionPath.down(); // 0/1/0
        executionPath.forward(); // 0/1/1
        executionPath.forward(); // 0/1/2
        expectedPath.append(ExecutionPath.PATH_SEPARATOR).append("2");
        doAssert(expectedPath, executionPath);

        executionPath.up(); // 0/1
        deleteLevel(expectedPath);
        doAssert(expectedPath, executionPath);

        executionPath.forward(); // 0/2
        executionPath.forward(); // 0/3
        deleteLevel(expectedPath);
        expectedPath.append(ExecutionPath.PATH_SEPARATOR).append("3");
        doAssert(expectedPath, executionPath);

        executionPath.down(); // 0/3/0
        expectedPath.append(ExecutionPath.PATH_SEPARATOR).append("0");
        executionPath.down(); // 0/3/0/0
        executionPath.forward(); // 0/3/0/1
        expectedPath.append(ExecutionPath.PATH_SEPARATOR).append("1");
        doAssert(expectedPath, executionPath);

        executionPath.up(); // 0/3/0
        deleteLevel(expectedPath);
        executionPath.up(); // 0/3
        deleteLevel(expectedPath);
        executionPath.up(); // 0
        deleteLevel(expectedPath);
        doAssert(expectedPath, executionPath);

        try {
            executionPath.up(); // 0
        } catch (Exception ex) {
            assertTrue(ex instanceof NoSuchElementException);
        }
        doAssert(expectedPath, executionPath);
    }

    private static void doAssert(StringBuilder expectedPath, ExecutionPath executionPath) {
        assertEquals(expectedPath.toString(), executionPath.getCurrentPath());
    }

    private static void deleteLevel(StringBuilder expectedPath) {
        expectedPath.delete(expectedPath.lastIndexOf(ExecutionPath.PATH_SEPARATOR), expectedPath.length());
    }

}
