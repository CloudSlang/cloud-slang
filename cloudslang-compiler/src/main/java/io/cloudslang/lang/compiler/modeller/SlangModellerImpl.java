/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.compiler.modeller;

import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/*
 * Created by stoneo on 2/2/2015.
 */

@Component
public class SlangModellerImpl implements SlangModeller{

    @Autowired
    private ExecutableBuilder executableBuilder;

    @Override
    public Executable createModel(ParsedSlang parsedSlang) {
        Validate.notNull(parsedSlang, "You must supply a parsed Slang source to compile");

        try {
            switch (parsedSlang.getType()) {
                case OPERATION:
                    return transformToExecutable(parsedSlang, parsedSlang.getOperation());
                case FLOW:
                    return transformToExecutable(parsedSlang, parsedSlang.getFlow());
                default:
                    throw new RuntimeException("Source: " + parsedSlang.getName() + " is not of flow type or operations");
            }
        } catch (Throwable ex){
            throw new RuntimeException("Error transforming source: " + parsedSlang.getName() + " to a Slang model. " + ex.getMessage(), ex);
        }
    }

    /**
     * transform a parsed slang source {@link io.cloudslang.lang.compiler.parser.model.ParsedSlang} to an {@link io.cloudslang.lang.compiler.modeller.model.Executable}
     *
     * @param parsedSlang the source to transform the operations from
     * @return {@link io.cloudslang.lang.compiler.modeller.model.Executable} representing the operation or flow in the source
     */
    private Executable transformToExecutable(ParsedSlang parsedSlang, Map<String, Object> rawData) {
        String executableName = (String) rawData.get(SlangTextualKeys.EXECUTABLE_NAME_KEY);
        if (StringUtils.isBlank(executableName)) {
            throw new RuntimeException("Executable in source: " + parsedSlang.getName() + " has no name");
        }
        return executableBuilder.transformToExecutable(parsedSlang, executableName, rawData);
    }
}
