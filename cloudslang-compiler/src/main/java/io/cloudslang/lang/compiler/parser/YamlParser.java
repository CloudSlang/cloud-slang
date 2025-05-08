/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.parser;


import io.cloudslang.lang.compiler.SlangByteSource;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.modeller.result.ParseModellingResult;
import io.cloudslang.lang.compiler.newyaml.YamlPoolService;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler;
import io.cloudslang.lang.compiler.validator.ExecutableValidator;
import org.apache.commons.lang3.Validate;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public abstract class YamlParser {

    private ParserExceptionHandler parserExceptionHandler;

    private ExecutableValidator executableValidator;

    private YamlPoolService yamlPoolService;

    protected abstract Yaml getYaml();

    public ParsedSlang validateAndThrowFirstError(ParsedSlang parsedSlang) {
        ParseModellingResult parseModellingResult = validate(parsedSlang);
        if (!parseModellingResult.getErrors().isEmpty()) {
            throw parseModellingResult.getErrors().get(0);
        } else {
            return parseModellingResult.getParsedSlang();
        }
    }

    public ParseModellingResult validate(ParsedSlang parsedSlang) {
        List<RuntimeException> errors = new ArrayList<>();
        try {
            executableValidator.validateNamespace(parsedSlang);
        } catch (RuntimeException rex) {
            errors.add(rex);
        }
        try {
            executableValidator.validateImportsSection(parsedSlang);
        } catch (RuntimeException rex) {
            errors.add(rex);
        }
        return new ParseModellingResult(parsedSlang, errors);
    }

    public ParsedSlang parse(SlangSource source) {
        Validate.notEmpty(source.getContent(), "Source " + source.getName() + " cannot be empty");

        Yaml yamlInstance = yamlPoolService.tryTakeYamlWithDefaultTimeout();
        try (Reader reader = new BufferedReader(new StringReader(source.getContent()))) {
            ParsedSlang parsedSlang = yamlInstance.loadAs(reader, ParsedSlang.class);
            if (parsedSlang == null) {
                throw new RuntimeException("Source " + source.getName() + " does not contain YAML content");
            }
            parsedSlang.setName(source.getName());
            parsedSlang.setFileExtension(source.getFileExtension());

            return parsedSlang;
        } catch (Throwable e) {
            throw new RuntimeException("There was a problem parsing the YAML source: " +
                    source.getName() + ".\n" + parserExceptionHandler.getErrorMessage(e), e);
        } finally {
            yamlPoolService.restoreYaml(yamlInstance);
        }
    }

    public ParsedSlang parse(SlangByteSource byteSource) {
        final byte[] sourceBytes = byteSource.getSource();
        if ((sourceBytes == null) || (sourceBytes.length == 0)) {
            throw new IllegalArgumentException("Source " + byteSource.getName() + " cannot be empty");
        }

        Yaml yamlInstance = yamlPoolService.tryTakeYamlWithDefaultTimeout();
        try (Reader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(sourceBytes),
                SlangSource.getCloudSlangCharset()))) {
            ParsedSlang parsedSlang = yamlInstance.loadAs(reader, ParsedSlang.class);

            if (parsedSlang == null) {
                throw new RuntimeException("Source " + byteSource.getName() + " does not contain YAML content");
            }
            parsedSlang.setName(byteSource.getName());
            parsedSlang.setFileExtension(byteSource.getFileExtension());

            return parsedSlang;
        } catch (Throwable e) {
            throw new RuntimeException("There was a problem parsing the YAML source: " +
                    byteSource.getName() + ".\n" + parserExceptionHandler.getErrorMessage(e), e);
        } finally {
            yamlPoolService.restoreYaml(yamlInstance);
        }
    }

    public void setParserExceptionHandler(ParserExceptionHandler parserExceptionHandler) {
        this.parserExceptionHandler = parserExceptionHandler;
    }

    public void setExecutableValidator(ExecutableValidator executableValidator) {
        this.executableValidator = executableValidator;
    }

    public void setYamlPoolService(YamlPoolService yamlPoolService) {
        this.yamlPoolService = yamlPoolService;
    }

}
