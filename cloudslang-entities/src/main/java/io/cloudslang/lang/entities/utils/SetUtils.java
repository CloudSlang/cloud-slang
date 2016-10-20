/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.entities.utils;

import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.InOutParam;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Bonczidai Levente
 * @since 7/6/2016
 */
public final class SetUtils {

    private SetUtils() {
    }

    public static boolean containsIgnoreCase(Set<String> set, String element) {
        for (String x : set) {
            if (x != null && x.equalsIgnoreCase(element)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsIgnoreCaseBasedOnFqn(Set<SystemProperty> set, SystemProperty element) {
        for (SystemProperty x : set) {
            if (x.getFullyQualifiedName().equalsIgnoreCase(element.getFullyQualifiedName())) {
                return true;
            }
        }
        return false;
    }

    public static <T> Set<T> mergeSets(Collection<Set<T>> collection) {
        Set<T> result = new HashSet<>();
        for (Set<T> set : collection) {
            result.addAll(set);
        }
        return result;
    }

    public static boolean containsIgnoreCaseBasedOnName(Collection<InOutParam> inOutParams, InOutParam element) {
        for (InOutParam current : inOutParams) {
            if (current.getName().equalsIgnoreCase(element.getName())) {
                return true;
            }
        }
        return false;
    }

}
