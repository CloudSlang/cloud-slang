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

import static org.apache.commons.lang3.tuple.Pair.of;

import io.cloudslang.lang.compiler.modeller.transformers.AbstractInOutForTransformer;
import io.cloudslang.lang.entities.bindings.ScriptFunction;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.apache.commons.lang3.tuple.Pair;

public class SystemPropertiesHelper extends AbstractInOutForTransformer {

    private static final String OBJECT = "object";
    private static final String OBJECTS = "objects";
    private static final String OBJECT_VALUE = "value";
    private static final String CHILD_OBJECTS = "child_objects";
    private static final String OBJECT_PROPERTY = "property";
    private static final String OBJECT_PROPERTIES = "properties";


    public Pair<Set<String>, Set<ScriptFunction>> getObjectRepositorySystemProperties(Map<String, Object> objectMap) {
        @SuppressWarnings("unchecked")
        List<Object> objects = (List<Object>) objectMap.get(OBJECTS);
        Set<String> systemProps = new HashSet<>();
        Set<ScriptFunction> functions = new HashSet<>();
        Stack<List<Object>> stack = new Stack<>();
        getSystemPropertiesObjRepo(objects, stack);
        stack.parallelStream()
                .forEach(stackElement -> {
                    Pair<Set<String>, Set<ScriptFunction>> result = getSystemPropertyForObject(stackElement);
                    systemProps.addAll(result.getLeft());
                    functions.addAll(result.getRight());
                });
        return of(systemProps, functions);
//        stack.removeAllElements();
    }

    private Pair<Set<String>, Set<ScriptFunction>> getSystemPropertyForObject(Object object) {
        Set<String> stringSet = new HashSet<>();
        Set<ScriptFunction> functionDependencies = new HashSet<>();
        if (object instanceof ArrayList) {
            for (Object elementList : (ArrayList) object) {
                LinkedHashMap valueMap = (LinkedHashMap) ((LinkedHashMap) elementList).get(OBJECT_PROPERTY);
                Accumulator accumulator = extractFunctionData(
                        (Serializable) ((LinkedHashMap) valueMap.get(OBJECT_VALUE)).get(OBJECT_VALUE));
                stringSet.addAll(accumulator.getSystemPropertyDependencies());
                functionDependencies.addAll(accumulator.getFunctionDependencies());
            }
        }
        return of(stringSet, functionDependencies);
    }

    private void getSystemPropertiesObjRepo(List<Object> objRepository, Stack<List<Object>> stack) {

        if (objRepository != null) {
            for (Object object : objRepository) {
                @SuppressWarnings("unchecked") Map<String, Object> mappedObject =
                        (Map<String, Object>) ((LinkedHashMap) object).get(OBJECT);
                @SuppressWarnings("unchecked") List<Object> childObjects =
                        (List<Object>) mappedObject.get(CHILD_OBJECTS);
                @SuppressWarnings("unchecked") List<Object> properties =
                        (List<Object>) mappedObject.get(OBJECT_PROPERTIES);
                if (!childObjects.isEmpty()) {
                    getSystemPropertiesObjRepo(childObjects, stack);
                }
                stack.push(properties);
            }
        }
    }

    public Pair<Set<String>, Set<ScriptFunction>> getSystemPropertiesFromSettings(Map<String, Object> objectMap) {
        @SuppressWarnings("unchecked")
        List<Object> objects = (List<Object>) objectMap.get("web_settings");
        Set<String> systemProps = new HashSet<>();
        Set<ScriptFunction> functions = new HashSet<>();
        Stack<List<Object>> stack = new Stack<>();
        getSystemPropertiesObjRepo(objects, stack);
        stack.parallelStream()
                .forEach(stackElement -> {
                    Pair<Set<String>, Set<ScriptFunction>> result = getSystemPropertyForObject(stackElement);
                    systemProps.addAll(result.getLeft());
                    functions.addAll(result.getRight());
                });
        return of(systemProps, functions);
//        stack.removeAllElements();
    }

}
    
