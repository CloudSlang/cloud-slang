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
package com.hp.score.lang.tests.operation.actions;

import com.hp.oo.sdk.content.annotations.Param;
import com.hp.oo.sdk.content.plugin.GlobalSessionObject;
import com.hp.oo.sdk.content.plugin.SerializableSessionObject;
import com.hp.oo.sdk.content.plugin.SessionResource;

import java.util.HashMap;
import java.util.Map;

/**
 * User: stoneo
 * Date: 07/10/2014
 * Time: 15:03
 */
public class LangTestActions {

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public Map<String, String> printAndReturnDur(@Param("string")String string){
        System.out.println(string);
        HashMap<String, String> outputs = new HashMap<>();
        outputs.put("dur", "120 ms");
        return outputs;
    }

    @SuppressWarnings("unused")
    public Map<String, String> getConnectionFromNonSerializableSession(@Param("connection") GlobalSessionObject<NonSerializableObject> connection){
        SessionResource<NonSerializableObject> sessionName = connection.getResource();
        Map<String, String> returnMap = new HashMap<>();
        String value = null;
        if(sessionName != null && sessionName.get() != null){
            value = sessionName.get().getName();
        }
        returnMap.put("connection", value);
        return returnMap;
    }

    @SuppressWarnings("unused")
    public Map<String, String> setConnectionOnNonSerializableSession(@Param("connection") GlobalSessionObject<NonSerializableObject> connection,
                                                                     @Param("value") String value){
        SessionResource<NonSerializableObject> sessionName = connection.getResource();
        Map<String, String> returnMap = new HashMap<>();
        connection.setResource(new NonSerializableSessionResource(new NonSerializableObject(value)));
        returnMap.put("connection", value);
        return returnMap;
    }

    @SuppressWarnings("unused")
    public Map<String, String> getNameFromSerializableSession(@Param("name") SerializableSessionObject name){
        String sessionName = name.getName();
        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("name", sessionName);
        return returnMap;
    }

    static class NonSerializableSessionResource extends SessionResource<NonSerializableObject> {
        private NonSerializableObject value;

        public NonSerializableSessionResource(NonSerializableObject value) {
            this.value = value;
        }

        @Override
        public NonSerializableObject get() {
            return value;
        }

        @Override
        public void release() {
            value = null;
        }
    }

    static class NonSerializableObject {
        private String name;

        NonSerializableObject(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
