/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.modeller;

import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import io.cloudslang.lang.compiler.modeller.result.ParseModellingResult;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;


public class SlangModellerImpl implements SlangModeller {

    private ExecutableBuilder executableBuilder;

    @Override
    public ExecutableModellingResult createModel(ParseModellingResult parseModellingResult) {
        ParsedSlang parsedSlang = parseModellingResult.getParsedSlang();
        Validate.notNull(parsedSlang, "You must supply a parsed Slang source to compile");

        try {
            switch (parsedSlang.getType()) {
                case OPERATION:
                    return aggregateModellingWithParseResult(
                            executableBuilder.transformToExecutable(parsedSlang, parsedSlang.getOperation()),
                            parseModellingResult
                    );
                case FLOW:
                    return aggregateModellingWithParseResult(
                            executableBuilder.transformToExecutable(parsedSlang, parsedSlang.getFlow()),
                            parseModellingResult
                    );
                case DECISION:
                    return aggregateModellingWithParseResult(
                            executableBuilder.transformToExecutable(parsedSlang, parsedSlang.getDecision()),
                            parseModellingResult
                    );
                default:
                    throw new RuntimeException("Source: " + parsedSlang.getName() +
                            " is not of flow, operations or decision type");
            }
        } catch (Throwable ex) {
            throw new RuntimeException("Error transforming source: " + parsedSlang.getName() +
                    " to a Slang model. " + ex.getMessage(), ex);
        }
    }

    private ExecutableModellingResult aggregateModellingWithParseResult(
            ExecutableModellingResult executableModellingResult,
            ParseModellingResult parseModellingResult) {
        List<RuntimeException> aggregatedExceptions = new ArrayList<>();
        aggregatedExceptions.addAll(parseModellingResult.getErrors());
        aggregatedExceptions.addAll(executableModellingResult.getErrors());
        return new ExecutableModellingResult(executableModellingResult.getExecutable(), aggregatedExceptions);
    }

    public void setExecutableBuilder(ExecutableBuilder executableBuilder) {
        this.executableBuilder = executableBuilder;
    }
}
