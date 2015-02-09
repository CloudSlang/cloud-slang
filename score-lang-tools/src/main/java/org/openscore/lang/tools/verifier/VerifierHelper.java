/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.openscore.lang.tools.verifier;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.openscore.api.ExecutionPlan;
import org.openscore.lang.compiler.SlangCompiler;
import org.openscore.lang.compiler.modeller.model.Executable;
import org.openscore.lang.compiler.modeller.model.SlangFileType;
import org.openscore.lang.compiler.scorecompiler.ScoreCompiler;
import org.openscore.lang.entities.CompilationArtifact;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.openscore.lang.compiler.SlangSource.fromFile;

/*
 * Created by stoneo on 2/9/2015.
 */
public class VerifierHelper {

    @Autowired
    private SlangCompiler slangCompiler;

    @Autowired
    private ScoreCompiler scoreCompiler;

    private final static Logger log = Logger.getLogger(SlangFilesVerifier.class);

    private String[] SLANG_FILE_EXTENSIONS = {"sl"};

    private Map<String, Executable> slangModels = new HashMap<>();

    private Map<String, ExecutionPlan> executionPlans = new HashMap<>();


    public void createAllSlangModelsFromDirectory(String directoryPath) throws IOException {
        log.info("Starting to compile all files under: " + directoryPath);
        Validate.notNull(directoryPath, "Directory path path can not be null");
        Collection<File> slangFiles = FileUtils.listFiles(new File(directoryPath), SLANG_FILE_EXTENSIONS, true);
        log.info("Start compiling all slang files under: " + directoryPath);
        log.info(slangFiles.size() + " .sl files were found");
        for(File slangFile: slangFiles){
            Validate.isTrue(slangFile.isFile(), "filePath must lead to a file");
            Executable sourceModel;
            try {
                sourceModel = slangCompiler.preCompile(fromFile(slangFile));
            } catch (Exception e) {
                log.error("Failed creating Slang ,models for directory: " + directoryPath + ". Exception is : " + e.getMessage());
                throw e;
            }
            verifyStaticCode(slangFile, sourceModel);
            slangModels.put(getUniqueName(sourceModel), sourceModel);
        }
        if(slangFiles.size() != slangModels.size()){
            log.warn("We found: " + slangFiles.size() + " .sl files in path: " + directoryPath + ". We managed to create slang models for only: " + slangModels.size());
        }
    }

    public void compileAllSlangModelsInDirectory() throws IOException {
        Collection<Executable> models = slangModels.values();
        for(Executable slangModel : models) {
            Set<Executable> dependenciesModels = getModelDependenciesRecursively(slangModel);
            try {
                //todo: add optimization - do not compile stuff that is already compiled
                CompilationArtifact compilationArtifact = scoreCompiler.compile(slangModel, dependenciesModels);
                if(compilationArtifact != null){
                    log.info("Compiled: " + slangModel.getName() + " successfully");
                    executionPlans.put(slangModel.getName(), compilationArtifact.getExecutionPlan());
                    Map<String, ExecutionPlan> dependencies = compilationArtifact.getDependencies();
                    if(dependencies != null){
                        for(Map.Entry<String, ExecutionPlan> dependency : dependencies.entrySet()){
                            executionPlans.put(dependency.getKey(), dependency.getValue());
                        }
                    }
                } else {
                    log.info("Failed to compile: " + slangModel.getName());
                }
            } catch (Exception e) {
                log.error("Failed compiling Slang source: " + slangModel.getName() + ". Exception is : " + e.getMessage());
                throw e;
            }
        }
        if(executionPlans.size() != slangModels.size()){
            log.warn("Out of: " + slangModels.size() + " slang models, we managed to compile only: " + slangModels.size());
        }
    }

    private Set<Executable> getModelDependenciesRecursively(Executable slangModel) {
        Map<String, SlangFileType> sourceDependencies = slangModel.getDependencies();
        Set<Executable> dependenciesModels = new HashSet<>();
        for (String dependencyName : sourceDependencies.keySet()) {
            Executable dependency = slangModels.get(dependencyName);
            dependenciesModels.add(dependency);
            dependenciesModels.addAll(getModelDependenciesRecursively(dependency));
        }
        return dependenciesModels;
    }

    private void verifyStaticCode(File slangFile, Executable executable){
        // todo: implement
    }

    private String getUniqueName(Executable sourceModel) {
        return sourceModel.getNamespace() + "." + sourceModel.getName();
    }

}
