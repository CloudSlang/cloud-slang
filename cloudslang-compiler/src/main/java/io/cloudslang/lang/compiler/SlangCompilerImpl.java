/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler;

import io.cloudslang.lang.compiler.caching.CacheResult;
import io.cloudslang.lang.compiler.caching.CacheValueState;
import io.cloudslang.lang.compiler.caching.CachedPrecompileService;
import io.cloudslang.lang.compiler.modeller.SlangModeller;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.result.CompilationModellingResult;
import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import io.cloudslang.lang.compiler.modeller.result.ParseModellingResult;
import io.cloudslang.lang.compiler.modeller.result.SystemPropertyModellingResult;
import io.cloudslang.lang.compiler.parser.YamlParser;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import io.cloudslang.lang.compiler.scorecompiler.ScoreCompiler;
import io.cloudslang.lang.compiler.validator.CompileValidator;
import io.cloudslang.lang.compiler.validator.SystemPropertyValidator;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.SystemProperty;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import io.cloudslang.lang.entities.utils.SetUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.NotImplementedException;

import static io.cloudslang.lang.compiler.SlangTextualKeys.SENSITIVE_KEY;
import static io.cloudslang.lang.compiler.SlangTextualKeys.VALUE_KEY;

/*
 * Created by orius123 on 05/11/14.
 */
public class SlangCompilerImpl implements SlangCompiler {

    public static final String NOT_A_VALID_SYSTEM_PROPERTY_FILE_ERROR_MESSAGE_SUFFIX =
            "is not a valid system property file.";
    public static final String ERROR_LOADING_PROPERTIES_FILE_MESSAGE =
            "Error loading properties source: '";
    public static final String PROPERTY_LIST_ELEMENT_WRONG_TYPE_ERROR_MESSAGE_PREFIX =
            "Property list element should be map in 'key: value' format. Found: ";
    public static final String SIZE_OF_SYSTEM_PROPERTY_ERROR_MESSAGE_PREFIX =
            "Size of system property represented as a map should be 1 (key: value). For property: '";
    public static final String SYSTEM_PROPERTY_KEY_WRONG_TYPE_ERROR_MESSAGE_PREFIX =
            "System property key must be string. Found: ";
    public static final String DUPLICATE_SYSTEM_PROPERTY_KEY_ERROR_MESSAGE_PREFIX =
            "Duplicate system property key: '";

    private YamlParser yamlParser;

    private SlangModeller slangModeller;

    private ScoreCompiler scoreCompiler;

    private CompileValidator compileValidator;

    private SystemPropertyValidator systemPropertyValidator;

    private CachedPrecompileService cachedPrecompileService;

    @Override
    public CompilationArtifact compile(SlangSource source, Set<SlangSource> dependencySources) {
        return compile(source, dependencySources, PrecompileStrategy.WITHOUT_CACHE);
    }

    @Override
    public CompilationArtifact compile(
            SlangSource source,
            Set<SlangSource> path,
            PrecompileStrategy precompileStrategy) {
        CompilationModellingResult result = compileSource(source, path, precompileStrategy);
        if (result.getErrors().size() > 0) {
            throw result.getErrors().get(0);
        }
        return result.getCompilationArtifact();
    }

    @Override
    public CompilationModellingResult compileSource(SlangSource source, Set<SlangSource> dependencySources) {
        return compileSource(source, dependencySources, PrecompileStrategy.WITHOUT_CACHE);
    }

    @Override
    public CompilationModellingResult compileSource(
            SlangSource source,
            Set<SlangSource> path,
            PrecompileStrategy precompileStrategy) {
        ExecutableModellingResult executableModellingResult = preCompileSource(source, precompileStrategy);
        List<RuntimeException> errors = executableModellingResult.getErrors();

        // we transform also all of the files in the given dependency sources to model objects
        Map<Executable, SlangSource> executablePairs = new HashMap<>();
        executablePairs.put(executableModellingResult.getExecutable(), source);

        if (CollectionUtils.isNotEmpty(path)) {
            for (SlangSource currentSource : path) {
                ExecutableModellingResult result = preCompileSource(currentSource, precompileStrategy);
                Executable preCompiledCurrentSource = result.getExecutable();
                errors.addAll(result.getErrors());

                List<RuntimeException> validatorErrors = compileValidator
                        .validateNoDuplicateExecutables(preCompiledCurrentSource, currentSource, executablePairs);
                errors.addAll(validatorErrors);

                executablePairs.put(preCompiledCurrentSource, currentSource);
            }
        }

        executablePairs.remove(executableModellingResult.getExecutable());

        CompilationModellingResult result = scoreCompiler
                .compileSource(executableModellingResult.getExecutable(), executablePairs.keySet());
        errors.addAll(result.getErrors());
        return new CompilationModellingResult(result.getCompilationArtifact(), errors);
    }

    @Override
    public Executable preCompile(SlangSource source) {
        return preCompile(source, PrecompileStrategy.WITHOUT_CACHE);
    }

