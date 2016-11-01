/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.commons.services.impl;

import io.cloudslang.lang.commons.services.api.CompilationHelper;

import java.io.File;
import java.util.concurrent.Future;

public class NoOpCompilationHelper implements CompilationHelper {
    @Override
    public void onCompilationFinish() {
        // Do nothing
    }

    @Override
    public Future<?> onEveryFile(File file) {
        // Do nothing
        return null;
    }
}
