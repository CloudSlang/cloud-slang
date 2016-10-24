/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.steps;


import com.hp.oo.sdk.content.annotations.Param;
import com.hp.oo.sdk.content.plugin.GlobalSessionObject;
import com.hp.oo.sdk.content.plugin.SerializableSessionObject;
import com.hp.oo.sdk.content.plugin.SessionResource;

import java.io.Serializable;
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
    public Map<String, Serializable> doJavaNumbersAction(
            @Param("port") Integer port) {
        Map<String, Serializable> returnValues = new HashMap<>();
        returnValues.put("port", port);
        return returnValues;
    }

    @SuppressWarnings("unused")
    public void doJavaActionWrongReturnType() {
    }

    @SuppressWarnings("unused")
    public void doJavaActionExceptionMethod() {
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
    public Map<String, String> getNameFromNonSerializableSession(
            @Param("name") GlobalSessionObject<NonSerializableObject> name) {
        SessionResource<NonSerializableObject> sessionName = name.getResource();
        Map<String, String> returnMap = new HashMap<>();
        String value = null;
        if (sessionName != null && sessionName.get() != null) {
            value = sessionName.get().getName();
        }
        returnMap.put("name", value);
        return returnMap;
    }

    @SuppressWarnings("unused")
    public Map<String, String> setNameOnNonSerializableSession(
            @Param("name") GlobalSessionObject<NonSerializableObject> name,
                                                               @Param("value") String value) {
        name.getResource();
        Map<String, String> returnMap = new HashMap<>();
        name.setResource(new NonSerializableSessionResource(new NonSerializableObject(value)));
        returnMap.put("name", value);
        return returnMap;
    }

    @SuppressWarnings("unused")
    public Map<String, String> getNameFromSerializableSession(@Param("name") SerializableSessionObject name) {
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
