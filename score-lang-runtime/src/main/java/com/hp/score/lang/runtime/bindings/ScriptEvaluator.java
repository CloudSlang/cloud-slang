package com.hp.score.lang.runtime.bindings;/*
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import java.io.Serializable;
import java.util.Map;

/**
 * User: stoneo
 * Date: 06/11/2014
 * Time: 08:59
 */
@Component
public class ScriptEvaluator {

    @Autowired
    private ScriptEngine engine;

    public Serializable evalExpr(String expr, Map<String, ? extends Serializable> context) {
        ScriptContext scriptContext = new SimpleScriptContext();
        for(Map.Entry<String, ? extends Serializable> entry:context.entrySet()){
            scriptContext.setAttribute(entry.getKey(), entry.getValue(), ScriptContext.ENGINE_SCOPE);
        }
        try {
            return (Serializable)engine.eval(expr,scriptContext);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
        return null;
    }
}
