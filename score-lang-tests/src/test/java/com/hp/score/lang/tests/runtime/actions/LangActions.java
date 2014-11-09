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
package com.hp.score.lang.tests.runtime.actions;

import com.hp.oo.sdk.content.annotations.Param;

import java.util.HashMap;
import java.util.Map;

/**
 * User: stoneo
 * Date: 07/10/2014
 * Time: 15:03
 */
public class LangActions {

    public Map<String, String> parseUrl(@Param("host")String host, @Param("port")String nova_port){
        String url = "http://" + host + ":" + nova_port;
        System.out.println(url);
        Map<String, String> returnValue = new HashMap<>();
        returnValue.put("url", url);
        return returnValue;
    }

    public Map<String, String> print(@Param("string")String string){
        System.out.println(string);
        return new HashMap<>();
    }

    public Map<String, String> printAndReturnDur(@Param("string")String string){
        System.out.println(string);
        HashMap<String, String> outputs = new HashMap<>();
        outputs.put("dur", "120 ms");
        return outputs;
    }
}
