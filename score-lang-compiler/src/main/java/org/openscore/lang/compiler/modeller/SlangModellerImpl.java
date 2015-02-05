/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.openscore.lang.compiler.modeller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.openscore.lang.compiler.SlangTextualKeys;
import org.openscore.lang.compiler.modeller.model.Executable;
import org.openscore.lang.compiler.parser.model.ParsedSlang;
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
            //then we transform those maps to model objects
            return createExecutable(parsedSlang);
        } catch (Throwable ex){
            throw new RuntimeException("Error transforming source: " + parsedSlang.getName() + " to a Slang model. " + ex.getMessage(), ex);
        }
    }

    /**
     * Utility method that transform a {@link org.openscore.lang.compiler.parser.model.ParsedSlang}
     * into a list of {@link org.openscore.lang.compiler.modeller.model.Executable}
     * also handles operations files
     *
     * @param parsedSlang the source to transform
     * @return List of {@link org.openscore.lang.compiler.modeller.model.Executable}  of the requested flow or operation
     */
    private Executable createExecutable(ParsedSlang parsedSlang) {
        switch (parsedSlang.getType()) {
            case OPERATION:
                return transformToOperation(parsedSlang);
            case FLOW:
                return transformToFlow(parsedSlang);
            case SYSTEM_PROPERTIES:
                return null;
            default:
                throw new RuntimeException("source: " + parsedSlang.getName() + " is not of flow type or operations");
        }
    }

    /**
     * transform an operation {@link org.openscore.lang.compiler.parser.model.ParsedSlang} to a List of {@link org.openscore.lang.compiler.modeller.model.Executable}
     *
     * @param parsedSlang the source to transform the operations from
     * @return {@link org.openscore.lang.compiler.modeller.model.Executable} representing the operation in the source
     */
    private Executable transformToOperation(ParsedSlang parsedSlang) {
        Map<String, Object> rawData = parsedSlang.getOperation();
        return transformToExecutable(parsedSlang, rawData);
    }

    /**
     * transform an flow {@link org.openscore.lang.compiler.parser.model.ParsedSlang} to a {@link org.openscore.lang.compiler.modeller.model.Executable}
     *
     * @param parsedSlang the source to transform the flow from
     * @return {@link org.openscore.lang.compiler.modeller.model.Executable} representing the flow in the source
     */
    private Executable transformToFlow(ParsedSlang parsedSlang) {
        Map<String, Object> rawData = parsedSlang.getFlow();
        return transformToExecutable(parsedSlang, rawData);
    }

    private Executable transformToExecutable(ParsedSlang parsedSlang, Map<String, Object> rawData) {
        String executableName = (String) rawData.get(SlangTextualKeys.EXECUTABLE_NAME_KEY);
        if (StringUtils.isBlank(executableName)) {
            throw new RuntimeException("Executable in source: " + parsedSlang.getName() + " has no name");
        }
        return executableBuilder.transformToExecutable(parsedSlang, executableName, rawData);
    }
}
