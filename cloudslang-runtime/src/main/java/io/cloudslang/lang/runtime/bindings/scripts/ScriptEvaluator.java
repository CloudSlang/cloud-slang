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
import io.cloudslang.lang.runtime.services.ScriptsService;
import io.cloudslang.runtime.api.python.PythonEvaluationResult;
import io.cloudslang.runtime.api.python.PythonRuntimeService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.python.core.Py;
import org.python.core.PyObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author stoneo
 * @version $Id$
 * @since 06/11/2014
 */
@Component
public class ScriptEvaluator extends ScriptProcessor {

    private static final String LINE_SEPARATOR = "\n";
    private static final String SYSTEM_PROPERTIES_MAP = "sys_prop";
    private static final String ACCESSED_RESOURCES_SET = "accessed_resources_set";
    private static final String ACCESS_MONITORING_METHOD_NAME = "accessed";

    private static final String BACKWARD_COMPATIBLE_ACCESS_METHOD = "def " + ACCESS_MONITORING_METHOD_NAME + "(key):" +
            LINE_SEPARATOR + "  pass";
    private static final boolean EXTERNAL_PYTHON = !Boolean.valueOf(
            System.getProperty("use.jython.expressions", "true"));
    public static final int MAX_LENGTH = Integer.getInteger("input.error.max.length", 1000);

    @Resource(name = "jythonRuntimeService")
    private PythonRuntimeService jythonRuntimeService;

    @Resource(name = "externalPythonRuntimeService")
    private PythonRuntimeService pythonRuntimeService;

    @Autowired
    private ScriptsService scriptsService;

    public Value evalExpr(String expr,
            Map<String, Value> context,
            Set<SystemProperty> systemProperties,
            Set<ScriptFunction> functionDependencies) {

        try {
            Map<String, Serializable> pythonContext = createPythonContext(context, EXTERNAL_PYTHON);
            boolean systemPropertiesDefined = functionDependencies.contains(ScriptFunction.GET_SYSTEM_PROPERTY);
            if (systemPropertiesDefined) {
                pythonContext.put(SYSTEM_PROPERTIES_MAP, prepareSystemProperties(systemProperties, EXTERNAL_PYTHON));
            }

            if (EXTERNAL_PYTHON) {
                PythonEvaluationResult result = pythonRuntimeService.eval(
                        buildFunctionsScriptExternalPython(functionDependencies), expr, pythonContext);

                //noinspection unchecked
                Set<String> accessedResources = (Set<String>) result.getResultContext().get(ACCESSED_RESOURCES_SET);
                return ValueFactory.create(result.getEvalResult(),
                        isAnyAccessedVariableSensitive(pythonContext, accessedResources));
            } else {
                return processJythonEvaluation(expr, pythonContext, systemPropertiesDefined, functionDependencies);
            }
        } catch (Exception exception) {
            throw new RuntimeException("Error in evaluating expression: '" +
                    getTruncatedExpression(expr) + "',\n\t" +
                    handleExceptionSpecialCases(exception.getMessage()), exception);
        }
    }

    public Value testExpr(String expr, Map<String, Value> context, Set<SystemProperty> systemProperties,
                          Set<ScriptFunction> functionDependencies, long timeoutPeriod) {
        try {

            Map<String, Serializable> pythonContext = createPythonContext(context, EXTERNAL_PYTHON);
            boolean systemPropertiesDefined = functionDependencies.contains(ScriptFunction.GET_SYSTEM_PROPERTY);
            if (systemPropertiesDefined) {
                pythonContext.put(SYSTEM_PROPERTIES_MAP, prepareSystemProperties(systemProperties,
                        EXTERNAL_PYTHON));
            }

            if (EXTERNAL_PYTHON) {
                PythonEvaluationResult result = pythonRuntimeService.test(
                        buildFunctionsScriptExternalPython(functionDependencies), expr, pythonContext, timeoutPeriod);

                //noinspection unchecked
                Set<String> accessedResources = (Set<String>) result.getResultContext().get(ACCESSED_RESOURCES_SET);
                return ValueFactory.create(result.getEvalResult(),
                        isAnyAccessedVariableSensitive(pythonContext, accessedResources));
            } else {
                return processJythonExpressionTesting(expr, pythonContext, systemPropertiesDefined,
                        functionDependencies, timeoutPeriod);
            }
        } catch (Exception exception) {
            throw new RuntimeException("Error in evaluating expression: '" +
                    getTruncatedExpression(expr) + "',\n\t" +
                    handleExceptionSpecialCases(exception.getMessage()), exception);
        }
    }

