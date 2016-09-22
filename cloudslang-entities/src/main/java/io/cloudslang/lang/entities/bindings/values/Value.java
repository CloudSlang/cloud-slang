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

import java.io.Serializable;

/**
 * InOutParam value
 * <p>
 * Created by Ifat Gavish on 19/04/2016
 */
public interface Value extends Serializable {

    Serializable get();

    boolean isSensitive();

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();

    @Override
    String toString();
}
