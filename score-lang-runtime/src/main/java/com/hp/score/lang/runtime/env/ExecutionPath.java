/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package com.hp.score.lang.runtime.env;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * @author moradi
 * @since 06/11/2014
 * @version $Id$
 */
public class ExecutionPath implements Serializable {

	private static final long serialVersionUID = 5536588094244112461L;

	private Deque<Integer> parentPositions;
	private int position;

	public ExecutionPath() {
		parentPositions = new ArrayDeque<>();
	}

	public int forward() {
		return position++;
	}

	public int down() {
		parentPositions.push(position);
		position = 0;
		return position;
	}

	public int up() {
		position = parentPositions.pop();
		return position;
	}

	public String getCurrentPath() {
		StringBuilder result = new StringBuilder();
		for(Iterator<Integer> iterator = parentPositions.descendingIterator(); iterator.hasNext();) {
			result.append(iterator.next()).append("/");
		}
		result.append(position);
		return result.toString();
	}

}
