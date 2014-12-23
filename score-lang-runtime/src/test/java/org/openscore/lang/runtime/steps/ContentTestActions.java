package org.openscore.lang.runtime.steps;
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

import com.hp.oo.sdk.content.annotations.Param;
import com.hp.oo.sdk.content.plugin.GlobalSessionObject;
import com.hp.oo.sdk.content.plugin.SerializableSessionObject;
import com.hp.oo.sdk.content.plugin.SessionResource;

import java.util.HashMap;
import java.util.Map;

/**
 * User: stoneo
 * Date: 16/11/2014
 * Time: 16:03
 */
public class ContentTestActions {

    @SuppressWarnings("unused")
    public Map<String, String> doJavaSampleAction(
            @Param("name") String name,
            @Param("role") String role) {
        Map<String, String> returnValues = new HashMap<>();
        returnValues.put("name", name);
        returnValues.put("role", role);
        return returnValues;
    }

    @SuppressWarnings("unused")
    public Map<String, String> doJavaNumberAsString(
            @Param("port") String port) {
        Map<String, String> returnValues = new HashMap<>();
        returnValues.put("port", port);
        return returnValues;
    }

    @SuppressWarnings("unused")
    public void doJavaActionWrongReturnType(){
    }

    @SuppressWarnings("unused")
    public void doJavaActionExceptionMethod(){
        throw new RuntimeException("Error");
    }

    @SuppressWarnings("unused")
    public Map<String, String> doJavaActionMissingAnnotation(@Param("name") String name, String role) {
        Map<String, String> returnValues = new HashMap<>();
        returnValues.put("name", name);
        returnValues.put("role", role);
        return returnValues;
    }

    @SuppressWarnings("unused")
    public Map<String, String> getNameFromNonSerializableSession(@Param("name") GlobalSessionObject<NonSerializableObject> name){
        SessionResource<NonSerializableObject> sessionName = name.getResource();
        Map<String, String> returnMap = new HashMap<>();
        String value = null;
        if(sessionName != null && sessionName.get() != null){
            value = sessionName.get().getName();
        }
        returnMap.put("name", value);
        return returnMap;
    }

    @SuppressWarnings("unused")
    public Map<String, String> setNameOnNonSerializableSession(@Param("name") GlobalSessionObject<NonSerializableObject> name,
                                                               @Param("value") String value){
        SessionResource<NonSerializableObject> sessionName = name.getResource();
        Map<String, String> returnMap = new HashMap<>();
        name.setResource(new NonSerializableSessionResource(new NonSerializableObject(value)));
        returnMap.put("name", value);
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
