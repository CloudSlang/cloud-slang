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
import java.lang.reflect.Modifier;

/**
 * PyObjectValue proxy factory
 *
 * Created by Ifat Gavish on 04/05/2016
 */
public class PyObjectValueProxyFactory {

    @SuppressWarnings("unchecked")
    public static PyObjectValue create(Serializable content, boolean sensitive) {
        PyObject pyObject = Py.java2py(content);
        try {
            ProxyFactory factory = new ProxyFactory();
            factory.setSuperclass(pyObject.getClass());
            factory.setInterfaces(new Class[]{PyObjectValue.class});
            factory.setFilter(new PyObjectValueMethodFilter());
            PyObjectConstructor constructor = getConstructor(pyObject);
            MethodHandler methodHandler = new PyObjectValueMethodHandler(content, sensitive, pyObject);
            return (PyObjectValue) factory.create(constructor.constructor.getParameterTypes(), constructor.params, methodHandler);
        } catch (Exception e) {
            // ToDo remove
            System.out.println("\nError in PyObjectValueProxyFactory\n");
            e.printStackTrace();

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
                if (!thisMethod.getName().equals("toString")) {
                    // ToDo remove
                    System.out.println("MethodHandler: " + thisMethod.getName() + ". " + thisMethod.toString());
                    accessed = true;
                }
                return pyObjectMethod.invoke(pyObject, args);
            } else {
                throw new RuntimeException("Failed to invoke PyObjectValue method. Implementing class not found");
            }
        }
    }

    private static class PyObjectValueMethodFilter implements MethodFilter {

        @Override
        public boolean isHandled(Method method) {
            return Modifier.isPublic(method.getModifiers());
        }
    }

    @SuppressWarnings("unchecked")
    private static PyObjectConstructor getConstructor(PyObject pyObject) {
        try {
            PyObjectConstructor pyObjectConstructor = new PyObjectConstructor(PyObject.class.getConstructor(), new Object[]{});
            if (pyObject.getClass().getConstructors().length > 0) {
                Constructor constructor = pyObject.getClass().getConstructors()[0];
                for (Constructor<?> con : pyObject.getClass().getConstructors()) {
                    if (con.getParameterCount() < constructor.getParameterCount()) {
                        constructor = con;
                    }
                }
                Object[] params = new Object[constructor.getParameterCount()];
                for (int index = 0; index < constructor.getParameterCount(); index++) {
                    Class<?> parameterType = constructor.getParameterTypes()[index];
                    params[index] = parameterType.equals(PyType.class) ? pyObject.getType() :
                            !parameterType.isPrimitive() ? null : ClassUtils.primitiveToWrapper(parameterType).getConstructor(String.class).newInstance("0");
                }
                pyObjectConstructor = new PyObjectConstructor(constructor, params);
            }
            return pyObjectConstructor;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get PyObject constructor", e);
        }

    }

    private static class PyObjectConstructor {

        Constructor<?> constructor;
        Object[] params;

        public PyObjectConstructor(Constructor<?> constructor, Object[] params) {
            this.constructor = constructor;
            this.params = params;
        }
    }
}
