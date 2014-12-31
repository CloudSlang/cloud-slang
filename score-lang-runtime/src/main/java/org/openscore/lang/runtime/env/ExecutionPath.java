/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/
package org.openscore.lang.runtime.env;

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

	public static final String PATH_SEPARATOR = "/";
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

	public int getDepth() {
		return parentPositions.size();
	}

	public String getCurrentPath() {
		StringBuilder result = new StringBuilder();
		for(Iterator<Integer> iterator = parentPositions.descendingIterator(); iterator.hasNext();) {
			result.append(iterator.next()).append(PATH_SEPARATOR);
		}
		result.append(position);
		return result.toString();
	}

}
