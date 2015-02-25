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
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ch.lambdaj.Lambda.*;

import static org.hamcrest.Matchers.*;

/**
 * @author lesant
 * @since 11/13/2014
 * @version $Id$
 */
@Component
public class CompilerHelperImpl implements CompilerHelper{

    @Autowired
    private Slang slang;
    @Autowired
    private Yaml yaml;

    private static final Logger logger = Logger.getLogger(CompilerHelperImpl.class);
    private static final String[] SLANG_FILE_EXTENSIONS = {"yml", "yaml", "py", "sl"};
    private static final String SP_DIR = "properties"; //TODO reconsider it after closing slang file extensions & some real usecases
    private static final String[] SP_EXT = {"yaml", "yml"};

    @Override
	public CompilationArtifact compile(String filePath, List<String> dependencies) throws IOException {
        Validate.notNull(filePath, "File path can not be null");
        Set<SlangSource> depsSources = new HashSet<>();
        File file = new File(filePath);
        Validate.isTrue(file.isFile(), "File: " + file.getName() + " was not found");
        if (CollectionUtils.isEmpty(dependencies)) {
            dependencies = Lists.newArrayList(file.getParent()); //default behavior is taking the parent dir
        }
        for (String dependency:dependencies) {
            Collection<File> dependenciesFiles = FileUtils.listFiles(new File(dependency), SLANG_FILE_EXTENSIONS, true);
            dependenciesFiles = select(dependenciesFiles, having(on(File.class).getPath(), not(containsString(SP_DIR))));
            depsSources.addAll(convert(dependenciesFiles, new Converter<File, SlangSource>() {
                @Override
                public SlangSource convert(File from) {
                    return SlangSource.fromFile(from);
                }
            }));
        }
        try {
            return slang.compile(SlangSource.fromFile(file), depsSources);
        } catch (Exception e) {
            logger.error("Failed compilation for file : "+file.getName() + " ,Exception is : " + e.getMessage());
            throw e;
        }
    }

	@Override
	public Map<String, ? extends Serializable> loadSystemProperties(List<String> systemPropertyFiles) throws IOException {
		if(CollectionUtils.isEmpty(systemPropertyFiles)) {
			Collection<File> spFiles = FileUtils.listFiles(new File("."), SP_EXT, true);
			spFiles = select(spFiles, having(on(File.class).getPath(), containsString(SP_DIR)));
			systemPropertyFiles = convert(spFiles, new Converter<File, String>() {
				@Override
				public String convert(File from) {
					return from.getPath();
				}
			});
		}
		if(CollectionUtils.isEmpty(systemPropertyFiles)) return null;
		Map<String, Serializable> result = new HashMap<>();
		for(String spFile : systemPropertyFiles) {
			logger.info("Loading " + spFile);
			result.putAll((Map<String, ? extends Serializable>)yaml.load(FileUtils.readFileToString(new File(spFile))));
		}
		return result;
	}

}