    @Override
    public Executable preCompile(SlangSource source, PrecompileStrategy precompileStrategy) {
        ExecutableModellingResult result = preCompileSource(source, precompileStrategy);
        if (result.getErrors().size() > 0) {
            throw result.getErrors().get(0);
        }
        return result.getExecutable();
    }

    @Override
    public ExecutableModellingResult preCompileSource(SlangSource source) {
        return preCompileSource(source, PrecompileStrategy.WITHOUT_CACHE);
    }

    @Override
    public ExecutableModellingResult preCompileSource(SlangSource source, PrecompileStrategy precompileStrategy) {
        Validate.notNull(source, "You must supply a source to compile");
        Validate.notNull(precompileStrategy, "Pre-compile strategy can not be null");

        final String filePath = source.getFilePath();

        // handle caching
        CacheResult cacheResult = precompileCachePreExecute(source, precompileStrategy, filePath);
        if (cacheResult != null && isValidCachedValue(cacheResult)) {
            return cacheResult.getExecutableModellingResult();
        }

        ExecutableModellingResult executableModellingResult = preCompileModel(source);

        // handle caching
        precompileCachePostExecute(source, precompileStrategy, filePath, executableModellingResult);

        return executableModellingResult;
    }

    @Override
    public void invalidateAllInPreCompileCache() {
        cachedPrecompileService.invalidateAll();
    }

    @Override
    public List<RuntimeException> validateSlangModelWithDirectDependencies(Executable slangModel,
                                                                           Set<Executable> directDependenciesModels) {
        return scoreCompiler.validateSlangModelWithDirectDependencies(slangModel, directDependenciesModels);
    }

    @Override
    public Set<SystemProperty> loadSystemProperties(SlangSource source) {
        SystemPropertyModellingResult systemPropertyModellingResult = loadSystemPropertiesFromSource(source);
        if (systemPropertyModellingResult.getErrors().size() > 0) {
            throw systemPropertyModellingResult.getErrors().get(0);
        }
        return systemPropertyModellingResult.getSystemProperties();
    }

    @Override
    public SystemPropertyModellingResult loadSystemPropertiesFromSource(SlangSource source) {
        ParseModellingResult parseModellingResult = parseSystemPropertiesFile(source);
        return extractProperties(parseModellingResult.getParsedSlang(), source, parseModellingResult.getErrors());
    }

    private void precompileCachePostExecute(
            SlangSource source,
            PrecompileStrategy precompileStrategy,
            String filePath,
            ExecutableModellingResult executableModellingResult) {
        switch (precompileStrategy) {
            case WITH_CACHE:
                cachedPrecompileService.cacheValue(filePath, executableModellingResult, source);
                break;
            case WITHOUT_CACHE:
                break;
            default:
                throw new NotImplementedException(generatePreCompileTypeErrorMessage(precompileStrategy));
        }
    }

    private CacheResult precompileCachePreExecute(
            SlangSource source,
            PrecompileStrategy precompileStrategy,
            String filePath) {
        CacheResult cacheResult = null;
        switch (precompileStrategy) {
            case WITH_CACHE:
                cacheResult = cachedPrecompileService.getValueFromCache(filePath, source);
                break;
            case WITHOUT_CACHE:
                break;
            default:
                throw new NotImplementedException(generatePreCompileTypeErrorMessage(precompileStrategy));
        }
        return cacheResult;
    }

    private String generatePreCompileTypeErrorMessage(PrecompileStrategy precompileStrategy) {
        return "Precompile type[" + precompileStrategy + "] not yet implemented";
    }

    private ExecutableModellingResult preCompileModel(SlangSource source) {
        //first thing we parse the yaml file into java maps
        ParsedSlang parsedSlang = yamlParser.parse(source);
        ParseModellingResult parseModellingResult = yamlParser.validate(parsedSlang);

        // Then we transform the parsed Slang source to a Slang model
        return slangModeller.createModel(parseModellingResult);
    }

    private boolean isValidCachedValue(CacheResult cacheResult) {
        CacheValueState cacheValueState = cacheResult.getState();
        switch (cacheValueState) {
            case VALID:
                return true;
            case MISSING:
            case OUTDATED:
                return false;
            default:
                throw new NotImplementedException("Cache value state[" + cacheValueState + "] not yet implemented");
        }
    }

    private ParseModellingResult parseSystemPropertiesFile(SlangSource source) {
        List<RuntimeException> exceptions = new ArrayList<>();
        ParsedSlang parsedSlang = null;
        try {
            parsedSlang = yamlParser.parse(source);
            if (!ParsedSlang.Type.SYSTEM_PROPERTY_FILE.equals(parsedSlang.getType())) {
                throw new RuntimeException("Source: " + parsedSlang.getName() + " " +
                        NOT_A_VALID_SYSTEM_PROPERTY_FILE_ERROR_MESSAGE_SUFFIX);
            }
        } catch (Throwable ex) {
            exceptions.add(getException(source, ex));
        }
        try {
            parsedSlang = yamlParser.validateAndThrowFirstError(parsedSlang);
        } catch (RuntimeException ex) {
            exceptions.add(getException(source, ex));
        }

        return new ParseModellingResult(parsedSlang, exceptions);
    }

