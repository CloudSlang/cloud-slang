/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.utils;

import io.cloudslang.lang.compiler.SlangSource;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import static io.cloudslang.lang.compiler.SlangTextualKeys.NEXT_STEP;

/**
 * @author Bonczidai Levente
 * @since 3/22/2017
 */
public abstract class SlangSourceUtils {
    public static List<String> readLines(SlangSource source) {
        try (Reader reader = new StringReader(source.getContent())) {
            return IOUtils.readLines(reader);
        } catch (IOException e) {
            throw new RuntimeException("Error reading " + source.getName() + ":" + e.getMessage(), e);
        }
    }

    public static String getNavigationStepName(Serializable value) {
        if (value instanceof String) {
            return (String) value;
        } else {
            Map<String, Serializable> options = (Map<String, Serializable>) value;
            return (String) options.get(NEXT_STEP);
        }
    }

    public static Serializable getNavigationTarget(Serializable value, String newName) {
        if (value instanceof String) {
            return newName;
        } else {
            Map<String, Serializable> options = (Map<String, Serializable>) value;
            if (!options.isEmpty()) {
                options.put(NEXT_STEP, newName);
            }
            return value;
        }
    }

    public static boolean containsNavigationNextStep(Serializable value) {
        Map<String, Serializable> options = (Map<String, Serializable>) value;
        return options.containsKey(NEXT_STEP);
    }

}
