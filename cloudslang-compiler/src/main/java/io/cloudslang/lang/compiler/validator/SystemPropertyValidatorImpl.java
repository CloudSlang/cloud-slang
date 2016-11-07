/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.validator;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Bonczidai Levente
 * @since 8/11/2016
 */
public class SystemPropertyValidatorImpl extends AbstractValidator implements SystemPropertyValidator {

    @Override
    public void validateNamespace(String input) {
        if (StringUtils.isNotEmpty(input)) {
            validateItem(input, "Error validating system property namespace.");
        }
    }

    @Override
    public void validateKey(String input) {
        if (StringUtils.isEmpty(input)) {
            throw new RuntimeException("Key cannot be empty.");
        } else {
            validateItem(input, "Error validating system property key.");
        }
    }

    private void validateItem(String input, String errorMessage) {
        try {
            validateNamespaceRules(input);
        } catch (RuntimeException rex) {
            throw new RuntimeException(errorMessage + " Nested exception is: " + rex.getMessage(), rex);
        }
    }

}
