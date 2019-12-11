/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.entities.bindings.values;

import javassist.util.proxy.MethodFilter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class PublicMethodFilter implements MethodFilter {
    @Override
    public boolean isHandled(Method method) {
        return Modifier.isPublic(method.getModifiers());
    }
}
