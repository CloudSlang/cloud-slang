/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.lang.runtime.bindings;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import java.io.Serializable;
import java.util.Map;

/**
 * @author stoneo
 * @version $Id$
 * @since 06/11/2014
 */
@Component
public class ScriptEvaluator {

    private static final String TRUE = "true";
    private static final String FALSE = "false";

    @Autowired
    private ScriptEngine engine;

    public Serializable evalExpr(
            String expr,
            Map<String, ? extends Serializable> context,
            Map<String, String> systemProperties) {
        ScriptContext scriptContext = new SimpleScriptContext();
        for (Map.Entry<String, ? extends Serializable> entry : context.entrySet()) {
            scriptContext.setAttribute(entry.getKey(), entry.getValue(), ScriptContext.ENGINE_SCOPE);
        }
        if (scriptContext.getAttribute(TRUE) == null)
            scriptContext.setAttribute(TRUE, Boolean.TRUE, ScriptContext.ENGINE_SCOPE);
        if (scriptContext.getAttribute(FALSE) == null)
            scriptContext.setAttribute(FALSE, Boolean.FALSE, ScriptContext.ENGINE_SCOPE);
        try {
            engine.eval(
                    "def get(key, default_value):\n" +
                            "  value = globals().get(key)\n" +
                            "  return value if value is not None else default_value",
                    scriptContext
            );

            String systemPropertyPairs = "";
            for (Map.Entry<String, ? extends Serializable> systemProperty : systemProperties.entrySet()) {
                String value = (String) systemProperty.getValue();
                String valueForPython = value == null ? "None" : "'" + value + "'";
                systemPropertyPairs += "    '" + systemProperty.getKey() + "': " + valueForPython + ", \n";
            }
            if (StringUtils.isNotEmpty(systemPropertyPairs)) {
                systemPropertyPairs = systemPropertyPairs.substring(0, systemPropertyPairs.length() - 3);
            }
            engine.eval(
                    "def get_system_property(key, default_value = None):\n" +
                            "  system_properties = {\n" +
                            systemPropertyPairs + "\n" +
                            "  }\n" +
                            "  property_value = system_properties.get(key)\n" +
                            "  return default_value if property_value is None else property_value"
                    , scriptContext
            );

            Serializable evalResult;
            evalResult = (Serializable) engine.eval(expr, scriptContext);
            return evalResult;
        } catch (ScriptException scriptException) {
            throw new RuntimeException(
                    "Error in running script expression or variable reference, for expression: '"
                            + expr + "',\n\tScript exception is: " + scriptException.getMessage(), scriptException);
        }
    }

}
