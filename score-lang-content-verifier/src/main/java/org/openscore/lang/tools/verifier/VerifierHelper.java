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


    /**
     * Transform all Slang files in given directory to Slang models, and store them
     * @param directoryPath given directory containing all Slang files
     */
    public void verifyAllSlangFilesInDirAreValid(String directoryPath){
        Map<String, Executable> slangModels = transformSlangFilesInDirToModels(directoryPath);
        compileAllSlangModels(slangModels);
    }

    private Map<String, Executable> transformSlangFilesInDirToModels(String directoryPath) {
        Validate.notNull(directoryPath, "You mut specify a path");
        Map<String, Executable> slangModels = new HashMap<>();
        Collection<File> slangFiles = FileUtils.listFiles(new File(directoryPath), SLANG_FILE_EXTENSIONS, true);
        log.info("Start compiling all slang files under: " + directoryPath);
        log.info(slangFiles.size() + " .sl files were found");
        for(File slangFile: slangFiles){
            Validate.isTrue(slangFile.isFile(), "file path \'" + slangFile.getAbsolutePath() + "\' must lead to a file");
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
            throw new RuntimeException("Found: " + slangFiles.size() + " .sl files in path: " + directoryPath + ". Managed to create slang models for only: " + slangModels.size());
        }
        return slangModels;
    }

    private void compileAllSlangModels(Map<String, Executable> slangModels)  {
        Collection<Executable> models = slangModels.values();
        Map<String, CompilationArtifact> compiledArtifacts = new HashMap<>();
        for(Executable slangModel : models) {
            try {
                Set<Executable> dependenciesModels = getModelDependenciesRecursively(slangModels, slangModel);
                CompilationArtifact compiledSource = compiledArtifacts.get(slangModel.getName());
                if (compiledSource == null) {
                    compiledSource = scoreCompiler.compile(slangModel, dependenciesModels);
                    log.info("Compiled: " + slangModel.getName() + " successfully");
                    compiledArtifacts.put(slangModel.getName(), compiledSource);
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

    private Set<Executable> getModelDependenciesRecursively(Map<String, Executable> slangModels, Executable slangModel) {
        Map<String, SlangFileType> sourceDependencies = slangModel.getDependencies();
        Set<Executable> dependenciesModels = new HashSet<>();
        for (String dependencyName : sourceDependencies.keySet()) {
            Executable dependency = slangModels.get(dependencyName);
            if(dependency == null){
                throw new RuntimeException("Failed compiling slang source: " + slangModel.getName() + ". Missing dependency: " + dependencyName);
            }
            dependenciesModels.add(dependency);
            dependenciesModels.addAll(getModelDependenciesRecursively(slangModels, dependency));
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