    private RuntimeException getException(SlangSource source, Throwable ex) {
        return new RuntimeException(
                ERROR_LOADING_PROPERTIES_FILE_MESSAGE + source.getName() + "'. Nested exception is: " + ex.getMessage(),
                ex);
    }

    private SystemPropertyModellingResult extractProperties(ParsedSlang parsedSlang, SlangSource source,
                                                            List<RuntimeException> exceptions) {
        Set<SystemProperty> modelledSystemProperties = new HashSet<>();
        Set<String> modelledSystemPropertyKeys = new HashSet<>();

        if (parsedSlang != null) {
            // parsedSlang is null when properties yaml node is not defined in the property .sl file

            List<Map<String, Object>> parsedSystemProperties =
                    convertRawProperties(parsedSlang.getProperties(), source, exceptions);
            for (Map<String, Object> propertyAsMap : parsedSystemProperties) {
                Map.Entry<String, Object> propertyAsEntry = propertyAsMap.entrySet().iterator().next();
                String propertyKey = getPropertyKey(propertyAsEntry, source, exceptions);
                if (SetUtils.containsIgnoreCase(modelledSystemPropertyKeys, propertyKey)) {
                    exceptions.add(getException(source, new RuntimeException(
                            DUPLICATE_SYSTEM_PROPERTY_KEY_ERROR_MESSAGE_PREFIX + propertyKey + "'.")));
                } else {
                    modelledSystemPropertyKeys.add(propertyKey);
                }

                Object propertyValue = propertyAsEntry.getValue();
                SystemProperty property =
                        transformSystemProperty(parsedSlang.getNamespace(), propertyKey, propertyValue);
                modelledSystemProperties.add(property);
            }
        }

        return new SystemPropertyModellingResult(modelledSystemProperties, exceptions);
    }

    private String getPropertyKey(Map.Entry<String, Object> propertyAsEntry, SlangSource source,
                                  List<RuntimeException> exceptions) {
        String propertyKey = propertyAsEntry.getKey();
        try {
            systemPropertyValidator.validateKey(propertyKey);
        } catch (RuntimeException ex) {
            exceptions.add(getException(source, ex));
        }
        return propertyKey;
    }

    // casting and validations
    private List<Map<String, Object>> convertRawProperties(Object propertiesAsObject, SlangSource source,
                                                           List<RuntimeException> exceptions) {
        List<Map<String, Object>> convertedProperties = new ArrayList<>();
        if (propertiesAsObject != null) {
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
                                exceptions.add(getException(source, new RuntimeException(
                                        SYSTEM_PROPERTY_KEY_WRONG_TYPE_ERROR_MESSAGE_PREFIX +
                                                propertyKeyAsObject +
                                                "(" + propertyKeyAsObject.getClass().getName() + ")."
                                )));
                            }
                        } else {
                            exceptions.add(getException(source, new RuntimeException(
                                    SIZE_OF_SYSTEM_PROPERTY_ERROR_MESSAGE_PREFIX +
                                            propertyAsMap + "' size is: " + propertyAsMap.size() + "."
                            )));
                        }
                    } else {
                        String errorMessageSuffix;
                        if (propertyAsObject == null) {
                            errorMessageSuffix = "null.";
                        } else {
                            errorMessageSuffix = propertyAsObject.toString() + "(" +
                                    propertyAsObject.getClass().getName() + ").";
                        }
                        exceptions.add(getException(source, new RuntimeException(
                                PROPERTY_LIST_ELEMENT_WRONG_TYPE_ERROR_MESSAGE_PREFIX + errorMessageSuffix
                        )));
                    }
                }
            } else {
                exceptions.add(getException(source, new RuntimeException(
                        "Under '" + SlangTextualKeys.SYSTEM_PROPERTY_KEY +
                                "' key there should be a list. Found: " + propertiesAsObject.getClass().getName() + "."
                )));
            }
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

    public void setYamlParser(YamlParser yamlParser) {
        this.yamlParser = yamlParser;
    }

    public void setSlangModeller(SlangModeller slangModeller) {
        this.slangModeller = slangModeller;
    }

    public void setScoreCompiler(ScoreCompiler scoreCompiler) {
        this.scoreCompiler = scoreCompiler;
    }

    public void setCompileValidator(CompileValidator compileValidator) {
        this.compileValidator = compileValidator;
    }

    public void setSystemPropertyValidator(SystemPropertyValidator systemPropertyValidator) {
        this.systemPropertyValidator = systemPropertyValidator;
    }

    public void setCachedPrecompileService(CachedPrecompileService cachedPrecompileService) {
        this.cachedPrecompileService = cachedPrecompileService;
    }
}
