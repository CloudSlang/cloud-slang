/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package io.cloudslang.lang.cli.utils;

import ch.lambdaj.function.convert.Converter;

import com.google.common.collect.Lists;

import io.cloudslang.lang.api.Slang;
import io.cloudslang.lang.cli.SlangCLI;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
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

    private static final Logger logger = Logger.getLogger(CompilerHelperImpl.class);
    private String[] SLANG_FILE_EXTENSIONS = {"sl", "sl.yaml", "sl.yml"};
    private static final String[] YAML_FILE_EXTENSIONS = {"yaml", "yml"};
    private static final String SP_DIR = "properties"; //TODO reconsider it after closing CloudSlang file extensions & some real usecases
    private static final String INPUT_DIR = "inputs";

    @Autowired
    private Slang slang;
    @Autowired
    private Yaml yaml;

    @Override
	public CompilationArtifact compile(String filePath, List<String> dependencies) throws IOException {
        Validate.notNull(filePath, "File path can not be null");
        Set<SlangSource> depsSources = new HashSet<>();
        File file = new File(filePath);
        Validate.isTrue(file.isFile(), "File: " + file.getName() + " was not found");

        boolean validFileExtension = checkIsFileSupported(file);
        Validate.isTrue(validFileExtension, "File: " + file.getName() + " must have one of the following extensions: sl, sl.yaml, sl.yml");

        if (CollectionUtils.isEmpty(dependencies)) {
            dependencies = new ArrayList<>();
            //app.home is the basedir property we set in our executables
            String appHome = System.getProperty("app.home", "");
            String contentRoot = appHome + File.separator + "content";
            File contentRootDir = new File(contentRoot);
            if (StringUtils.isNotEmpty(appHome) &&
                    contentRootDir.exists() && contentRootDir.isDirectory()) {
                dependencies.add(contentRoot);
            } else {
                //default behavior is taking the parent dir if not running from our executables
                dependencies.add(file.getParent());
            }
        }
        for (String dependency:dependencies) {
            Collection<File> dependenciesFiles = FileUtils.listFiles(new File(dependency), SLANG_FILE_EXTENSIONS, true);
            dependenciesFiles = select(dependenciesFiles, having(on(File.class).getPath(), not(containsString(SP_DIR))));
            dependenciesFiles = select(dependenciesFiles, having(on(File.class).getPath(), not(containsString(INPUT_DIR))));
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
	public Map<String, ? extends Serializable> loadSystemProperties(List<String> systemPropertyFiles) {
		return loadFiles(systemPropertyFiles, YAML_FILE_EXTENSIONS, SP_DIR);
	}

    @Override
    public Map<String, ? extends Serializable> loadInputsFromFile(List<String> inputFiles) {
        return loadFiles(inputFiles, YAML_FILE_EXTENSIONS, INPUT_DIR);
    }

    private Map<String, ? extends Serializable> loadFiles(List<String> files, String[] extensions, String directory) {
        if(CollectionUtils.isEmpty(files)) {
            Collection<File> implicitFiles = FileUtils.listFiles(new File("."), extensions, false);
            implicitFiles = select(implicitFiles, having(on(File.class).getPath(), containsString(directory)));
            files = convert(implicitFiles, new Converter<File, String>() {
                @Override
                public String convert(File from) {
                    return from.getPath();
                }
            });
        }
        if(CollectionUtils.isEmpty(files)) return null;
        Map<String, Serializable> result = new HashMap<>();
		for(String inputFile : files) {
			logger.info("Loading file: " + inputFile);
			try {
                String inputsFileContent = FileUtils.readFileToString(new File(inputFile));
                if (StringUtils.isNotEmpty(inputsFileContent)) {
                    @SuppressWarnings("unchecked") Map<String, ? extends Serializable> inputFileYamlContent =
                            (Map<String, ? extends Serializable>) yaml.load(inputsFileContent);
                    result.putAll(inputFileYamlContent);
                }
			} catch(IOException ex) {
				logger.error("Error loading file: " + inputFile, ex);
				throw new RuntimeException(ex);
			}
		}
        return result;
    }
    private Boolean checkIsFileSupported(File file){
        String[] suffixes = new String[SLANG_FILE_EXTENSIONS.length];
        for(int i = 0; i < suffixes.length; ++i){
            suffixes[i] = "." + SLANG_FILE_EXTENSIONS[i];
        }
        return new SuffixFileFilter(suffixes).accept(file);
    }

}
