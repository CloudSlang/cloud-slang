/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.systemtests.actions;

import com.hp.oo.sdk.content.annotations.Param;
import com.hp.oo.sdk.content.plugin.GlobalSessionObject;
import com.hp.oo.sdk.content.plugin.SerializableSessionObject;
import com.hp.oo.sdk.content.plugin.SessionResource;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * User: stoneo
 * Date: 07/10/2014
 * Time: 15:03
 */
public class LangTestActions {

    @SuppressWarnings("unused")
    public Map<String, String> parseUrl(@Param("host") String host, @Param("port") String novaPort) {
        String url = "http://" + host + ":" + novaPort;
        System.out.println(url);
        Map<String, String> returnValue = new HashMap<>();
        returnValue.put("url", url);
        return returnValue;
    }

    public Map<String, String> print(@Param("string") String string) {
        System.out.println(string);
        return new HashMap<>();
    }

    @SuppressWarnings("unused")
    public Map<String, Serializable> printAndReturnDur(@Param("string") String string) {
        System.out.println(string);
        HashMap<String, Serializable> outputs = new HashMap<>();
        outputs.put("dur", 120);
        return outputs;
    }

    @SuppressWarnings("unused")
    public Map<String, String> getConnectionFromNonSerializableSession(
            @Param("connection") GlobalSessionObject<NonSerializableObject> connection) {
        SessionResource<NonSerializableObject> sessionName = connection.getResource();
        Map<String, String> returnMap = new HashMap<>();
        String value = null;
        if (sessionName != null && sessionName.get() != null) {
            value = sessionName.get().getName();
        }
        returnMap.put("connection", value);
        return returnMap;
    }

    @SuppressWarnings("unused")
    public Map<String, String> setConnectionOnNonSerializableSession(
            @Param("connection") GlobalSessionObject<NonSerializableObject> connection,
                                                                     @Param("value") String value) {
        connection.getResource();
        Map<String, String> returnMap = new HashMap<>();
        connection.setResource(new NonSerializableSessionResource(new NonSerializableObject(value)));
        returnMap.put("connection", value);
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
