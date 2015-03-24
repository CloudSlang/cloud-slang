/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/
package io.cloudslang.lang.entities.bindings;

/**
 * @author stoneo
 * @since 06/11/2014
 * @version $Id$
 */
public class Result extends InOutParam {

	private static final long serialVersionUID = -809266116566407854L;

	public Result(String name, String expression) {
		super(name, expression);
	}

    /**
     * only here to satisfy serialization libraries
     */
    private Result(){}

}
