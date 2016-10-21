/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.tools.build.validation;

import io.cloudslang.lang.compiler.Extension;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.model.Metadata;
import io.cloudslang.lang.entities.bindings.InOutParam;
import io.cloudslang.lang.entities.bindings.Input;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;

/**
 * Created by bancl on 8/30/2016.
 */
@Component
public class StaticValidatorImpl implements StaticValidator {

    // TODO correct this regex
    private static final Pattern PATTERN = Pattern.compile("^[a-zA-Z_0-9-.]+$");

    @Override
    public void validateSlangFile(File slangFile, Executable executable, Metadata metadata,
                                  boolean shouldValidateDescription) {
        validateNamespace(slangFile, executable);

        validateExecutableName(slangFile, executable);

        if (shouldValidateDescription) {
            validateExecutableAgainstMetadata(executable, metadata);
        }
    }

    private void validateExecutableAgainstMetadata(Executable executable, Metadata metadata) {
        validateInOutParams(metadata.getInputs(), executable.getInputs(),
                String.format("Error for executable %1$s: Input", executable.getId()));
        validateInOutParams(metadata.getOutputs(), executable.getOutputs(),
                String.format("Error for executable %1$s: Output", executable.getId()));
        validateInOutParams(metadata.getResults(), executable.getResults(),
                String.format("Error for executable %1$s: Result", executable.getId()));
    }

    private void validateInOutParams(Map<String, String> metadataInOutParams,
                                     List<? extends InOutParam> inOutParams, String errorMessagePrefix) {
        for (InOutParam inOutParam : ListUtils.emptyIfNull(inOutParams)) {
            if (metadataInOutParams == null) {
                throw new MetadataMissingException(errorMessagePrefix + "s are missing description entirely.");
            } else if (metadataInOutParams.get(inOutParam.getName()) == null &&
                    (!(inOutParam instanceof Input) || !((Input) inOutParam).isPrivateInput())) {
                throw new MetadataMissingException(errorMessagePrefix + " '" + inOutParam.getName() +
                        "' is missing description.");
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
        String filePathWithoutFileName = slangFile.getParent();
        Validate.isTrue(endsWithIgnoreCase(filePathWithoutFileName, executableNamespacePath), namespaceErrorMessage);

        // Validate that the namespace is composed only of abc letters, _ or -
        Matcher matcher = PATTERN.matcher(namespace);
        Validate.isTrue(matcher.matches(), "Namespace: " + namespace + " is invalid. It can contain only " +
                "alphanumeric characters, underscore or hyphen");
    }

    private void validateExecutableName(File slangFile, Executable executable) {
        // Validate executable name is the same as the file name
        String fileNameNoExtension = Extension.removeExtension(slangFile.getName());
        String executableNameErrorMessage = "Error validating Slang file: \'" + slangFile.getAbsoluteFile() +
                "\'. Name of flow or operation: \'" + executable.getName() +
                "\' is not valid.\nIt should be identical to the file name: \'" + fileNameNoExtension + "\'";
        Validate.isTrue(fileNameNoExtension.equals(executable.getName()), executableNameErrorMessage);
    }
}
