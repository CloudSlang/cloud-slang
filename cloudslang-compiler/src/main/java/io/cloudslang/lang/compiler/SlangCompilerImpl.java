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
import io.cloudslang.lang.compiler.validator.CompileValidator;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidator;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.entities.utils.SetUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.cloudslang.lang.compiler.SlangTextualKeys.SENSITIVE_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.VALUE_KEY;

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

    @Autowired
    private CompileValidator compileValidator;

    @Autowired
    private SystemPropertyValidator systemPropertyValidator;

    @Override
    public CompilationArtifact compile(SlangSource source, Set<SlangSource> dependencySources) {

        Executable executable = preCompile(source);

        // we transform also all of the files in the given dependency sources to model objects
        Map<Executable, SlangSource> executablePairs = new HashMap<>();
        executablePairs.put(executable, source);

        if (CollectionUtils.isNotEmpty(dependencySources)) {
            for (SlangSource currentSource : dependencySources) {
                Executable preCompiledCurrentSource = preCompile(currentSource);

                compileValidator.validateNoDuplicateExecutables(preCompiledCurrentSource, currentSource, executablePairs);

                executablePairs.put(preCompiledCurrentSource, currentSource);
            }
        }

        executablePairs.remove(executable);
        return scoreCompiler.compile(executable, executablePairs.keySet());
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
        return slangModeller.createModel(parsedSlang);
    }

    @Override
    public List<RuntimeException> validateSlangModelWithDirectDependencies(Executable slangModel, Set<Executable> directDependenciesModels) {
        return scoreCompiler.validateSlangModelWithDirectDependencies(slangModel,directDependenciesModels);
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
        String namespace = getNameSpace(parsedSlang);
        List<Map<String, Object>> parsedSystemProperties = convertRawProperties(parsedSlang.getProperties());
        Set<SystemProperty> modelledSystemProperties = new HashSet<>();
        Set<String> modelledSystemPropertyKeys = new HashSet<>();

        for (Map<String, Object> propertyAsMap : parsedSystemProperties) {
            Map.Entry<String, Object> propertyAsEntry = propertyAsMap.entrySet().iterator().next();
            String propertyKey = getPropertyKey(propertyAsEntry);
            if (SetUtils.containsIgnoreCase(modelledSystemPropertyKeys, propertyKey)) {
                throw new RuntimeException(
                        DUPLICATE_SYSTEM_PROPERTY_KEY_ERROR_MESSAGE_PREFIX + propertyKey + "'."
                );
            } else {
                modelledSystemPropertyKeys.add(propertyKey);
            }

            Object propertyValue = propertyAsEntry.getValue();
            SystemProperty property = transformSystemProperty(namespace, propertyKey, propertyValue);
            modelledSystemProperties.add(property);
        }

        return modelledSystemProperties;
    }

    private String getNameSpace(ParsedSlang parsedSlang) {
        String namespace = parsedSlang.getNamespace();
        systemPropertyValidator.validateNamespace(namespace);
        return namespace;
    }

    private String getPropertyKey(Map.Entry<String, Object> propertyAsEntry) {
        String propertyKey = propertyAsEntry.getKey();
        systemPropertyValidator.validateKey(propertyKey);
        return propertyKey;
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

    private SystemProperty transformSystemProperty(
            String rawNamespace,
            String key,
            Object rawValue) {
        String namespace = rawNamespace == null ? "" : rawNamespace;
        if (rawValue == null) {
            return new SystemProperty(namespace, key, (String) null);
        }
        if (rawValue instanceof Map) {
            Map rawModifiers = (Map) rawValue;
            Map<String, Serializable> modifiers = convertRawMap(rawModifiers, key);

            List<String> knownModifierKeys = Arrays.asList(SENSITIVE_KEY, VALUE_KEY);
            for (String modifierKey : modifiers.keySet()) {
                if (!knownModifierKeys.contains(modifierKey)) {
                    throw new RuntimeException(
                            "Artifact {" + key + "} has unrecognized tag {" + modifierKey + "}" +
                            ". Please take a look at the supported features per versions link");
                }
            }

            Serializable valueAsSerializable = modifiers.get(VALUE_KEY);
            String value = valueAsSerializable == null ? null : valueAsSerializable.toString();
            boolean sensitive = modifiers.containsKey(SENSITIVE_KEY) && (boolean) modifiers.get(SENSITIVE_KEY);

            if (sensitive) {
                return new SystemProperty(namespace, key, ValueFactory.createEncryptedString(value));
            } else {
                return new SystemProperty(namespace, key, value);
            }
        } else {
            return new SystemProperty(namespace, key, rawValue.toString());
        }
    }

    private Map<String, Serializable> convertRawMap(Map rawMap, String artifact) {
        Map<String, Serializable> convertedMap = new HashMap<>();
        @SuppressWarnings("unchecked")
        Set<Map.Entry> entrySet = rawMap.entrySet();
        for (Map.Entry entry : entrySet) {
            Object rawKey = entry.getKey();
            if (!(rawKey instanceof String)) {
                throw new RuntimeException(
                        "Artifact {" + artifact + "} has invalid tag {" + rawKey + "}:" +
                                " Value cannot be cast to String"
                );
            }
            Object rawValue = entry.getValue();
            if (!(rawValue instanceof Serializable)) {
                throw new RuntimeException(
                        "Artifact {" + artifact + "} has invalid value {" + rawValue + "}:" +
                                " Value cannot be cast to Serializable"
                );
            }
            convertedMap.put((String) rawKey, (Serializable) rawValue);
        }
        return convertedMap;
    }

}
