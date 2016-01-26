/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
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
import org.apache.commons.lang3.StringUtils;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;

/**
 * @author stoneo
 * @version $Id$
 * @since 06/11/2014
 */
@Component
public class ScriptEvaluator extends AbstractScriptInterpreter {

    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static String LINE_SEPARATOR = System.lineSeparator();
    private static final String SYSTEM_PROPERTIES_MAP = "__sys_prop__";
    private static final String GET_FUNCTION_DEFINITION =
            "def get(key, default_value):" + LINE_SEPARATOR +
                    "  value = globals().get(key)" + LINE_SEPARATOR +
                    "  return default_value if value is None else value";
    private static final String GET_SP_FUNCTION_DEFINITION =
            "def get_sp(key, default_value=None):" + LINE_SEPARATOR +
                    "  property_value = __sys_prop__.get(key)" + LINE_SEPARATOR +
                    "  return default_value if property_value is None else property_value";

    @Autowired
    @Qualifier("evalInterpreter")
    private PythonInterpreter interpreter;

    public Serializable evalExpr(
            String expr,
            Map<String, ? extends Serializable> context,
            Set<SystemProperty> systemProperties){
        return evalExpr(expr, context, systemProperties, new HashSet<ScriptFunction>());
    }

    //we need this method to be synchronized so we will not have multiple scripts run in parallel on the same context
    public synchronized Serializable evalExpr(
            String expr,
            Map<String, ? extends Serializable> context,
            Set<SystemProperty> systemProperties,
            Set<ScriptFunction> functionDependencies) {
        try {
            cleanInterpreter(interpreter);
            prepareInterpreterContext(context);
            addFunctionsToContext(systemProperties, functionDependencies);
            return eval(interpreter, expr);
        } catch (Exception exception) {
            throw new RuntimeException(
                    "Error in running script expression: '"
                            + expr + "',\n\tException is: " + exception.getMessage(), exception);
        }
    }

    private void addFunctionsToContext(Set<SystemProperty> systemProperties, Set<ScriptFunction> functionDependencies) {
        String functions = "";
        for (ScriptFunction function : functionDependencies) {
            switch (function) {
                case GET:
                    functions += GET_FUNCTION_DEFINITION;
                    functions = appendDelimiterBetweenFunctions(functions);
                    break;
                case GET_SYSTEM_PROPERTY:
                    interpreter.set(SYSTEM_PROPERTIES_MAP, prepareSystemProperties(systemProperties));
                    functions += GET_SP_FUNCTION_DEFINITION;
                    functions = appendDelimiterBetweenFunctions(functions);
                    break;
            }
        }

        if (StringUtils.isNotEmpty(functions)) {
            functions = trimScript(functions);
            exec(interpreter, functions);
        }
    }

    private void prepareInterpreterContext(Map<String, ? extends Serializable> context) {
        for (Map.Entry<String, ? extends Serializable> entry : context.entrySet()) {
            interpreter.set(entry.getKey(), entry.getValue());
        }
        if (interpreter.get(TRUE) == null)
            interpreter.set(TRUE, Boolean.TRUE);
        if (interpreter.get(FALSE) == null)
            interpreter.set(FALSE, Boolean.FALSE);
    }

    private String appendDelimiterBetweenFunctions(String text) {
        return text + LINE_SEPARATOR + LINE_SEPARATOR;
    }

    private String trimScript(String text) {
        return text.trim();
    }

    private Map<String, String> prepareSystemProperties(Set<SystemProperty> properties) {
        Map<String, String> processedSystemProperties = new HashMap<>();
        for (SystemProperty property : properties) {
            processedSystemProperties.put(property.getFullyQualifiedName(), property.getValue());
        }
        return processedSystemProperties;
    }

}
