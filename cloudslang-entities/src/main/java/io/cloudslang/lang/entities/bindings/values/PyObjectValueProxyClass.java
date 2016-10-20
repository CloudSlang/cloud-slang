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

import java.lang.reflect.Constructor;
import java.util.Arrays;

import static java.util.Arrays.copyOf;

/**
 * PyObject value proxy class
 * <p>
 * Created by Ifat Gavish on 19/05/2016
 */
public class PyObjectValueProxyClass {

    private Class proxyClass;
    private Constructor<?> constructor;
    private Object[] params;

    public PyObjectValueProxyClass(Class proxyClass, Constructor<?> constructor, Object[] params) {
        this.proxyClass = proxyClass;
        this.constructor = constructor;
        this.params = getParamsHelper(params);
    }

    public Class getProxyClass() {
        return proxyClass;
    }

    public void setProxyClass(Class proxyClass) {
        this.proxyClass = proxyClass;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public void setConstructor(Constructor<?> constructor) {
        this.constructor = constructor;
    }

    public Object[] getParams() {
        return getParamsHelper(this.params);
    }

    public void setParams(Object[] params) {
        this.params = getParamsHelper(params);
    }

    private static Object[] getParamsHelper(Object[] params) {
        return (params != null) ? Arrays.copyOf(params, params.length) : null;
    }

    @Override
    public String toString() {
        return "PyObjectValueProxy{" +
                "proxyClass=" + proxyClass +
                ", constructor=" + constructor +
                ", params=" + Arrays.toString(params) +
                '}';
    }
}

