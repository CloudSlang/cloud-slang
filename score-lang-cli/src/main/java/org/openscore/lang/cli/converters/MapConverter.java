package org.openscore.lang.cli.converters;

/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/


import org.springframework.shell.core.Completion;
import org.springframework.shell.core.Converter;
import org.springframework.shell.core.MethodTarget;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Date: 11/11/2014
 *
 * @author lesant
 */

@Component
public class MapConverter implements Converter<Map<String, String>> {
    @Override
    public boolean supports(Class<?> type, String optionContext) {
        return Map.class.isAssignableFrom(type);
    }

    @Override
    public Map<String, String> convertFromText(String value, Class<?> targetType, String optionContext) {
        String[] values = StringUtils.commaDelimitedListToStringArray(value);
        Map<String, String> map = new HashMap<>();

        for (String v : values) {
            String[] keyValue = StringUtils.delimitedListToStringArray(v, "=");
            if (keyValue.length == 2) {
                map.put(keyValue[0], keyValue[1]);
            } else {
                throw new RuntimeException("Input should be in a key=value comma separated format, e.g. key1=val1,key2=val2 etc.");
            }
        }

        return map;
    }

    @Override
    public boolean getAllPossibleValues(List<Completion> completions, Class<?> targetType, String existingData, String optionContext, MethodTarget target) {
        return true;
    }
}
