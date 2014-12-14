/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package com.hp.score.lang.runtime.bindings;

import org.apache.log4j.Logger;
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
    private static final Logger logger = Logger.getLogger(ScriptEvaluator.class);

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
            return (Serializable)engine.eval(expr,scriptContext);
        } catch (ScriptException e) {
            logger.debug("Error in running script expression or variable reference ,for expression: " + expr
                    + " Script exception is: " + e.getMessage());
            //todo - add event here?
        }
        return null;
    }

}
