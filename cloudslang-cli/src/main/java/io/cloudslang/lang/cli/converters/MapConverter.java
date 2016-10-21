/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.cli.converters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.shell.core.Completion;
import org.springframework.shell.core.Converter;
import org.springframework.shell.core.MethodTarget;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Date: 11/11/2014
 *
 * @author lesant
 */

@Component
public class MapConverter implements Converter<Map<String, String>> {

    public static final String ESCAPE_EXPRESSION = "\\&^\\&";

    @Override
    public boolean supports(Class<?> type, String optionContext) {
        return Map.class.isAssignableFrom(type);
    }

    @Override
    public Map<String, String> convertFromText(String value, Class<?> targetType, String optionContext) {
        value = value.replace("\\,", ESCAPE_EXPRESSION);
        String[] values = StringUtils.commaDelimitedListToStringArray(value);
        Map<String, String> map = new HashMap<>();

        for (String v : values) {
            String[] keyValue = StringUtils.delimitedListToStringArray(v, "=");
            if (keyValue.length == 2) {
                keyValue[1] = keyValue[1].replace(ESCAPE_EXPRESSION, ",");
                map.put(keyValue[0], keyValue[1]);
            } else {
                throw new RuntimeException("Input should be in a key=value " +
                        "comma separated format, e.g. key1=val1,key2=val2 etc.");
            }
        }

        return map;
    }

    @Override
    public boolean getAllPossibleValues(List<Completion> completions, Class<?> targetType,
                                        String existingData, String optionContext, MethodTarget target) {
        return true;
    }
}
