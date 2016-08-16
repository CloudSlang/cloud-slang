/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.validator;

import io.cloudslang.lang.entities.constants.RegexConstants;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author Bonczidai Levente
 * @since 8/11/2016
 */
@Component
public class SystemPropertyValidatorImpl implements SystemPropertyValidator {
    private Pattern namingPattern;

    public SystemPropertyValidatorImpl() {
        namingPattern = Pattern.compile(RegexConstants.SYSTEM_PROPERTY);
    }

    @Override
    public void validateNamespace(String input) {
        if (StringUtils.isNotEmpty(input)) {
            String type = "Namespace";
            validateChars(input, type);
            validateDelimiter(input, type);
        }
    }

    @Override
    public void validateKey(String input) {
        if (StringUtils.isEmpty(input)) {
            throw new RuntimeException("Key cannot be empty.");
        } else {
            String type = "Key";
            validateChars(input, type);
            validateDelimiter(input, type);
        }
    }

    private void validateChars(String input, String type) {
        if (!namingPattern.matcher(input).matches()) {
            throw new RuntimeException(type + "[" + input +"] contains invalid characters.");
        }
    }

    private void validateDelimiter(String input, String type) {
        if (input.startsWith(RegexConstants.SYSTEM_PROPERTY_DELIMITER)) {
            throw new RuntimeException(
                    type + "[" + input +"] cannot start with system property delimiter[" + RegexConstants.SYSTEM_PROPERTY_DELIMITER + "]."
            );
        }
        if (input.endsWith(RegexConstants.SYSTEM_PROPERTY_DELIMITER)) {
            throw new RuntimeException(
                    type + "[" + input +"] cannot end with system property delimiter[" + RegexConstants.SYSTEM_PROPERTY_DELIMITER + "]."
            );
        }
        String[] parts = input.split(RegexConstants.SYSTEM_PROPERTY_DELIMITER_ESCAPED);
        for (String part : parts) {
            if ("".equals(part)) {
                throw new RuntimeException(
                        type + "[" + input + "] cannot contain multiple system property delimiters["
                                + RegexConstants.SYSTEM_PROPERTY_DELIMITER + "] without content."
                );
            }
        }
    }

}
