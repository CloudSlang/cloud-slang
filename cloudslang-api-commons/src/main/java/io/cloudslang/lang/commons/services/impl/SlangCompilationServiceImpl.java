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
import io.cloudslang.lang.compiler.PrecompileStrategy;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.modeller.result.CompilationModellingResult;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SlangCompilationServiceImpl implements SlangCompilationService {

    private static final Logger logger = Logger.getLogger(SlangCompilationServiceImpl.class);

    @Autowired
    private Slang slang;

    @Override
    public List<CompilationModellingResult> compileFolders(final List<String> foldersPaths,
                                                           final CompilationHelper compilationHelper) {
        List<CompilationModellingResult> results = new ArrayList<>();
        try {
            Set<SlangSource> dependencySources = getSourcesFromFolders(foldersPaths);
            for (SlangSource dependencySource : dependencySources) {
                File file = getFile(dependencySource.getFilePath());
                compilationHelper.onEveryFile(file);
                try {
                    CompilationModellingResult result =
                            slang.compileSource(dependencySource, dependencySources, PrecompileStrategy.WITH_CACHE);
                    result.setFile(file);
                    results.add(result);
                } catch (Exception e) {
                    logger.error("Failed compilation for file : " + file.getName() +
                            " ,Exception is : " + e.getMessage());
                }
            }
        } finally {
            compilationHelper.onCompilationFinish();
            slang.invalidateAllInPreCompileCache();
        }
        return results;
    }

    @Override
    public File getFile(final String filePath) {
        Validate.notNull(filePath, "File path can not be null");
        File file = new File(filePath);
        Validate.isTrue(file.isFile(), "File: " + file.getName() + " was not found");
        Extension.validateSlangFileExtension(file.getName());
        return file;
    }

    @Override
    public Set<SlangSource> getSourcesFromFolders(final List<String> dependencies) {
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

    @Override
    public Collection<File> listSlangFiles(File directory, boolean recursive) {
        Validate.isTrue(directory.isDirectory(), "Parameter '" + directory.getPath() +
                INVALID_DIRECTORY_ERROR_MESSAGE_SUFFIX);
        return FileUtils.listFiles(directory,
                new IOFileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return Extension.SL == Extension.findExtension(file.getName());
                    }

                    @Override
                    public boolean accept(File file, String name) {
                        return Extension.SL == Extension.findExtension(name);
                    }
                }, recursive ? TrueFileFilter.INSTANCE : null);
    }
}
