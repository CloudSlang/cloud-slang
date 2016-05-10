/**
 * ****************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 * <p/>
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * *****************************************************************************
 */
package io.cloudslang.lang.compiler.modeller.transformers;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.Validate;

/**
 * @author Bonczidai Levente
 * @since 5/5/2016
 */
public abstract class AbstractTransformer {

    public static final String MISSING_KEYS_ERROR_MESSAGE_PREFIX = "Following keys are missing: ";
    public static final String INVALID_KEYS_ERROR_MESSAGE_PREFIX = "Following keys are invalid: ";

    protected void validateKeySet(
            Set<String> keySet,
            Set<String> mandatoryKeys,
            Set<String> optionalKeys) {
        Validate.notNull(keySet);
        Validate.notNull(mandatoryKeys);
        Validate.notNull(optionalKeys);

        Set<String> missingKeys = new HashSet<>(mandatoryKeys);
        missingKeys.removeAll(keySet);
        if (CollectionUtils.isNotEmpty(missingKeys)) {
            throw new RuntimeException(
                    MISSING_KEYS_ERROR_MESSAGE_PREFIX + missingKeys.toString()
            );
        }

        Set<String> invalidKeys = new HashSet<>(keySet);
        invalidKeys.removeAll(mandatoryKeys);
        invalidKeys.removeAll(optionalKeys);
        if (CollectionUtils.isNotEmpty(invalidKeys)) {
            throw new RuntimeException(
                    INVALID_KEYS_ERROR_MESSAGE_PREFIX + invalidKeys.toString()
            );
        }
    }

}
