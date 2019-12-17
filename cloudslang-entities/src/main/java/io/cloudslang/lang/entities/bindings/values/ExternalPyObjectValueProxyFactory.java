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

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

import java.io.Serializable;
import java.lang.reflect.Method;

public class ExternalPyObjectValueProxyFactory {

    public static PyObjectValue create(Serializable content, boolean sensitive) {
        ExternalPyString pyString = new ExternalPyString(content.toString());
        try {

            Class proxyClass = createProxyClass();
            PyObjectValue pyObjectProxy = (PyObjectValue) proxyClass.newInstance();
            ((Proxy) pyObjectProxy).setHandler(new ExternalPyObjectValueMethodHandler(content, sensitive, pyString));
            return pyObjectProxy;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create a proxy to new instance for PyObjectValue and " +
                    content.getClass().getSimpleName(), e);
        }
    }

    private static Class createProxyClass() {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setInterfaces(new Class[]{PyObjectValue.class});
        proxyFactory.setSuperclass(ExternalPyString.class);
        proxyFactory.setFilter(new PublicMethodFilter());
        proxyFactory.setUseWriteReplace(false);
        return proxyFactory.createClass();
    }

    private static class ExternalPyObjectValueMethodHandler implements MethodHandler, Serializable {
        private static final long serialVersionUID = 6564597653476264253L;

        private static final String ACCESSED_GETTER_METHOD = "isAccessed";

        protected Value value;
        private boolean accessed;
        private ExternalPyString pyString;

        public ExternalPyObjectValueMethodHandler(Serializable value, boolean sensitive, ExternalPyString pyString) {
            this.value = ValueFactory.create(value, sensitive);
            this.pyString = pyString;
            this.accessed = false;
        }

        @Override
        public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
            if (thisMethod.getName().equals(ACCESSED_GETTER_METHOD)) {
                return accessed;
            }
            if (ExternalPyString.class.isAssignableFrom(thisMethod.getDeclaringClass())) {
                Method objectValueMethod = pyString.getClass().getMethod(thisMethod.getName(),
                        thisMethod.getParameterTypes());
                if (!thisMethod.getName().equals("toString")) {
                    accessed = true;
                }
                return objectValueMethod.invoke(pyString, args);
            } else if (Value.class.isAssignableFrom(thisMethod.getDeclaringClass())) {
                Method valueMethod = value.getClass().getMethod(thisMethod.getName(), thisMethod.getParameterTypes());
                return valueMethod.invoke(value, args);
            } else {
                throw new RuntimeException("Failed to invoke PyObjectValue method. Implementing class not found");
            }
        }
    }
}
