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

import java.util.HashSet;
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
        Validate.notNull(source, "You must supply a source to compile");

        //first thing we parse the yaml file into java maps
        ParsedSlang parsedSlang = yamlParser.parse(source);

        // Then we transform the parsed Slang source to a Slang model
        return slangModeller.createModel(parsedSlang);
    }
}
