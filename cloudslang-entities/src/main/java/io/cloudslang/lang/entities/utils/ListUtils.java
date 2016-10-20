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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bonczidai Levente
 * @since 8/16/2016
 */
public final class ListUtils {

    private ListUtils() {
    }

    public static List<String> subtract(List<String> a, List<String> b) {
        List<String> result = new ArrayList<>(a);
        result.removeAll(b);
        return result;
    }

}
