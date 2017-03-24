/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.parser.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum DescriptionTag {
    DESCRIPTION("@description"),
    PREREQUISITES("@prerequisites"),
    INPUT("@input"),
    OUTPUT("@output"),
    RESULT("@result");

    private static final List<DescriptionTag> DESCRIPTION_TAGS_LIST =
            Collections.unmodifiableList(Arrays.asList(DescriptionTag.values()));
    private final String value;

    DescriptionTag(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static List<DescriptionTag> asList() {
        return DESCRIPTION_TAGS_LIST;
    }

    public static boolean isDescriptionTag(String str) {
        for (DescriptionTag descriptionTag : DescriptionTag.asList()) {
            if (str.contains(descriptionTag.getValue())) {
                return true;
            }
        }
        return false;
    }

    public static DescriptionTag fromString(String str) {
        for (DescriptionTag descriptionTag : DescriptionTag.asList()) {
            if (str.contains(descriptionTag.getValue())) {
                return descriptionTag;
            }
        }
        return null;
    }
}
