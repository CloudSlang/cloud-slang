package io.cloudslang.lang.entities.bindings.values;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import org.apache.commons.lang.ClassUtils;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyType;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * PyObjectValue proxy factory
 *
 * Created by Ifat Gavish on 04/05/2016
 */
public class PyObjectValueProxyFactory {

    public static PyObjectValue create(Serializable content, boolean sensitive) {
        PyObject pyObject = Py.java2py(content);
        MethodHandler methodHandler = new PyObjectValueMethodHandler(content, sensitive, pyObject);
        return pyObject.getClass().getConstructors().length == 0 ?
                createProxyToSingleInstance(pyObject, methodHandler) :
                createProxyToNewInstance(pyObject, methodHandler);
    }

    private static PyObjectValue createProxyToSingleInstance(PyObject pyObject, MethodHandler methodHandler) {
        try {
            ProxyFactory factory = new ProxyFactory();
            factory.setSuperclass(PyObject.class);
            factory.setInterfaces(new Class[]{PyObjectValue.class});
            factory.setFilter(new PyObjectValueMethodFilter());
            return (PyObjectValue) factory.create(new Class[0], new Object[0], methodHandler);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create a proxy to single instance for PyObjectValue and " + pyObject.getClass().getSimpleName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static PyObjectValue createProxyToNewInstance(PyObject pyObject, MethodHandler methodHandler) {
        try {
            Constructor<?> constructor = pyObject.getClass().getConstructors()[0];
            for (Constructor<?> con : pyObject.getClass().getConstructors()) {
                if (con.getParameterCount() < constructor.getParameterCount()) {
                    constructor = con;
                }
            }
            Object[] args = new Object[constructor.getParameterCount()];
            for (int index = 0; index < constructor.getParameterCount(); index++) {
                Class<?> parameterType = constructor.getParameterTypes()[index];
                args[index] = parameterType.equals(PyType.class) ? pyObject.getType() :
                        !parameterType.isPrimitive() ? null : ClassUtils.primitiveToWrapper(parameterType).getConstructor(String.class).newInstance("0");
            }
            ProxyFactory factory = new ProxyFactory();
            factory.setSuperclass(pyObject.getClass());
            factory.setInterfaces(new Class[]{PyObjectValue.class});
            factory.setFilter(new PyObjectValueMethodFilter());
            return (PyObjectValue) factory.create(constructor.getParameterTypes(), args, methodHandler);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create a proxy to new instance for PyObjectValue and " + pyObject.getClass().getSimpleName(), e);
        }
    }

    private static class PyObjectValueMethodHandler implements MethodHandler, Serializable {

        private Value value;
        private PyObject pyObject;
        private boolean accessed;

        public PyObjectValueMethodHandler(Serializable content, boolean sensitive, PyObject pyObject) {
            this.value = ValueFactory.create(content, sensitive);
            this.pyObject = pyObject;
            this.accessed = false;
        }

        @Override
        public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
            if (thisMethod.getName().equals("isAccessed")) {
                return accessed;
            } else if (thisMethod.getDeclaringClass().isAssignableFrom(Value.class)) {
                Method valueMethod = value.getClass().getMethod(thisMethod.getName(), thisMethod.getParameterTypes());
                return valueMethod.invoke(value, args);
            } else if (PyObject.class.isAssignableFrom(thisMethod.getDeclaringClass())) {
                Method pyObjectMethod = pyObject.getClass().getMethod(thisMethod.getName(), thisMethod.getParameterTypes());
                System.out.println("MethodHandler: " + thisMethod.getName() + ". " + thisMethod.toString());
                accessed = true;
                return pyObjectMethod.invoke(pyObject, args);
            } else {
                throw new RuntimeException("Failed to invoke PyObjectValue method. Implementing class not found");
            }
        }
    }

    private static class PyObjectValueMethodFilter implements MethodFilter {

        @Override
        public boolean isHandled(Method method) {
            return !method.getName().equals("finalize") && !method.getName().equals("toString");
        }
    }
}
