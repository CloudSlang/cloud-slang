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
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.endsWithIgnoreCase;

@Component
public class StaticValidatorImpl implements StaticValidator {

    private static final Pattern PATTERN = Pattern.compile("^[a-zA-Z_0-9-.]+$");

    @Override
    public void validateSlangFile(File slangFile, Executable executable, Metadata metadata,
                                  boolean shouldValidateDescription, Queue<RuntimeException> exceptions) {
        validateNamespace(slangFile, executable, exceptions);

        validateExecutableName(slangFile, executable, exceptions);

        if (shouldValidateDescription) {
            validateExecutableAgainstMetadata(executable, metadata, exceptions);
        }
    }

    private void validateExecutableAgainstMetadata(Executable executable, Metadata metadata,
                                                   Queue<RuntimeException> exceptions) {
        validateInOutParams(metadata.getInputs(), executable.getInputs(),
                String.format("Error for executable %1$s: Input", executable.getId()), exceptions);
        validateInOutParams(metadata.getOutputs(), executable.getOutputs(),
                String.format("Error for executable %1$s: Output", executable.getId()), exceptions);
        validateInOutParams(metadata.getResults(), executable.getResults(),
                String.format("Error for executable %1$s: Result", executable.getId()), exceptions);
    }

    private void validateInOutParams(Map<String, String> metadataInOutParams,
                                     List<? extends InOutParam> inOutParams, String errorMessagePrefix,
                                     Queue<RuntimeException> exceptions) {
        for (InOutParam inOutParam : ListUtils.emptyIfNull(inOutParams)) {
            if (MapUtils.isEmpty(metadataInOutParams)) {
                exceptions.add(new MetadataMissingException(errorMessagePrefix +
                        "s are missing description entirely."));
            } else if (metadataInOutParams.get(inOutParam.getName()) == null &&
                    (!(inOutParam instanceof Input) || !((Input) inOutParam).isPrivateInput())) {
                exceptions.add(new MetadataMissingException(errorMessagePrefix + " '" + inOutParam.getName() +
                        "' is missing description."));
            }
        }
    }

    private void validateNamespace(File slangFile, Executable executable, Queue<RuntimeException> exceptions) {
        // Validate that the namespace is not empty
        String namespace = executable.getNamespace();
        addExceptionIfEmptyString(namespace, "Error validating Slang file: \'" + slangFile.getAbsoluteFile() +
                "\'. Namespace of slang source: \'" + executable.getName() + "\' cannot be empty.", exceptions);

        // Validate that the namespace matches the path of the file
        String executableNamespacePath = namespace.replace('.', File.separatorChar);
        String namespaceErrorMessage = "Error validating Slang file: \'" + slangFile.getAbsoluteFile() +
                "\'. Namespace of slang source: " + executable.getName() + " is wrong.\nIt is currently \'" +
                namespace + "\', but it should match the file path: \'" + slangFile.getPath() + "\'";
        String filePathWithoutFileName = slangFile.getParent();
        addExceptionIfTrue(endsWithIgnoreCase(filePathWithoutFileName, executableNamespacePath),
                namespaceErrorMessage, exceptions);

        // Validate that the namespace is composed only of abc letters, _ or -
        Matcher matcher = PATTERN.matcher(namespace);
        addExceptionIfTrue(matcher.matches(), "Namespace: " + namespace + " is invalid. It can contain only " +
                "alphanumeric characters, underscore or hyphen", exceptions);
    }

    private void validateExecutableName(File slangFile, Executable executable, Queue<RuntimeException> exceptions) {
        // Validate executable name is the same as the file name
        String fileNameNoExtension = Extension.removeExtension(slangFile.getName());
        String executableNameErrorMessage = "Error validating Slang file: \'" + slangFile.getAbsoluteFile() +
                "\'. Name of flow or operation: \'" + executable.getName() +
                "\' is not valid.\nIt should be identical to the file name: \'" + fileNameNoExtension + "\'";
        addExceptionIfTrue(fileNameNoExtension.equals(executable.getName()), executableNameErrorMessage, exceptions);
    }

    private void addExceptionIfEmptyString(String string, String message, Queue<RuntimeException> exceptions) {
        if (StringUtils.isEmpty(string)) {
            exceptions.add(new RuntimeException(message));
        }
    }

    private void addExceptionIfTrue(boolean expression, String message, Queue<RuntimeException> exceptions) {
        if (!expression) {
            exceptions.add(new RuntimeException(message));
        }
    }
}
