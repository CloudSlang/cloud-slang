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
 * @since 06/11/2014
 * @version $Id$
 */
@Component
public class ScriptEvaluator {

	private static final String TRUE = "true";
	private static final String FALSE = "false";

    @Autowired
    private ScriptEngine engine;

    public Serializable evalExpr(String expr, Map<String, ? extends Serializable> context) {
        ScriptContext scriptContext = new SimpleScriptContext();
        for(Map.Entry<String, ? extends Serializable> entry:context.entrySet()){
            scriptContext.setAttribute(entry.getKey(), entry.getValue(), ScriptContext.ENGINE_SCOPE);
        }
		if(scriptContext.getAttribute(TRUE) == null) scriptContext.setAttribute(TRUE, Boolean.TRUE, ScriptContext.ENGINE_SCOPE);
		if(scriptContext.getAttribute(FALSE) == null) scriptContext.setAttribute(FALSE, Boolean.FALSE, ScriptContext.ENGINE_SCOPE);
        try {
            return (Serializable) engine.eval(expr,scriptContext);
        } catch (ScriptException e) {
            ScriptException scriptException =  new ScriptException(e);
            throw new RuntimeException(
                    "Error in running script expression or variable reference, for expression: '"
                    + expr + "',\n\tScript exception is: " + scriptException.getMessage(), scriptException);
        }
    }

}
