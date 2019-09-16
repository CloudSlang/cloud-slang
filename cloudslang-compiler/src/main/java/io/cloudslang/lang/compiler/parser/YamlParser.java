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



import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.modeller.result.ParseModellingResult;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import io.cloudslang.lang.compiler.parser.utils.ParserExceptionHandler;
import io.cloudslang.lang.compiler.validator.ExecutableValidator;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.Validate;
import org.yaml.snakeyaml.Yaml;

public abstract class YamlParser {

    private ParserExceptionHandler parserExceptionHandler;

    private ExecutableValidator executableValidator;

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

        try {
            ParsedSlang parsedSlang = getYaml().loadAs(source.getContent(), ParsedSlang.class);
            if (parsedSlang == null) {
                throw new RuntimeException("Source " + source.getName() + " does not contain YAML content");
            }
            parsedSlang.setName(source.getName());
            parsedSlang.setFileExtension(source.getFileExtension());

            return parsedSlang;
        } catch (Throwable e) {
            throw new RuntimeException("There was a problem parsing the YAML source: " +
                    source.getName() + ".\n" + parserExceptionHandler.getErrorMessage(e), e);
        }
    }

    public void setParserExceptionHandler(ParserExceptionHandler parserExceptionHandler) {
        this.parserExceptionHandler = parserExceptionHandler;
    }

    public void setExecutableValidator(ExecutableValidator executableValidator) {
        this.executableValidator = executableValidator;
    }
}
