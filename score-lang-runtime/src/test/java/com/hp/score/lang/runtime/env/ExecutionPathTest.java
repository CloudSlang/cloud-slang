/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package com.hp.score.lang.runtime.env;

import java.util.NoSuchElementException;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static com.hp.score.lang.runtime.env.ExecutionPath.PATH_SEPARATOR;

/**
 * @author moradi
 * @since 09/11/2014
 * @version $Id$
 */
public class ExecutionPathTest {

	/**
	 * Test method for {@link com.hp.score.lang.runtime.env.ExecutionPath#getCurrentPath()}.
	 */
	@Test
	@SuppressWarnings("static-method")
	public void testCurrentPath() {
		ExecutionPath executionPath = new ExecutionPath();
		StringBuilder expectedPath = new StringBuilder("0");
		doAssert(expectedPath, executionPath);

		executionPath.down(); // 0/0
		executionPath.forward(); // 0/1
		expectedPath.append(PATH_SEPARATOR).append("1");
		doAssert(expectedPath, executionPath);

		executionPath.down(); // 0/1/0
		executionPath.forward(); // 0/1/1
		executionPath.forward(); // 0/1/2
		expectedPath.append(PATH_SEPARATOR).append("2");
		doAssert(expectedPath, executionPath);

		executionPath.up(); // 0/1
		deleteLevel(expectedPath);
		doAssert(expectedPath, executionPath);

		executionPath.forward(); // 0/2
		executionPath.forward(); // 0/3
		deleteLevel(expectedPath);
		expectedPath.append(PATH_SEPARATOR).append("3");
		doAssert(expectedPath, executionPath);

		executionPath.down(); // 0/3/0
		expectedPath.append(PATH_SEPARATOR).append("0");
		executionPath.down(); // 0/3/0/0
		executionPath.forward(); // 0/3/0/1
		expectedPath.append(PATH_SEPARATOR).append("1");
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
		} catch(Exception ex) {
			assertTrue(ex instanceof NoSuchElementException);
		}
		doAssert(expectedPath, executionPath);
	}

	private static void doAssert(StringBuilder expectedPath, ExecutionPath executionPath) {
		assertEquals(expectedPath.toString(), executionPath.getCurrentPath());
	}

	private static void deleteLevel(StringBuilder expectedPath) {
		expectedPath.delete(expectedPath.lastIndexOf(PATH_SEPARATOR), expectedPath.length());
	}

}
