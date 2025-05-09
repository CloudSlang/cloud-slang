/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler;

import java.util.Objects;

public class SlangByteSource {

    private final byte[] source;
    private final String name;
    private final String filePath;
    private final Extension fileExtension;

    public SlangByteSource(byte[] source,
                           String name,
                           String filePath,
                           Extension fileExtension) {
        Objects.requireNonNull(source, "Source cannot be null");
        this.source = source;
        this.name = name;
        this.filePath = filePath;
        this.fileExtension = fileExtension;
    }

    public SlangByteSource(byte[] source, String name) {
        Objects.requireNonNull(source, "Source cannot be null");
        this.source = source;
        this.name = name;
        this.filePath = null;
        this.fileExtension = null;
    }

    public byte[] getSource() {
        return source;
    }

    public String getName() {
        return name;
    }

    public String getFilePath() {
        return filePath;
    }

    public Extension getFileExtension() {
        return fileExtension;
    }

}
