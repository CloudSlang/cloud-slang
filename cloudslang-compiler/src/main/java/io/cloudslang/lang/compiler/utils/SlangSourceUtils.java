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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.cloudslang.lang.compiler.SlangTextualKeys.NEXT_STEP;

/**
 * @author Bonczidai Levente
 * @since 3/22/2017
 */
public abstract class SlangSourceUtils {
    public static List<String> readLines(SlangSource source) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(source.getContent()))) {
            String nextLine = getNextLine(reader);
            while (nextLine != null) {
                lines.add(nextLine);
                nextLine = reader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading " + source.getName() + ":" + e.getMessage(), e);
        }
        return lines;
    }

    private static String getNextLine(BufferedReader reader) throws IOException {
        return reader.readLine();
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
