/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.bindings.scripts;

import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.ScriptFunction;
import io.cloudslang.lang.entities.bindings.values.PyObjectValue;
import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.runtime.api.python.PythonEvaluationResult;
import io.cloudslang.runtime.api.python.PythonRuntimeService;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.python.core.Py;
import org.python.core.PyObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author stoneo
 * @version $Id$
 * @since 06/11/2014
 */
@Component
public class ScriptEvaluator extends ScriptProcessor {
    private static String LINE_SEPARATOR = System.lineSeparator();
    private static final String SYSTEM_PROPERTIES_MAP = "sys_prop";
    private static final String GET_FUNCTION_DEFINITION =
            "def get(key, default_value=None):" + LINE_SEPARATOR +
                    "  value = globals().get(key)" + LINE_SEPARATOR +
                    "  return default_value if value is None else value";
    private static final String GET_SP_FUNCTION_DEFINITION =
            "def get_sp(key, default_value=None):" + LINE_SEPARATOR +
                    "  property_value = " + SYSTEM_PROPERTIES_MAP + ".get(key)" + LINE_SEPARATOR +
                    "  return default_value if property_value is None else property_value";
    private static final String CHECK_EMPTY_FUNCTION_DEFINITION =
            "def check_empty(value_to_check, default_value=None):" + LINE_SEPARATOR +
                    "  return default_value if value_to_check is None else value_to_check";

    @Autowired
    private PythonRuntimeService pythonRuntimeService;

    public Value evalExpr(String expr, Map<String, Value> context, Set<SystemProperty> systemProperties,
                          Set<ScriptFunction> functionDependencies) {
        try {
            Map<String, Serializable> pythonContext = createPythonContext(context);
            boolean systemPropertiesDefined = functionDependencies.contains(ScriptFunction.GET_SYSTEM_PROPERTY);
            if (systemPropertiesDefined) {
                pythonContext.put(SYSTEM_PROPERTIES_MAP, (Serializable) prepareSystemProperties(systemProperties));
            }
            PythonEvaluationResult result = pythonRuntimeService.eval(
                    buildAddFunctionsScript(functionDependencies), expr, pythonContext);
            if (systemPropertiesDefined) {
                pythonContext.remove(SYSTEM_PROPERTIES_MAP);
            }

            return ValueFactory.create(result.getEvalResult(),
                    getSensitive(result.getResultContext(), systemPropertiesDefined));
        } catch (Exception exception) {
            throw new RuntimeException("Error in running script expression: '" +
                            expr + "',\n\tException is: " +
                    handleExceptionSpecialCases(exception.getMessage()), exception);
        }
    }

    private String buildAddFunctionsScript(Set<ScriptFunction> functionDependencies) {
        String functions = "";
        for (ScriptFunction function : functionDependencies) {
            switch (function) {
                case GET:
                    functions += GET_FUNCTION_DEFINITION;
                    functions = appendDelimiterBetweenFunctions(functions);
                    break;
                case GET_SYSTEM_PROPERTY:
                    functions += GET_SP_FUNCTION_DEFINITION;
                    functions = appendDelimiterBetweenFunctions(functions);
                    break;
                case CHECK_EMPTY:
                    functions += CHECK_EMPTY_FUNCTION_DEFINITION;
                    functions = appendDelimiterBetweenFunctions(functions);
                    break;
                default:
                    throw new RuntimeException("Error adding function to context: '" + function.getValue() +
                            "' is not valid.");
            }
        }
        return functions;
    }

    private String appendDelimiterBetweenFunctions(String text) {
        return text + LINE_SEPARATOR + LINE_SEPARATOR;
    }

    private Map<String, Value> prepareSystemProperties(Set<SystemProperty> properties) {
        Map<String, Value> processedSystemProperties = new HashMap<>();
        for (SystemProperty property : properties) {
            processedSystemProperties.put(property.getFullyQualifiedName(),
                    ValueFactory.createPyObjectValue(property.getValue()));
        }
        return processedSystemProperties;
    }

    private String handleExceptionSpecialCases(String message) {
        String processedMessage = message;
        if (StringUtils.isNotEmpty(message) && message.contains("get_sp") && message.contains("not defined")) {
            processedMessage = message + ". Make sure to use correct syntax for the function:" +
                    " get_sp('fully.qualified.name', optional_default_value).";
        }
        return processedMessage;
    }

    private boolean getSensitive(Map<String, Serializable> executionResultContext, boolean systemPropertiesInContext) {
        if (systemPropertiesInContext) {
            Map<String, Serializable> context = new HashMap<>(executionResultContext);
            PyObject rawSystemProperties = (PyObject) context.remove(SYSTEM_PROPERTIES_MAP);
            @SuppressWarnings("unchecked")
            Map<String, Value> systemProperties = Py.tojava(rawSystemProperties, Map.class);
            @SuppressWarnings("unchecked")
            Collection<Serializable> systemPropertyValues = (Collection) systemProperties.values();
            return checkSensitivity(systemPropertyValues) || checkSensitivity(context.values());
        } else {
            return (checkSensitivity(executionResultContext.values()));
        }
    }

    private boolean checkSensitivity(Collection<Serializable> values) {
        for (Serializable value : values) {
            if (value != null && value instanceof PyObjectValue) {
                PyObjectValue pyObjectValue = (PyObjectValue) value;
                if (pyObjectValue.isSensitive() && pyObjectValue.isAccessed()) {
                    return true;
                }
            }
        }
        return false;
    }
}
