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

import org.apache.commons.lang.StringUtils;


public class DependencyFormatValidator {
    private static final int DEPENDENCY_PARTS = 3;
    public static final String INVALID_DEPENDENCY = "Dependency definition should contain exactly [" +
            DEPENDENCY_PARTS + "] non empty parts separated by ':'";

    void validateDependency(String dependency) {
        String[] gavParts = dependency.split(":");
        if (gavParts.length != DEPENDENCY_PARTS ||
                StringUtils.isEmpty(gavParts[0].trim()) ||
                StringUtils.isEmpty(gavParts[1].trim()) ||
                StringUtils.isEmpty(gavParts[2].trim())) {
            throw new RuntimeException(INVALID_DEPENDENCY);
        }
    }
}
