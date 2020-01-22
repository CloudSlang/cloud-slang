/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.runtime.bindings;

import io.cloudslang.dependency.impl.services.utils.UnzipUtil;

import java.io.File;

public class BaseTestClass {
    static {
        ClassLoader classLoader = BaseTestClass.class.getClassLoader();
        String resource = classLoader.getResource("python-3.8.0.zip").getPath();
        String pythonHome = new File(resource).getParent();
        String pythonPath = new File(pythonHome, "python").getAbsolutePath();
        UnzipUtil.unzipToFolder(pythonPath, classLoader.getResourceAsStream("python-3.8.0.zip"));
        System.setProperty("python.path", new File(pythonPath, "python-3.8.0").getAbsolutePath());
    }
}
