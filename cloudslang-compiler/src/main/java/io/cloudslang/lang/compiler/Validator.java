package io.cloudslang.lang.compiler;

import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;

import java.util.List;
import java.util.Map;

/**
 * User: bancl
 * Date: 5/16/2016
 */
public interface Validator {
    void validateFileName(String executableName, ParsedSlang parsedSlang, ExecutableModellingResult result);

    void validateInputNamesDifferentFromOutputNames(ExecutableModellingResult result);

    List<RuntimeException> validateModelWithDependencies(
            Executable executable,
            Map<String, Executable> filteredDependencies);

}
