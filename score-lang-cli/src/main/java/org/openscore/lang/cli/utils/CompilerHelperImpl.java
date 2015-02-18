/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.openscore.lang.cli.utils;

import ch.lambdaj.function.convert.Converter;

import com.google.common.collect.Lists;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.openscore.lang.api.Slang;
import org.openscore.lang.compiler.SlangSource;
import org.openscore.lang.entities.CompilationArtifact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ch.lambdaj.Lambda.convert;

import static org.openscore.lang.compiler.SlangSource.fromFile;

/**
 * @author lesant
 * @since 11/13/2014
 * @version $Id$
 */
@Component
public class CompilerHelperImpl implements CompilerHelper{

    @Autowired
    private Slang slang;

    private final static Logger logger = Logger.getLogger(CompilerHelperImpl.class);

    private String[] SLANG_FILE_EXTENSIONS = {"yml", "yaml", "py","sl"};

    //tot add javadoc
    public CompilationArtifact compile(String filePath, String opName, List<String> dependencies) throws IOException {
        Validate.notNull(filePath, "File path can not be null");

        Set<SlangSource> depsSources = new HashSet<>();
        File file = new File(filePath);
        Validate.isTrue(file.isFile(), "File: " + file.getName() + " was not found");

        if (dependencies == null || dependencies.isEmpty()) {
            dependencies = Lists.newArrayList(file.getParent()); //default behavior is taking the parent dir
        }

        for (String dependency:dependencies) {
            Collection<File> dependenciesFiles = FileUtils.listFiles(new File(dependency), SLANG_FILE_EXTENSIONS, true);
            depsSources.addAll(convert(dependenciesFiles, new Converter<File, SlangSource>() {
                @Override
                public SlangSource convert(File from) {
                    return fromFile(from);
                }
            }));
        }

        try {
            //todo - support compile of op too?
            return slang.compile(fromFile(file), depsSources);
        } catch (Exception e) {
            logger.error("Failed compilation for file : "+file.getName() + " ,Exception is : " + e.getMessage());
            throw e;
        }
    }

	@Override
	public Map<String, ? extends Serializable> loadSystemProperties(List<String> systemPropertyFiles) {
		if(CollectionUtils.isEmpty(systemPropertyFiles)) return null;
		SlangSource[] sources = new SlangSource[systemPropertyFiles.size()];
		for(int i = 0; i < systemPropertyFiles.size(); i++) {
			sources[i] = SlangSource.fromFile(new File(systemPropertyFiles.get(i)));
		}
		return slang.loadSystemProperties(sources);
	}

}
