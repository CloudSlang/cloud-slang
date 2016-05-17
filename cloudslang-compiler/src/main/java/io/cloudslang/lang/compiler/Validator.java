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

    List<RuntimeException> validateRequiredNonPrivateInputs(
            Executable executable,
            Map<String, Executable> filteredDependencies);

    /**
     * Validate that for all the steps in the flow, all results from the referenced operation or flow have matching navigations
     * If the given {@link io.cloudslang.lang.compiler.modeller.model.Executable} is an operation, the method does nothing
     * Throws {@link java.lang.IllegalArgumentException} if:
     *      - Any reference of the executable is missing
     *      - There is a missing navigation for one of the steps' references' results
     *
     * @param executable the flow to validate
     * @param filteredDependencies a map holding for each reference name, its {@link io.cloudslang.lang.compiler.modeller.model.Executable} object
     */
    List<RuntimeException> validateAllDependenciesResultsHaveMatchingNavigations(Executable executable,
                                                                                 Map<String, Executable> filteredDependencies);
}
