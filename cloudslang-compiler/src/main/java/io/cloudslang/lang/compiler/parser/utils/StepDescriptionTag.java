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

/**
 * @author Bonczidai Levente
 * @since 3/21/2017
 */
public enum StepDescriptionTag {
    INPUT("@input"),
    OUTPUT("@output");

    private static final List<DescriptionTag> DESCRIPTION_TAGS_LIST =
            Collections.unmodifiableList(Arrays.asList(DescriptionTag.values()));
    private final String value;

    StepDescriptionTag(String value) {
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

}
