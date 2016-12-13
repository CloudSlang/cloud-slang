/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build.verifier;

import io.cloudslang.lang.commons.services.api.SlangCompilationService;
import io.cloudslang.lang.compiler.MetadataExtractor;
import io.cloudslang.lang.compiler.SlangCompiler;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.model.Metadata;
import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import io.cloudslang.lang.compiler.modeller.result.MetadataModellingResult;
import io.cloudslang.lang.compiler.scorecompiler.ScoreCompiler;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.logging.LoggingService;
import io.cloudslang.lang.tools.build.validation.MetadataMissingException;
import io.cloudslang.lang.tools.build.validation.StaticValidator;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Created by stoneo on 3/15/2015.
 **/
@Component
public class SlangContentVerifier {

    @Autowired
    private SlangCompiler slangCompiler;

    @Autowired
    private MetadataExtractor metadataExtractor;

    @Autowired
    private StaticValidator staticValidator;

    @Autowired
    private ScoreCompiler scoreCompiler;

    @Autowired
    private LoggingService loggingService;

    @Autowired
    private SlangCompilationService slangCompilationService;

    public PreCompileResult createModelsAndValidate(String directoryPath, boolean shouldValidateDescription) {
        Validate.notEmpty(directoryPath, "You must specify a path");
        Validate.isTrue(new File(directoryPath).isDirectory(), "Directory path argument \'" +
                directoryPath + "\' does not lead to a directory");
        Map<String, Executable> slangModels = new HashMap<>();
        Collection<File> slangFiles = slangCompilationService.listSlangFiles(new File(directoryPath), true);
        loggingService.logEvent(Level.INFO, "Start compiling all slang files under: " + directoryPath);
        loggingService.logEvent(Level.INFO, slangFiles.size() + " .sl files were found");
        loggingService.logEvent(Level.INFO, "");
        Queue<RuntimeException> exceptions = new ArrayDeque<>();
        for (File slangFile: slangFiles) {
            Executable sourceModel = null;
            try {
                Validate.isTrue(slangFile.isFile(), "file path \'" + slangFile.getAbsolutePath() +
                        "\' must lead to a file");
                SlangSource slangSource = SlangSource.fromFile(slangFile);

                ExecutableModellingResult preCompileResult = slangCompiler.preCompileSource(slangSource);
                sourceModel = preCompileResult.getExecutable();
                exceptions.addAll(preCompileResult.getErrors());

                MetadataModellingResult metadataResult = metadataExtractor.extractMetadataModellingResult(slangSource);
                Metadata sourceMetadata = metadataResult.getMetadata();
                exceptions.addAll(metadataResult.getErrors());

                if (sourceModel != null) {
                    int size = exceptions.size();
                    staticValidator.validateSlangFile(slangFile, sourceModel,
                                    sourceMetadata, shouldValidateDescription, exceptions);
                    if (size == exceptions.size()) {
                        slangModels.put(getUniqueName(sourceModel), sourceModel);
                    }
                }
            } catch (Exception e) {
                String errorMessage = "Failed to extract metadata for file: \'" +
                        slangFile.getAbsoluteFile() + "\'.\n" + e.getMessage();
                loggingService.logEvent(Level.ERROR, errorMessage);
                exceptions.add(new RuntimeException(errorMessage, e));
                if (e instanceof MetadataMissingException && sourceModel != null) {
                    slangModels.put(getUniqueName(sourceModel), sourceModel);
                }
            }
        }
        if (slangFiles.size() != slangModels.size()) {
            exceptions.add(new RuntimeException("Some Slang files were not pre-compiled.\nFound: " + slangFiles.size() +
                    " executable files in path: \'" + directoryPath +
                    "\' But managed to create slang models for only: " + slangModels.size()));
        }
        PreCompileResult preCompileResult = new PreCompileResult();
        preCompileResult.addExceptions(exceptions);
        preCompileResult.addResults(slangModels);
        return preCompileResult;
    }

    public CompileResult compileSlangModels(Map<String, Executable> slangModels) {
        CompileResult compileResult = new CompileResult();
        Map<String, CompilationArtifact> compiledArtifacts = new HashMap<>();
        for (Map.Entry<String, Executable> slangModelEntry : slangModels.entrySet()) {
            Executable slangModel = slangModelEntry.getValue();
            try {
                CompilationArtifact compiledSource = compiledArtifacts.get(getUniqueName(slangModel));
                if (compiledSource == null) {
                    Set<Executable> dependenciesModels = getModelDependenciesRecursively(slangModels, slangModel);
                    compiledSource = scoreCompiler.compile(slangModel, dependenciesModels);
                    if (compiledSource != null) {
                        loggingService.logEvent(Level.INFO, "Compiled: \'" + slangModel.getNamespace() + "." +
                                slangModel.getName() + "\' successfully");
                        compiledArtifacts.put(getUniqueName(slangModel), compiledSource);
                    } else {
                        loggingService.logEvent(Level.ERROR, "Failed to compile source: \'" +
                                slangModel.getNamespace() + "." + slangModel.getName() + "\'");
                    }
                }
            } catch (Exception e) {
                String errorMessage = "Failed compiling Slang source: \'" + slangModel.getNamespace() + "." +
                        slangModel.getName() + "\'.\n" + e.getMessage();
                loggingService.logEvent(Level.ERROR, errorMessage);
                compileResult.addException(new RuntimeException(errorMessage, e));
            }
        }

        compileResult.addResults(compiledArtifacts);
        return compileResult;
    }

    private Set<Executable> getModelDependenciesRecursively(Map<String, Executable> slangModels,
                                                            Executable slangModel) {
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

}
