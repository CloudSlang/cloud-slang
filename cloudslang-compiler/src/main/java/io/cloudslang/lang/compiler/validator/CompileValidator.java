/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.validator;

import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.modeller.model.Executable;

import java.util.List;
import java.util.Map;

public interface CompileValidator {

    List<RuntimeException> validateModelWithDependencies(Executable executable,
                                                         Map<String, Executable> filteredDependencies);

    List<RuntimeException> validateModelWithDirectDependencies(Executable executable,
                                                               Map<String, Executable> directDependencies);

    List<RuntimeException> validateNoDuplicateExecutables(Executable currentExecutable,
                                                          SlangSource currentSource,
                                                          Map<Executable, SlangSource> allAvailableExecutables);

}