    private String getTruncatedExpression(String expr) {
        return expr.length() > MAX_LENGTH ? expr.substring(0, MAX_LENGTH) + "..." : expr;
    }

    private String buildFunctionsScriptExternalPython(Set<ScriptFunction> functionDependencies) {
        StringBuilder functionsBuilder = new StringBuilder();
        doBuildFunctionsScript(functionDependencies, LINE_SEPARATOR, functionsBuilder);
        return functionsBuilder.toString();
    }

    private String buildFunctionsScriptJython(Set<ScriptFunction> functionDependencies) {
        StringBuilder functionsBuilder = new StringBuilder();
        doBuildFunctionsScript(functionDependencies, LINE_SEPARATOR, functionsBuilder);
        if (!functionDependencies.isEmpty()) {
            functionsBuilder.append(BACKWARD_COMPATIBLE_ACCESS_METHOD)
                    .append(LINE_SEPARATOR)
                    .append(LINE_SEPARATOR);
        }
        return functionsBuilder.toString();
    }

    private void doBuildFunctionsScript(final Set<ScriptFunction> functionDependencies,
            final String newLine,
            final StringBuilder functionsBuilder) {
        for (ScriptFunction function : functionDependencies) {
            functionsBuilder.append(scriptsService.getScript(function))
                    .append(newLine)
                    .append(newLine);
        }
    }

    private HashMap<String, Value> prepareSystemProperties(Set<SystemProperty> properties, boolean externalPython) {
        HashMap<String, Value> processedSystemProperties = new HashMap<>();
        for (SystemProperty property : properties) {
            processedSystemProperties.put(property.getFullyQualifiedName(),
                    ValueFactory.createPyObjectValue(property.getValue(), externalPython));
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

    private boolean isAnyAccessedVariableSensitive(Map<String, Serializable> fullContext, Set<String> accessedVars) {
        //noinspection unchecked
        return !CollectionUtils.isEmpty(accessedVars) && fullContext.entrySet().stream()
                .flatMap(entry ->
                    entry.getValue() instanceof Map ?
                            ((Map<String, Serializable>) entry.getValue()).entrySet().stream()
                            : Stream.of(entry))
                .filter(entry -> accessedVars.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .anyMatch(this::checkSensitivityForValue);
    }

    @Deprecated
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
            if (value instanceof PyObjectValue) {
                PyObjectValue pyObjectValue = (PyObjectValue) value;
                if (pyObjectValue.isSensitive() && pyObjectValue.isAccessed()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkSensitivityForValue(Serializable v) {
        return (v instanceof PyObjectValue) && ((PyObjectValue) v).isSensitive() && ((PyObjectValue) v).isAccessed();
    }

    //region Legacy Content
    private Value processJythonEvaluation(String expr, Map<String, Serializable> pythonContext,
                                                boolean systemPropertiesDefined,
                                                Set<ScriptFunction> functionDependencies) {
        PythonEvaluationResult result = jythonRuntimeService.eval(
                buildFunctionsScriptJython(functionDependencies), expr, pythonContext);
        return ValueFactory.create(result.getEvalResult(), getSensitive(result.getResultContext(),
                systemPropertiesDefined));
    }

    private Value processJythonExpressionTesting(String expr, Map<String, Serializable> pythonContext,
                                                       boolean systemPropertiesDefined,
                                                       Set<ScriptFunction> functionDependencies, long timeoutPeriod) {
        PythonEvaluationResult result = jythonRuntimeService.test(
                buildFunctionsScriptJython(functionDependencies), expr, pythonContext,
                timeoutPeriod);
        return ValueFactory.create(result.getEvalResult(), getSensitive(result.getResultContext(),
                systemPropertiesDefined));
    }
    //endregion
}
