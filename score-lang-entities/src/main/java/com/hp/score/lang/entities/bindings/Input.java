package com.hp.score.lang.entities.bindings;
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

import java.io.Serializable;

/*
 * Created by orius123 on 05/11/14.
 */
public class Input {

    private final String name;

    private final Serializable defaultValue;

    private final String expression ;

    private final boolean encrypted;

    private final boolean required;

    public Input(String name,String expression) {
        this.name = name;
        this.defaultValue = null;
        this.expression = expression;
        this.encrypted = false ;
        this.required = true ;
    }

    public Input(String name,String expression,Serializable defaultValue,boolean encrypted,boolean required) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.expression = expression;
        this.encrypted = encrypted ;
        this.required = required ;
    }

    public String getName() {
        return name;
    }

    public Serializable getDefaultValue() {
        return defaultValue;
    }

    public String getExpression() {
        return expression;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public boolean isRequired() {
        return required;
    }

}
