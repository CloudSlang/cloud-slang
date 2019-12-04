/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller;

import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import io.cloudslang.lang.compiler.modeller.transformers.AbstractInOutForTransformer;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;

public class SystemPropertiesHelper extends AbstractInOutForTransformer {

    private static final String OBJECT = "object";
    private static final String OBJECTS = "objects";
    private static final String OBJECT_VALUE = "value";
    private static final String CHILD_OBJECTS = "child_objects";
    private static final String OBJECT_PROPERTY = "property";
    private static final String OBJECT_PROPERTIES = "properties";


    public Set<String> getObjectRepositorySystemProperties(Map<String, Object> objectMap) {
        List objects = (List) objectMap.get(OBJECTS);
        Set<String> systemProps = new HashSet<>();
        if (isNotEmpty(objects)) {
            getSystemPropertiesObjRepo(objects, systemProps);
        }
        return systemProps;
    }

    private void getSystemPropertiesObjRepo(List objects, Set<String> systemProps) {
        ArrayDeque<Map> objectsQueue = new ArrayDeque<>();

        changeObjectProperties(objects, objectsQueue, systemProps);
        while (!objectsQueue.isEmpty()) {
            Map currentObj = objectsQueue.pop();
            List childObjects =
                    (List) currentObj.get(CHILD_OBJECTS);
            if (childObjects != null) {
                changeObjectProperties(childObjects, objectsQueue, systemProps);
            }
        }
    }

    private void changeObjectProperties(List objects, ArrayDeque<Map> objectsQueue, Set<String> systemProps) {
        for (Object object : objects) {
            Map obj = (Map) ((Map) object).get(OBJECT);
            systemProps.addAll(getSystemPropertiesForObjRepositoryProperties(obj));
            objectsQueue.push(obj);
        }
    }

    private Set<String> getSystemPropertiesForObjRepositoryProperties(Object object) {
        List properties = (List) ((Map) object).get(OBJECT_PROPERTIES);
        Set<String> stringSet = new HashSet<>();
        if (properties instanceof ArrayList) {
            for (Object elementList : properties) {
                Map valueMap = (Map) ((Map) elementList).get(OBJECT_PROPERTY);
                if (nonNull(valueMap) && nonNull(valueMap.get(OBJECT_VALUE)) &&
                        nonNull(((Map) valueMap.get(OBJECT_VALUE)).get(OBJECT_VALUE))) {
                    Accumulator accumulator = extractFunctionData(
                            (Serializable) ((Map) valueMap.get(OBJECT_VALUE)).get(OBJECT_VALUE));
                    stringSet.addAll(accumulator.getSystemPropertyDependencies());
                }
            }
        }
        return stringSet;
    }


    public Set<String> getSystemPropertiesFromSettings(Map objectMap) {
        Set<String> systemProps = new HashSet<>();
        Map sapSettings = (Map) objectMap.get("sap");
        findSystemPropertiesSapSettings(sapSettings, systemProps);
        Map windowsSettings = (Map) objectMap.get("windows");
        findSystemPropertiesWindowsSettings(windowsSettings, systemProps);
        Map webSettings = (Map) objectMap.get("web");
        findSystemPropertiesWebSettings(webSettings, systemProps);
        return systemProps;
    }

    private void findSystemPropertiesSapSettings(Map sapSettings,
            Set<String> systemProps) {
        systemProps.addAll(getSystemPropertyValue((String) sapSettings.get("user")));
        systemProps.addAll(getSystemPropertyValue((String) sapSettings.get("client")));
        systemProps.addAll(getSystemPropertyValue((String) sapSettings.get("language")));
        systemProps.addAll(getSystemPropertyValue((String) sapSettings.get("password")));
        systemProps.addAll(getSystemPropertyValue((String) sapSettings.get("server")));
    }

    private void findSystemPropertiesWindowsSettings(Map windowsSettings,
            Set<String> systemProps) {
        Map appType = (Map) windowsSettings.get("apps");
        if (MapUtils.isNotEmpty(appType)) {
            for (Object value : appType.values()) {
                systemProps.addAll(getSystemPropertyValue((String) ((Map) value).get("args")));
                systemProps.addAll(getSystemPropertyValue((String) ((Map) value).get("path")));
                systemProps.addAll(getSystemPropertyValue((String) ((Map) value).get("directory")));
            }
        }
    }

    private void findSystemPropertiesWebSettings(Map webSettings,
            Set<String> systemProps) {
        systemProps.addAll(getSystemPropertyValue((String) webSettings.get("address")));
        systemProps.addAll(getSystemPropertyValue((String) webSettings.get("browser")));
    }

    private Set<String> getSystemPropertyValue(String property) {
        Set<String> systemProperties = new HashSet<>();
        if (StringUtils.isNotEmpty(property)) {
            systemProperties = extractFunctionData(property).getSystemPropertyDependencies();
        }
        return systemProperties;
    }
}
    
