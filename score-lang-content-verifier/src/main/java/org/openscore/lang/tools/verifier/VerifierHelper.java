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

/**
 * Verifies all files with extensions: .sl, .sl.yaml or .sl.yml in a given directory are valid
 */
public class VerifierHelper {

    @Autowired
    private SlangCompiler slangCompiler;

    @Autowired
    private ScoreCompiler scoreCompiler;

    private final static Logger log = Logger.getLogger(SlangFilesVerifier.class);

    private String[] SLANG_FILE_EXTENSIONS = {"sl", "sl.yaml", "sl.yml"};

    private Map<String, Executable> slangModels = new HashMap<>();

    private Map<String, CompilationArtifact> compiledArtifacts = new HashMap<>();

    /**
     * Transform all Slang files in given directory to Slang models, and store them
     * @param directoryPath given directory containing all Slang files
     */
    public void verifyAllSlangFilesInDirAreValid(String directoryPath){
        transformSlangFilesInDirToModels(directoryPath);
        compileAllSlangModels();
    }

    private void transformSlangFilesInDirToModels(String directoryPath) {
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
            throw new RuntimeException("We found: " + slangFiles.size() + " .sl files in path: " + directoryPath + ". We managed to create slang models for only: " + slangModels.size());
        }
    }

    private void compileAllSlangModels()  {
        Collection<Executable> models = slangModels.values();
        for(Executable slangModel : models) {
            try {
                Set<Executable> dependenciesModels = getModelDependenciesRecursively(slangModel);
                CompilationArtifact compiledSource = compiledArtifacts.get(slangModel.getName());
                if (compiledSource == null) {
                    compiledSource = scoreCompiler.compile(slangModel, dependenciesModels);
                    log.info("Compiled: " + slangModel.getName() + " successfully");
                    compiledArtifacts.put(slangModel.getName(), compiledSource);
                } else {
                    log.error("Failed to compile: " + slangModel.getName());
                }
            } catch (Exception e) {
                log.error("Failed compiling Slang source: " + slangModel.getName() + ". Exception is : " + e.getMessage());
                throw e;
            }
        }
        if(compiledArtifacts.size() != slangModels.size()){
            throw new RuntimeException("Out of: " + slangModels.size() + " slang models, we managed to compile only: " + slangModels.size());
        }
        String successMessage = "Successfully finished Compilation of: " + compiledArtifacts.size() + " Slang files";
        log.info(successMessage);
        System.out.println(successMessage);
    }

    private Set<Executable> getModelDependenciesRecursively(Executable slangModel) {
        Map<String, SlangFileType> sourceDependencies = slangModel.getDependencies();
        Set<Executable> dependenciesModels = new HashSet<>();
        for (String dependencyName : sourceDependencies.keySet()) {
            Executable dependency = slangModels.get(dependencyName);
            if(dependency == null){
                throw new RuntimeException("Failed compiling slang source: " + slangModel.getName() + ". Missing dependency: " + dependencyName);
            }
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
