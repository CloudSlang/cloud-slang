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

import java.util.ArrayList;
import java.util.List;
import org.springframework.shell.core.Completion;
import org.springframework.shell.core.Converter;
import org.springframework.shell.core.MethodTarget;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Created by lesant on 12/16/2014.
 */
@Component
public class ListConverter implements Converter<List<String>> {
    @Override
    public boolean supports(Class<?> type, String optionContext) {
        return List.class.isAssignableFrom(type);
    }

    @Override
    public List<String> convertFromText(String value, Class<?> targetType, String optionContext) {
        String[] values = StringUtils.commaDelimitedListToStringArray(value);
        List<String> list = new ArrayList<>();
        for (String v : values) {
            list.add(v);
        }
        return list;
    }

    @Override
    public boolean getAllPossibleValues(List<Completion> completions, Class<?> targetType, String existingData,
                                        String optionContext, MethodTarget target) {
        return true;
    }


}