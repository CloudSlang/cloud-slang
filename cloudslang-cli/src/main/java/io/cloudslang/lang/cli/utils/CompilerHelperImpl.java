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
import io.cloudslang.lang.api.Slang;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.entities.SystemProperty;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
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
import java.util.*;

import static ch.lambdaj.Lambda.convert;

/**
 * @author lesant
 * @since 11/13/2014
 * @version $Id$
 */
@Component
public class CompilerHelperImpl implements CompilerHelper{

    private static final Logger logger = Logger.getLogger(CompilerHelperImpl.class);
    private static final String[] SLANG_FILE_EXTENSIONS = {"sl", "sl.yaml", "sl.yml"};
    private static final String[] PROPERTIES_FILE_EXTENSIONS = {"prop.sl"};
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
        validateFileExtension(file, SLANG_FILE_EXTENSIONS);

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
            Collection<File> dependenciesFiles = listFiles(new File(dependency), SLANG_FILE_EXTENSIONS, true, PROPERTIES_FILE_EXTENSIONS);
            for (File dependencyCandidate : dependenciesFiles) {
                SlangSource source = SlangSource.fromFile(dependencyCandidate);
                depsSources.add(source);
            }
        }
        try {
            return slang.compile(SlangSource.fromFile(file), depsSources);
        } catch (Exception e) {
            logger.error("Failed compilation for file : "+file.getName() + " ,Exception is : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Set<SystemProperty> loadSystemProperties(List<String> systemPropertyFiles) {
        return loadPropertiesFromFiles(convertToFiles(systemPropertyFiles), PROPERTIES_FILE_EXTENSIONS, SP_DIR);
    }

    @Override
    public Map<String, Serializable> loadInputsFromFile(List<String> inputFiles) {
        return loadMapsFromFiles(convertToFiles(inputFiles), YAML_FILE_EXTENSIONS, INPUT_DIR);
    }

    private Map<String, Serializable> loadMapsFromFiles(List<File> files, String[] extensions, String directory) {
        Collection<File> fileCollection;
        if(CollectionUtils.isEmpty(files)) {
            fileCollection = loadDefaultFiles(extensions, directory, false);
            if(CollectionUtils.isEmpty(fileCollection)) return null;
        } else {
            fileCollection = files;
        }
        Map<String, Serializable> result = new HashMap<>();
		for(File inputFile : fileCollection) {
			logger.info("Loading file: " + inputFile);
			try {
                String inputsFileContent = FileUtils.readFileToString(inputFile);
                Boolean emptyContent = true;
                if (StringUtils.isNotEmpty(inputsFileContent)) {
                    @SuppressWarnings("unchecked") Map<String, ? extends Serializable> inputFileYamlContent =
                            (Map<String, ? extends Serializable>) yaml.load(inputsFileContent);
                    if (MapUtils.isNotEmpty(inputFileYamlContent)) {
                        emptyContent = false;
                        result.putAll(inputFileYamlContent);
                    }
                }
                if (emptyContent){
                    throw new RuntimeException("Inputs file: " + inputFile + " is empty or does not contain valid YAML content.");
                }
			} catch(IOException ex) {
				logger.error("Error loading file: " + inputFile + ". Nested exception is: " + ex.getMessage(), ex);
				throw new RuntimeException(ex);
			}
		}
        return result;
    }

    private Set<SystemProperty> loadPropertiesFromFiles(List<File> files, String[] extensions, String directory) {
        Collection<File> fileCollection;
        if(CollectionUtils.isEmpty(files)) {
            fileCollection = loadDefaultFiles(extensions, directory, true);
            if(CollectionUtils.isEmpty(fileCollection)) return new HashSet<>();
        } else {
            fileCollection = files;
            for (File propertyFileCandidate : fileCollection) {
                validateFileExtension(propertyFileCandidate, PROPERTIES_FILE_EXTENSIONS);
            }
        }
        Set<SystemProperty> result = new HashSet<>();
        for(File propFile : fileCollection) {
            try {
                SlangSource source = SlangSource.fromFile(propFile);
                logger.info("Loading file: " + propFile);
                Set<SystemProperty> propsFromFile = slang.loadSystemProperties(source);
                result.addAll(propsFromFile);
            } catch(Throwable ex) {
                String errorMessage = "Error loading file: " + propFile + " nested exception is " + ex.getMessage();
                logger.error(errorMessage, ex);
                throw new RuntimeException(errorMessage, ex);
            }
        }
        return result;
    }

    private Collection<File> loadDefaultFiles(String[] extensions, String directory, boolean recursive) {
        Collection<File> files;
        String appHome = System.getProperty("app.home", "");
        String defaultDirectoryPath = appHome + File.separator + "bin" + File.separator + directory;
        File defaultDirectory = new File(defaultDirectoryPath);
        if (defaultDirectory.isDirectory()) {
            files = FileUtils.listFiles(defaultDirectory, extensions, recursive);
        } else {
            files = Collections.emptyList();
        }
        return files;
    }

    private void validateFileExtension(File file, String[] extensions) {
        boolean validFileExtension = hasExtension(file, extensions);
        String extensionsAsString =  Arrays.toString(extensions);
        Validate.isTrue(
                validFileExtension,
                "File: " + file.getName() + " must have one of the following extensions: " +
                        extensionsAsString.substring(1, extensionsAsString.length() - 1) + "."
        );
    }

    private Boolean hasExtension(File file, String[] extensions){
        String[] suffixes = new String[extensions.length];
        for(int i = 0; i < suffixes.length; ++i){
            suffixes[i] = "." + extensions[i];
        }
        return new SuffixFileFilter(suffixes).accept(file);
    }

    private List<File> convertToFiles(List<String> fileList) {
        return convert(fileList, new Converter<String, File>() {
            @Override
            public File convert(String from) {
                return new File(from);
            }
        });
    }

    // e.g. exclude .prop.sl from .sl set
    private Collection<File> listFiles(
            File directory,
            String[] extensions,
            boolean recursive,
            String[] excludedExtensions) {
        Collection<File> dependenciesFiles = FileUtils.listFiles(directory, extensions, recursive);
        Collection<File> result = new ArrayList<>();
        for (File file : dependenciesFiles) {
            if (!hasExtension(file, excludedExtensions)) {
                result.add(file);
            }
        }
        return result;
    }

}
