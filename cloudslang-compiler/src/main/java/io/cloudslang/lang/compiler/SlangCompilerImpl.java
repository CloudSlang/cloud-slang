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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * Created by orius123 on 05/11/14.
 */
@Component
public class SlangCompilerImpl implements SlangCompiler {

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
        if(CollectionUtils.isNotEmpty(path)) {
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
    public Set<SystemProperty> loadSystemProperties(SlangSource source) {
        try {
            ParsedSlang parsedSlang = parseSystemPropertiesFile(source);
            return extractProperties(parsedSlang);
        } catch (Throwable ex) {
            throw new RuntimeException(
                    "Error loading properties file: " + source.getName() + " nested exception is " + ex.getMessage(),
                    ex
            );
        }
    }

    private ParsedSlang parseSystemPropertiesFile(SlangSource source) {
        ParsedSlang parsedSlang = yamlParser.parse(source);
        if (!ParsedSlang.Type.SYSTEM_PROPERTY_FILE.equals(parsedSlang.getType())) {
            throw new RuntimeException("Source: " + parsedSlang.getName() + " is not a valid system property file.");
        }
        return parsedSlang;
    }

    private Set<SystemProperty> extractProperties(ParsedSlang parsedSlang) {
        String namespace = parsedSlang.getNamespace();
        Map<String, Object> rawSystemProperties = parsedSlang.getProperties();
        Set<SystemProperty> properties = new HashSet<>();
        if (rawSystemProperties != null) {
            Set<Map.Entry<String, Object>> entrySet = rawSystemProperties.entrySet();
            for (Map.Entry<String, Object> entry : entrySet) {
                Object value = entry.getValue();
                SystemProperty property = new SystemProperty(
                        namespace == null ? "" : namespace,
                        entry.getKey(),
                        value == null ? null : value.toString()
                );
                properties.add(property);
            }
        }
        return properties;
    }

}
