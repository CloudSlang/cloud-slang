/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.compiler;

import io.cloudslang.lang.compiler.modeller.SlangModeller;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import io.cloudslang.lang.compiler.parser.YamlParser;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import io.cloudslang.lang.compiler.scorecompiler.ScoreCompiler;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.SystemProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*
 * Created by orius123 on 05/11/14.
 */
@Component
public class SlangCompilerImpl implements SlangCompiler {

    public static final String NOT_A_VALID_SYSTEM_PROPERTY_FILE_ERROR_MESSAGE_SUFFIX = "is not a valid system property file.";
    public static final String ERROR_LOADING_PROPERTIES_FILE_MESSAGE = "Error loading properties source: '";
    public static final String PROPERTY_LIST_ELEMENT_WRONG_TYPE_ERROR_MESSAGE_PREFIX = "Property list element should be map in 'key: value' format. Found: ";
    public static final String SIZE_OF_SYSTEM_PROPERTY_ERROR_MESSAGE_PREFIX = "Size of system property represented as a map should be 1 (key: value). For property: '";
    public static final String SYSTEM_PROPERTY_KEY_WRONG_TYPE_ERROR_MESSAGE_PREFIX = "System property key must be string. Found: ";
    public static final String DUPLICATE_SYSTEM_PROPERTY_KEY_ERROR_MESSAGE_PREFIX = "Duplicate system property key: '";

    @Autowired
    private YamlParser yamlParser;

    @Autowired
    private SlangModeller slangModeller;

    @Autowired
    private ScoreCompiler scoreCompiler;

    @Override
    public CompilationArtifact compile(SlangSource source, Set<SlangSource> path) {

        Executable executable = preCompile(source);

        //we transform also all of the files in the given path to model objects
        Set<Executable> pathExecutables = new HashSet<>();
        if (CollectionUtils.isNotEmpty(path)) {
            for (SlangSource pathSource : path) {
                pathExecutables.add(preCompile(pathSource));
            }
        }

        return scoreCompiler.compile(executable, pathExecutables);
    }

    @Override
    public Executable preCompile(SlangSource source) {
        ExecutableModellingResult result = preCompileSource(source);
        if (result.getErrors().size() > 0) {
            throw result.getErrors().get(0);
        }
        return result.getExecutable();
    }

    @Override
    public ExecutableModellingResult preCompileSource(SlangSource source) {
        Validate.notNull(source, "You must supply a source to compile");

        //first thing we parse the yaml file into java maps
        ParsedSlang parsedSlang = yamlParser.parse(source);

        // Then we transform the parsed Slang source to a Slang model
        ExecutableModellingResult result = slangModeller.createModel(parsedSlang);
        if (result.getExecutable().getNamespace() == null || result.getExecutable().getNamespace().length() == 0) {
            result.getErrors().add(new IllegalArgumentException("Operation/Flow " + result.getExecutable().getName() + " must have a namespace"));
        }
        return result;
    }

    @Override
    public List<RuntimeException> validateSlangModelWithDependencies(Executable slangModel, Set<Executable> dependenciesModels) {
        return scoreCompiler.validateSlangModelWithDependencies(slangModel,dependenciesModels);
    }

    @Override
    public Set<SystemProperty> loadSystemProperties(SlangSource source) {
        try {
            ParsedSlang parsedSlang = parseSystemPropertiesFile(source);
            return extractProperties(parsedSlang);
        } catch (Throwable ex) {
            throw new RuntimeException(
                    ERROR_LOADING_PROPERTIES_FILE_MESSAGE + source.getFileName() + "'. Nested exception is: " + ex.getMessage(),
                    ex
            );
        }
    }

    private ParsedSlang parseSystemPropertiesFile(SlangSource source) {
        ParsedSlang parsedSlang = yamlParser.parse(source);
        if (!ParsedSlang.Type.SYSTEM_PROPERTY_FILE.equals(parsedSlang.getType())) {
            throw new RuntimeException("Source: " + parsedSlang.getName() + " " + NOT_A_VALID_SYSTEM_PROPERTY_FILE_ERROR_MESSAGE_SUFFIX);
        }
        return parsedSlang;
    }

    private Set<SystemProperty> extractProperties(ParsedSlang parsedSlang) {
        String namespace = parsedSlang.getNamespace();
        List<Map<String, Object>> parsedSystemProperties = convertRawProperties(parsedSlang.getProperties());
        Set<SystemProperty> modelledSystemProperties = new HashSet<>();
        Set<String> modelledSystemPropertyKeys = new HashSet<>();

        for (Map<String, Object> propertyAsMap : parsedSystemProperties) {
            Map.Entry<String, Object> propertyAsEntry = propertyAsMap.entrySet().iterator().next();
            String propertyKey = propertyAsEntry.getKey();
            if (modelledSystemPropertyKeys.contains(propertyKey)) {
                throw new RuntimeException(
                        DUPLICATE_SYSTEM_PROPERTY_KEY_ERROR_MESSAGE_PREFIX + propertyKey + "'."
                );
            } else {
                modelledSystemPropertyKeys.add(propertyKey);
            }

            Object propertyValue = propertyAsEntry.getValue();
            SystemProperty property = new SystemProperty(
                    namespace == null ? "" : namespace,
                    propertyKey,
                    propertyValue == null ? null : propertyValue.toString()
            );
            modelledSystemProperties.add(property);
        }

        return modelledSystemProperties;
    }

    // casting and validations
    private List<Map<String, Object>> convertRawProperties(Object propertiesAsObject) {
        List<Map<String, Object>> convertedProperties = new ArrayList<>();
        if (propertiesAsObject instanceof List) {
            List propertiesAsList = (List) propertiesAsObject;
            for (Object propertyAsObject : propertiesAsList) {
                if (propertyAsObject instanceof Map) {
                    Map propertyAsMap = (Map) propertyAsObject;
                    if (propertyAsMap.size() == 1) {
                        Map.Entry propertyAsEntry = (Map.Entry) propertyAsMap.entrySet().iterator().next();
                        Object propertyKeyAsObject = propertyAsEntry.getKey();
                        if (propertyKeyAsObject instanceof String) {
                            Map<String, Object> convertedProperty = new HashMap<>();
                            convertedProperty.put((String) propertyKeyAsObject, propertyAsEntry.getValue());
                            convertedProperties.add(convertedProperty);
                        } else {
                            throw new RuntimeException(
                                    SYSTEM_PROPERTY_KEY_WRONG_TYPE_ERROR_MESSAGE_PREFIX +
                                            propertyKeyAsObject + "(" + propertyKeyAsObject.getClass().getName() + ")."
                            );
                        }
                    } else {
                        throw new RuntimeException(
                                SIZE_OF_SYSTEM_PROPERTY_ERROR_MESSAGE_PREFIX +
                                        propertyAsMap + "' size is: " + propertyAsMap.size() + "."
                        );
                    }
                } else {
                    String errorMessageSuffix;
                    if (propertyAsObject == null) {
                        errorMessageSuffix = "null.";
                    } else {
                        errorMessageSuffix = propertyAsObject.toString() + "(" + propertyAsObject.getClass().getName() + ").";
                    }
                    throw new RuntimeException(
                            PROPERTY_LIST_ELEMENT_WRONG_TYPE_ERROR_MESSAGE_PREFIX + errorMessageSuffix
                    );
                }
            }
        } else {
            throw new RuntimeException(
                    "Under '" + SlangTextualKeys.SYSTEM_PROPERTY_KEY +
                            "' key there should be a list. Found: " + propertiesAsObject.getClass().getName() + "."
            );
        }
        return convertedProperties;
    }

}
