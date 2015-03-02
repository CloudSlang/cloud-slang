/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package org.openscore.lang.tools.verifier;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.openscore.lang.compiler.SlangCompiler;
import org.openscore.lang.compiler.modeller.model.Executable;
import org.openscore.lang.compiler.scorecompiler.ScoreCompiler;
import org.openscore.lang.entities.CompilationArtifact;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.openscore.lang.compiler.SlangSource.fromFile;

/*
 * Created by stoneo on 2/9/2015.
 */
/**
 * Verifies all files with extensions: .sl, .sl.yaml or .sl.yml in a given directory are valid
 */
public class SlangContentVerifier {

    @Autowired
    private SlangCompiler slangCompiler;

    @Autowired
    private ScoreCompiler scoreCompiler;

    private final static Logger log = Logger.getLogger(VerifierMain.class);

    private String[] SLANG_FILE_EXTENSIONS = {"sl", "sl.yaml", "sl.yml"};


    /**
     * Transform all Slang files in given directory to Slang models, and store them
     * @param directoryPath given directory containing all Slang files
     * @return the number of valid Slang files in the given directory
     */
    public int verifyAllSlangFilesInDirAreValid(String directoryPath){
        Map<String, Executable> slangModels = transformSlangFilesInDirToModels(directoryPath);
        return compileAllSlangModels(slangModels);
    }

    private Map<String, Executable> transformSlangFilesInDirToModels(String directoryPath) {
        Validate.notEmpty(directoryPath, "You must specify a path");
        Validate.isTrue(new File(directoryPath).isDirectory(), "Directory path argument \'" + directoryPath + "\' does not lead to a directory");
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
                String errorMessage = "Failed creating Slang models for file: \'" + slangFile.getAbsoluteFile() + "\'.\n" + e.getMessage();
                log.error(errorMessage);
                throw new RuntimeException(errorMessage, e);
            }
            if(sourceModel != null) {
                staticSlangFileValidation(slangFile, sourceModel);
                slangModels.put(getUniqueName(sourceModel), sourceModel);
            }
        }
        if(slangFiles.size() != slangModels.size()){
            throw new RuntimeException("Some Slang files were not pre-compiled.\nFound: " + slangFiles.size() +
                    " slang files in path: \'" + directoryPath + "\' But managed to create slang models for only: " + slangModels.size());
        }
        return slangModels;
    }

    private int compileAllSlangModels(Map<String, Executable> slangModels)  {
        Collection<Executable> models = slangModels.values();
        Map<String, CompilationArtifact> compiledArtifacts = new HashMap<>();
        for(Executable slangModel : models) {
            try {
                Set<Executable> dependenciesModels = getModelDependenciesRecursively(slangModels, slangModel);
                CompilationArtifact compiledSource = compiledArtifacts.get(slangModel.getName());
                if (compiledSource == null) {
                    compiledSource = scoreCompiler.compile(slangModel, dependenciesModels);
                    if(compiledSource != null) {
                        log.info("Compiled: \'" + slangModel.getNamespace() + "." + slangModel.getName() + "\' successfully");
                        compiledArtifacts.put(slangModel.getName(), compiledSource);
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
        if(compiledArtifacts.size() != slangModels.size()){
            throw new RuntimeException("Some Slang files were not compiled.\n" +
                    "Found: " + slangModels.size() + " slang models, but managed to compile only: " + compiledArtifacts.size());
        }
        String successMessage = "Successfully finished Compilation of: " + compiledArtifacts.size() + " Slang files";
        log.info(successMessage);
        return compiledArtifacts.size();
    }

    private Set<Executable> getModelDependenciesRecursively(Map<String, Executable> slangModels, Executable slangModel) {
        Set<Executable> dependenciesModels = new HashSet<>();
        for (String dependencyName : slangModel.getDependencies()) {
            Executable dependency = slangModels.get(dependencyName);
            if(dependency == null){
                throw new RuntimeException("Failed compiling slang source: " + slangModel.getNamespace() + "." +
                        slangModel.getName() + ". Missing dependency: " + dependencyName);
            }
            dependenciesModels.add(dependency);
            dependenciesModels.addAll(getModelDependenciesRecursively(slangModels, dependency));
        }
        return dependenciesModels;
    }

    private void staticSlangFileValidation(File slangFile, Executable executable){
        validateNamespace(slangFile, executable);

        validateExecutableName(slangFile, executable);
    }

    private void validateExecutableName(File slangFile, Executable executable) {
        // Validate executable name is the same as the file name
        String[] splitName = slangFile.getName().split("\\Q.");
        String fileNameNoExtension = splitName[0];
        String executableNameErrorMessage = "Error validating Slang file: \'" + slangFile.getAbsoluteFile() +
                "\'. Name of flow or operation: \'" + executable.getName() +
                "\' is invalid.\nIt should be identical to the file name: \'" + fileNameNoExtension + "\'";
        Validate.isTrue(fileNameNoExtension.equals(executable.getName()), executableNameErrorMessage);
    }

    private void validateNamespace(File slangFile, Executable executable) {
        // Validate that the namespace is not empty
        String namespace = executable.getNamespace();
        Validate.notEmpty(namespace, "Error validating Slang file: \'" + slangFile.getAbsoluteFile() +
                "\'. Namespace of slang source: \'" + executable.getName() + "\' cannot be empty.");

        // Validate that the namespace matches the path of the file
        String executableNamespacePath = namespace.replace('.', File.separatorChar);
        String namespaceErrorMessage = "Error validating Slang file: \'" + slangFile.getAbsoluteFile() +
                "\'. Namespace of slang source: " + executable.getName() + " is wrong.\nIt is currently \'" +
                namespace + "\', but it should match the file path: \'" + slangFile.getPath() + "\'";
        int indexOfLastFileSeparator = slangFile.getAbsolutePath().lastIndexOf(File.separatorChar);
        String filePathWithoutFileName = slangFile.getAbsolutePath().substring(0, indexOfLastFileSeparator);
        Validate.isTrue(filePathWithoutFileName.toLowerCase().endsWith(executableNamespacePath.toLowerCase()), namespaceErrorMessage);

        // Validate that the namespace is composed only of abc letters, _ or -
        Pattern pattern = Pattern.compile("^[\\w-\\.]+$");
        Matcher matcher = pattern.matcher(namespace);
        Validate.isTrue(matcher.matches(), "Namespace: " + namespace + " is invalid. It can contain only alphanumeric characters, underscore or hyphen");
    }

    private String getUniqueName(Executable sourceModel) {
        return sourceModel.getNamespace() + "." + sourceModel.getName();
    }

}
