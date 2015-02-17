/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.openscore.lang.compiler;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.Validate;
import org.openscore.lang.compiler.modeller.SlangModeller;
import org.openscore.lang.compiler.modeller.model.Executable;
import org.openscore.lang.compiler.parser.YamlParser;
import org.openscore.lang.compiler.parser.model.ParsedSlang;
import org.openscore.lang.compiler.scorecompiler.ScoreCompiler;
import org.openscore.lang.entities.CompilationArtifact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
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
	public Map<String, ? extends Serializable> loadSystemProperties(SlangSource... sources) {
		Validate.notNull(sources, "You must supply a source to load");
		Map<String, Serializable> result = new HashMap<>();
		for(SlangSource source : sources) {
			ParsedSlang parsedSlang = yamlParser.parse(source);
			Map<String, ? extends Serializable> systemProperties = parsedSlang.getSystemProperties();
			Validate.notNull(systemProperties, "No system properties specified");
			String namespace = parsedSlang.getNamespace();
			for(Map.Entry<String, ? extends Serializable> entry : systemProperties.entrySet()) {
				result.put(namespace + "." + entry.getKey(), entry.getValue());
			}
		}
		return result;
	}
    @Override
    public Map<String, Serializable> loadFileInputs(SlangSource... sources) {
        Validate.notNull(sources, "You must supply a source to load");
        Map<String, Serializable> result = new HashMap<>();
        for(SlangSource source : sources) {
            Map<String, Serializable> fileInputs = yamlParser.parseInputFile(source);
            Validate.notNull(fileInputs, "No file inputs specified");
            for(Map.Entry<String, Serializable> entry : fileInputs.entrySet()) {
                result.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return result;
    }



    @Override
    public Executable preCompile(SlangSource source) {
        Validate.notNull(source, "You must supply a source to compile");

        //first thing we parse the yaml file into java maps
        ParsedSlang parsedSlang = yamlParser.parse(source);

        // Then we transform the parsed Slang source to a Slang model
        return slangModeller.createModel(parsedSlang);
    }
}
