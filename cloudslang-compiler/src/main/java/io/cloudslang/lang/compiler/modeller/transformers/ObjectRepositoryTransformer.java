/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller.transformers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class ObjectRepositoryTransformer extends AbstractInOutForTransformer {

    public Set<String> getObjectRepositorySystemProperties(Map<String, Object> objectMap) {

        @SuppressWarnings("unchecked") List<Object> objects = (List<Object>) objectMap.get("objects");
        Set<String> systemProps = new HashSet<>();
        Stack<List<Object>> stack = new Stack<>();
        getSystemPropertiesObjRepo(objects, stack);
        while (!stack.isEmpty()) {
            List<Object> stackObject = stack.pop();
            stackObject.parallelStream()
                    .forEach(property -> systemProps.addAll(getSystemPropertyForObject(stackObject)));
        }
        return systemProps;
    }

    private Set<String> getSystemPropertyForObject(Object object) {
        Set<String> stringSet = new HashSet<>();
        if (object instanceof ArrayList) {
            for(Object elementList : (ArrayList)object) {
                LinkedHashMap valueMap = (LinkedHashMap) ((LinkedHashMap) elementList).get("property");
                Accumulator accumulator = extractFunctionData(
                        (Serializable) ((LinkedHashMap) valueMap.get("value")).get("value"));
                stringSet.addAll(accumulator.getSystemPropertyDependencies());
            }
        }
        return stringSet;
    }

    private void getSystemPropertiesObjRepo(List<Object> objRepository, Stack<List<Object>> stack) {

        if (objRepository != null) {
            for (Object object : objRepository) {
                @SuppressWarnings("unchecked") Map<String, Object> mappedObject =
                        (Map<String, Object>) ((LinkedHashMap)object).get("object");
                @SuppressWarnings("unchecked") List<Object> childObjects =
                        (List<Object>) mappedObject.get("child_objects");
                @SuppressWarnings("unchecked") List<Object> properties =
                        (List<Object>) mappedObject.get("properties");
                if (!childObjects.isEmpty()) {
                    getSystemPropertiesObjRepo(childObjects, stack);
                }
                stack.push(properties);
            }
        }
    }
}
