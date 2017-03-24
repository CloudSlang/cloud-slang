/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.parser.utils;

import io.cloudslang.lang.compiler.SlangSource;
import java.util.List;

public interface MetadataValidator {
    List<RuntimeException> validateCheckstyle(SlangSource source);
}
