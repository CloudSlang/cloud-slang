/**
 * ****************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 * <p/>
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * *****************************************************************************
 */
package io.cloudslang.lang.entities.bindings.values;

/**
 * PyObject value
 * <p>
 * Created by Ifat Gavish on 19/04/2016
 */
public interface PyObjectValue extends Value {

    boolean isAccessed();
}
