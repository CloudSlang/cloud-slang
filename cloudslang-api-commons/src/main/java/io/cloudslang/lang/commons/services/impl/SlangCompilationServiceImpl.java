/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.commons.services.impl;

import io.cloudslang.lang.api.Slang;
import io.cloudslang.lang.commons.services.api.CompilationHelper;
import io.cloudslang.lang.commons.services.api.SlangCompilationService;
import io.cloudslang.lang.compiler.Extension;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.modeller.result.CompilationModellingResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.fusesource.jansi.Ansi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SlangCompilationServiceImpl implements SlangCompilationService {

    private static final Logger logger = Logger.getLogger(SlangCompilationServiceImpl.class);

    @Autowired
    private Slang slang;

    @Override
    public List<CompilationModellingResult> compileFoldersTemp(List<String> foldersPaths,
                                                               CompilationHelper compilationHelper) {
        List<CompilationModellingResult> results = new ArrayList<>();
        try {
            Set<SlangSource> dependencySources = getSourcesFromFolders(foldersPaths);
            for (SlangSource dependencySource : dependencySources) {
                File file = getFile(dependencySource.getFilePath());
                compilationHelper.repeat(Ansi.Color.GREEN, "Compiling " + file.getName());
                try {
                    CompilationModellingResult result = slang.compileSource(dependencySource, dependencySources);
                    result.setFile(file);
                    results.add(result);
                } catch (Exception e) {
                    logger.error("Failed compilation for file : " + file.getName() +
                            " ,Exception is : " + e.getMessage());
                }
            }
        } finally {
            slang.compileCleanUp();
            compilationHelper.finish();
        }
        return results;
    }

    @Override
    public File getFile(String filePath) {
        Validate.notNull(filePath, "File path can not be null");
        File file = new File(filePath);
        Validate.isTrue(file.isFile(), "File: " + file.getName() + " was not found");
        Extension.validateSlangFileExtension(file.getName());
        return file;
    }

    @Override
    public Set<SlangSource> getSourcesFromFolders(List<String> dependencies) {
        Set<SlangSource> dependencySources = new HashSet<>();
        for (String dependency : dependencies) {
            Collection<File> dependenciesFiles = listSlangFiles(new File(dependency), true);
            for (File dependencyCandidate : dependenciesFiles) {
                SlangSource source = SlangSource.fromFile(dependencyCandidate);
                dependencySources.add(source);
            }
        }
        return dependencySources;
    }

    // e.g. exclude .prop.sl from .sl set
    private Collection<File> listSlangFiles(File directory, boolean recursive) {
        Validate.isTrue(directory.isDirectory(), "Parameter '" + directory.getPath() +
                INVALID_DIRECTORY_ERROR_MESSAGE_SUFFIX);
        Collection<File> dependenciesFiles = FileUtils.listFiles(directory,
                Extension.getSlangFileExtensionValues(), recursive);
        Collection<File> result = new ArrayList<>();
        for (File file : dependenciesFiles) {
            if (Extension.SL.equals(Extension.findExtension(file.getName()))) {
                result.add(file);
            }
        }
        return result;
    }
}
