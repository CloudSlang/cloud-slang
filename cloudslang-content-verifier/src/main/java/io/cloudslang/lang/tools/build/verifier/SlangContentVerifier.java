/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package io.cloudslang.lang.tools.build.verifier;

import io.cloudslang.lang.compiler.Extension;
import io.cloudslang.lang.compiler.MetadataExtractor;
import io.cloudslang.lang.compiler.SlangCompiler;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.model.Metadata;
import io.cloudslang.lang.compiler.scorecompiler.ScoreCompiler;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.tools.build.validation.MetadataMissingException;
import io.cloudslang.lang.tools.build.validation.StaticValidator;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Queue;
import java.util.ArrayDeque;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by stoneo on 3/15/2015.
 **/
@Component
public class SlangContentVerifier {

    private final static Logger log = Logger.getLogger(SlangContentVerifier.class);

    @Autowired
    private SlangCompiler slangCompiler;

    @Autowired
    private MetadataExtractor metadataExtractor;

    @Autowired
    private StaticValidator staticValidator;

    @Autowired
    private ScoreCompiler scoreCompiler;

    public CompilationResult createModelsAndValidate(String directoryPath, boolean shouldValidateDescription) {
        Validate.notEmpty(directoryPath, "You must specify a path");
        Validate.isTrue(new File(directoryPath).isDirectory(), "Directory path argument \'" + directoryPath + "\' does not lead to a directory");
        Map<String, Executable> slangModels = new HashMap<>();
        Collection<File> slangFiles = listSlangFiles(new File(directoryPath), true);
        log.info("Start compiling all slang files under: " + directoryPath);
        log.info(slangFiles.size() + " .sl files were found");
        log.info("");
        Queue<RuntimeException> exceptions = new ArrayDeque<>();
        for (File slangFile: slangFiles) {
            Executable sourceModel = null;
            try {
                Validate.isTrue(slangFile.isFile(), "file path \'" + slangFile.getAbsolutePath() + "\' must lead to a file");
                SlangSource slangSource = SlangSource.fromFile(slangFile);
                sourceModel = slangCompiler.preCompile(slangSource);
                Metadata sourceMetadata = metadataExtractor.extractMetadata(slangSource);
                if (sourceModel != null) {
                    staticValidator.validateSlangFile(slangFile, sourceModel, sourceMetadata, shouldValidateDescription);
                    slangModels.put(getUniqueName(sourceModel), sourceModel);
                }
            } catch (Exception e) {
                String errorMessage = "Failed to extract metadata for file: \'" + slangFile.getAbsoluteFile() + "\'.\n" + e.getMessage();
                log.error(errorMessage);
                exceptions.add(new RuntimeException(errorMessage, e));
                if (e instanceof MetadataMissingException && sourceModel != null) {
                    slangModels.put(getUniqueName(sourceModel), sourceModel);
                }
            }
        }
        if (slangFiles.size() != slangModels.size()) {
            exceptions.add(new RuntimeException("Some Slang files were not pre-compiled.\nFound: " + slangFiles.size() +
                    " executable files in path: \'" + directoryPath + "\' But managed to create slang models for only: " + slangModels.size()));
        }
        CompilationResult compilationResult = new CompilationResult();
        compilationResult.addExceptions(exceptions);
        compilationResult.addResults(slangModels);
        return compilationResult;
    }

    public Map<String, CompilationArtifact> compileSlangModels(Map<String, Executable> slangModels) {
        Map<String, CompilationArtifact> compiledArtifacts = new HashMap<>();
        for (Map.Entry<String, Executable> slangModelEntry : slangModels.entrySet()) {
            Executable slangModel = slangModelEntry.getValue();
            try {
                CompilationArtifact compiledSource = compiledArtifacts.get(getUniqueName(slangModel));
                if (compiledSource == null) {
                    Set<Executable> dependenciesModels = getModelDependenciesRecursively(slangModels, slangModel);
                    compiledSource = scoreCompiler.compile(slangModel, dependenciesModels);
                    if (compiledSource != null) {
                        log.info("Compiled: \'" + slangModel.getNamespace() + "." + slangModel.getName() + "\' successfully");
                        compiledArtifacts.put(getUniqueName(slangModel), compiledSource);
                    } else {
                        log.error("Failed to compile source: \'" + slangModel.getNamespace() + "." + slangModel.getName() + "\'");
                    }
                }
            } catch (Exception e) {
                String errorMessage = "Failed compiling Slang source: \'" + slangModel.getNamespace() + "." + slangModel.getName() + "\'.\n" + e.getMessage();
                log.error(errorMessage);
                throw new RuntimeException(errorMessage, e);
            }
        }
        return compiledArtifacts;
    }

    private Set<Executable> getModelDependenciesRecursively(Map<String, Executable> slangModels, Executable slangModel) {
        Set<Executable> dependenciesModels = new HashSet<>();
        for (String dependencyName : slangModel.getExecutableDependencies()) {
            Executable dependency = slangModels.get(dependencyName);
            if (dependency == null) {
                throw new RuntimeException("Failed compiling slang source: " + slangModel.getNamespace() + "." +
                        slangModel.getName() + ". Missing dependency: " + dependencyName);
            }
            dependenciesModels.add(dependency);
            dependenciesModels.addAll(getModelDependenciesRecursively(slangModels, dependency));
        }
        return dependenciesModels;
    }

    private String getUniqueName(Executable sourceModel) {
        return sourceModel.getNamespace() + "." + sourceModel.getName();
    }

    // e.g. exclude .prop.sl from .sl set
    private Collection<File> listSlangFiles(File directory, boolean recursive) {
        Collection<File> dependenciesFiles = FileUtils.listFiles(directory, Extension.getSlangFileExtensionValues(), recursive);
        Collection<File> result = new ArrayList<>();
        for (File file : dependenciesFiles) {
            if (Extension.SL.equals(Extension.findExtension(file.getName()))) {
                result.add(file);
            }
        }
        return result;
    }

}
