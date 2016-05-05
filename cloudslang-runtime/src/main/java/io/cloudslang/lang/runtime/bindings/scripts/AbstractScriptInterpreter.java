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
package io.cloudslang.lang.runtime.bindings.scripts;

import io.cloudslang.lang.entities.bindings.values.PyObjectValue;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import org.python.core.Py;
import org.python.core.PyBoolean;
import org.python.core.PyException;
import org.python.core.PyFile;
import org.python.core.PyFunction;
import org.python.core.PyModule;
import org.python.core.PyObject;
import org.python.core.PyStringMap;
import org.python.core.PySystemState;
import org.python.core.PyType;
import org.python.util.PythonInterpreter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Bonczidai Levente
 * @since 1/19/2016
 */
public abstract class AbstractScriptInterpreter {

    protected void cleanInterpreter(PythonInterpreter interpreter) {
        interpreter.setLocals(new PyStringMap());
    }

    protected Map<String, Value> exec(PythonInterpreter interpreter, String script) {
        interpreter.exec(script);
        boolean sensitive = getSensitive(interpreter);
        return getReturnValues(interpreter, sensitive);
    }

    private boolean getSensitive(PythonInterpreter interpreter) {
        for (PyObject pyObject : interpreter.getLocals().asIterable()) {
            String key = pyObject.asString();
            PyObject value = interpreter.get(key);
            if (value != null && value instanceof PyObjectValue) {
                PyObjectValue pyObjectValue = (PyObjectValue) value;
                if (pyObjectValue.isSensitive() && pyObjectValue.isAccessed()) {
                    return true;
                }
            }
        }
        return false;
    }

    private Map<String, Value> getReturnValues(PythonInterpreter interpreter, boolean sensitive) {
        Iterator<PyObject> localsIterator = interpreter.getLocals().asIterable().iterator();
        Map<String, Value> returnValues = new HashMap<>();
        while (localsIterator.hasNext()) {
            String key = localsIterator.next().asString();
            PyObject value = interpreter.get(key);
            if (keyIsExcluded(key, value)) {
                continue;
            }
            Value javaValue = resolveJythonObjectToJavaExec(value, key, sensitive);
            returnValues.put(key, javaValue);
        }
        return returnValues;
    }

    protected Value eval(PythonInterpreter interpreter, String script) {
        PyObject evalResultAsPyObject = interpreter.eval(script);
        boolean sensitive = getSensitive(interpreter);
        return resolveJythonObjectToJavaEval(evalResultAsPyObject, sensitive, script);
    }

    private Value resolveJythonObjectToJavaExec(PyObject value, String key, boolean sensitive) {
        String errorMessage =
                "Non-serializable values are not allowed in the output context of a Python script:\n" +
                        "\tConversion failed for '" + key + "' (" + getValue(value) + "),\n" +
                        "\tThe error can be solved by removing the variable from the context in the script: e.g. 'del " + key + "'.\n";
        return resolveJythonObjectToJava(value, sensitive, errorMessage);
    }

    private Value resolveJythonObjectToJavaEval(PyObject value, boolean sensitive, String expression) {
        String errorMessage =
                "Evaluation result for a Python expression should be serializable:\n" +
                        "\tConversion failed for '" + expression + "' (" + getValue(value) + ").\n";
        return resolveJythonObjectToJava(value, sensitive, errorMessage);
    }

    private Object getValue(PyObject value) {
        return value != null && value instanceof Value ? ((Value)value).get() : value;
    }

    private Value resolveJythonObjectToJava(PyObject value, boolean sensitive, String errorMessage) {
        if (value == null) {
            return null;
        }
        if (value instanceof Value) {
            return ValueFactory.create(((Value)value).get(), ((Value)value).isSensitive() || sensitive);
        }
        if (value instanceof PyBoolean) {
            PyBoolean pyBoolean = (PyBoolean) value;
            return ValueFactory.create(pyBoolean.getBooleanValue(), sensitive);
        }
        try {
            return ValueFactory.create(Py.tojava(value, Serializable.class), sensitive);
        } catch (PyException e) {
            PyObject typeObject = e.type;
            if (typeObject instanceof PyType) {
                PyType type = (PyType) typeObject;
                String typeName = type.getName();
                if ("TypeError".equals(typeName)) {
                    throw new RuntimeException(errorMessage, e);
                }
            }
            throw e;
        }
    }

    private boolean keyIsExcluded(String key, PyObject value) {
        return (key.startsWith("__") && key.endsWith("__")) ||
                value instanceof PyFile ||
                value instanceof PyModule ||
                value instanceof PyFunction ||
                value instanceof PySystemState;
    }
}
