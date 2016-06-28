package io.cloudslang.lang.compiler.validator;

import io.cloudslang.lang.compiler.modeller.model.Executable;

import java.util.List;
import java.util.Map;

/**
 * User: bancl
 * Date: 5/16/2016
 */
public interface CompileValidator {

    List<RuntimeException> validateModelWithDependencies(Executable executable, Map<String, Executable> filteredDependencies);

    List<RuntimeException> validateModelWithDirectDependencies(Executable executable, Map<String, Executable> directDependencies);

}
