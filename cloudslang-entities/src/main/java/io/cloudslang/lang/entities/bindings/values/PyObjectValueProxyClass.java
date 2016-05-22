package io.cloudslang.lang.entities.bindings.values;

import java.lang.reflect.Constructor;
import java.util.Arrays;

/**
 * PyObject value proxy class
 *
 * Created by Ifat Gavish on 19/05/2016
 */
public class PyObjectValueProxyClass {

    private Class proxyClass;
    private Constructor<?> constructor;
    private Object[] params;

    public PyObjectValueProxyClass(Class proxyClass, Constructor<?> constructor, Object[] params) {
        this.proxyClass = proxyClass;
        this.constructor = constructor;
        this.params = params;
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
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
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

