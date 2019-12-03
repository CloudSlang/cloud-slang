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

import io.cloudslang.lang.compiler.modeller.transformers.AbstractInOutForTransformer;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
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
        if (CollectionUtils.isNotEmpty(objects)) {
            getSystemPropertiesObjRepo(objects, systemProps);
        }
        return systemProps;
    }

    private void getSystemPropertiesObjRepo(List objects, Set<String> systemProps) {
        ArrayDeque<Map> objectsStack = new ArrayDeque<>();
        for (Object object : objects) {
            @SuppressWarnings("unchecked") Map<String, Object> mappedObject =
                    (Map<String, Object>) ((Map) object).get(OBJECT);
            objectsStack.push(mappedObject);
            systemProps.addAll(getSystemPropertiesForObjRepositoryProperties(mappedObject));
        }
        while (!objectsStack.isEmpty()) {
            Map currentObj = objectsStack.pop();
            List childObjects =
                    (List) currentObj.get(CHILD_OBJECTS);
            if (childObjects != null) {
                for (Object child : childObjects) {
                    @SuppressWarnings("unchecked") Map<String, Object> childObject =
                            (Map<String, Object>) ((Map) child).get(OBJECT);
                    systemProps.addAll(getSystemPropertiesForObjRepositoryProperties(childObject));
                    objectsStack.push(childObject);
                }
            }
        }
    }

    private Set<String> getSystemPropertiesForObjRepositoryProperties(Object object) {
        @SuppressWarnings("unchecked") List<Object> properties =
                (List<Object>) ((Map) object).get(OBJECT_PROPERTIES);
        Set<String> stringSet = new HashSet<>();
        if (nonNull(properties) && properties instanceof ArrayList) {
            for (Object elementList : properties) {
                Map valueMap = (Map) ((Map)elementList).get(OBJECT_PROPERTY);
                boolean valueExist = nonNull(valueMap) && nonNull(valueMap.get(OBJECT_VALUE)) &&
                        nonNull(((Map)valueMap.get(OBJECT_VALUE)).get(OBJECT_VALUE));
                if (valueExist) {
                    Accumulator accumulator = extractFunctionData(
                            (Serializable) ((Map) valueMap.get(OBJECT_VALUE)).get(OBJECT_VALUE));
                    stringSet.addAll(accumulator.getSystemPropertyDependencies());
                }
            }
        }
        return stringSet;
    }


    public Set<String> getSystemPropertiesFromSettings(Map<String, Object> objectMap) {
        Set<String> systemProps = new HashSet<>();
        @SuppressWarnings("unchecked")
        Map<String, Object> sapSettings = (Map<String, Object>) objectMap.get("sap");
        findSystemPropertiesSapSettings(sapSettings, systemProps);
        @SuppressWarnings("unchecked")
        Map<String, Object> windowsSettings = (Map<String, Object>) objectMap.get("windows");
        findSystemPropertiesWindowsSettings(windowsSettings, systemProps);
        @SuppressWarnings("unchecked")
        Map<String, Object> webSettings = (Map<String, Object>) objectMap.get("web");
        findSystemPropertiesWebSettings(webSettings, systemProps);
        return systemProps;
    }

    private void findSystemPropertiesSapSettings(Map<String, Object> sapSettings,
            Set<String> systemProps) {
        systemProps.addAll(getSystemPropertyValue((String) sapSettings.get("user")));
        systemProps.addAll(getSystemPropertyValue((String) sapSettings.get("client")));
        systemProps.addAll(getSystemPropertyValue((String) sapSettings.get("language")));
        systemProps.addAll(getSystemPropertyValue((String) sapSettings.get("password")));
        systemProps.addAll(getSystemPropertyValue((String) sapSettings.get("server")));
    }

    private void findSystemPropertiesWindowsSettings(Map<String, Object> windowsSettings,
            Set<String> systemProps) {
        @SuppressWarnings("unchecked")
        Map<String, Object> appType = (Map) windowsSettings.get("apps");
        if (MapUtils.isNotEmpty(appType)) {
            for (Object value : appType.values()) {
                systemProps.addAll(getSystemPropertyValue((String) ((Map) value).get("args")));
                systemProps.addAll(getSystemPropertyValue((String) ((Map) value).get("path")));
                systemProps.addAll(getSystemPropertyValue((String) ((Map) value).get("directory")));
            }
        }
    }

    private void findSystemPropertiesWebSettings(Map<String, Object> webSettings,
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
    
