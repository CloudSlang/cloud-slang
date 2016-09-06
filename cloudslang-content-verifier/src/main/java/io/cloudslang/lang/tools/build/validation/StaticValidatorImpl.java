/*
 * (c) Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package io.cloudslang.lang.tools.build.validation;

import io.cloudslang.lang.compiler.Extension;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.model.Metadata;
import io.cloudslang.lang.entities.bindings.InOutParam;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.Validate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bancl on 8/30/2016.
 */
@Component
public class StaticValidatorImpl implements StaticValidator {

    @Override
    public void validateSlangFile(File slangFile, Executable executable, Metadata metadata, Boolean shouldValidateDescription){
        validateNamespace(slangFile, executable);

        validateExecutableName(slangFile, executable);

        if (shouldValidateDescription) {
            validateExecutableAgainstMetadata(executable, metadata);
        }
    }

    private void validateExecutableAgainstMetadata(Executable executable, Metadata metadata) {
        validateInOutParams(metadata.getInputs(), executable.getInputs(), "Error for executable " +
                executable.getId() + ": Input");
        validateInOutParams(metadata.getOutputs(), executable.getOutputs(), "Error for executable " +
                executable.getId() + ": Output");
        validateInOutParams(metadata.getResults(), executable.getResults(), "Error for executable " +
                executable.getId() + ": Result");
    }

    private void validateInOutParams(Map<String, String> metadataInOutParams, List<? extends InOutParam> inOutParams, String errorMessagePrefix) {
        for (InOutParam inOutParam : ListUtils.emptyIfNull(inOutParams)) {
            if (metadataInOutParams == null) {
                throw new RuntimeException(errorMessagePrefix + "s are missing description entirely.");
            } else if (metadataInOutParams.get(inOutParam.getName()) == null) {
                throw new RuntimeException(errorMessagePrefix + " '" + inOutParam.getName() + "' is missing description.");
            }
        }
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

    private void validateExecutableName(File slangFile, Executable executable) {
        // Validate executable name is the same as the file name
        String fileNameNoExtension = Extension.removeExtension(slangFile.getName());
        String executableNameErrorMessage = "Error validating Slang file: \'" + slangFile.getAbsoluteFile() +
                "\'. Name of flow or operation: \'" + executable.getName() +
                "\' is invalid.\nIt should be identical to the file name: \'" + fileNameNoExtension + "\'";
        Validate.isTrue(fileNameNoExtension.equals(executable.getName()), executableNameErrorMessage);
    }
}
