/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.entities;

import java.util.Objects;

public enum PromptType {
    TEXT("text"), SINGLE_CHOICE("single_choice"), MULTI_CHOICE("multi_choice");

    private final String name;

    PromptType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static PromptType fromString(String str) {
        for (PromptType type : values()) {
            if (Objects.equals(str, type.getName())) {
                return type;
            }
        }
        return null;
    }
}
