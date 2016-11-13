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

import io.cloudslang.lang.entities.SlangSystemPropertyConstant;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class SlangSource {

    private final String content;
    private final String name;
    private final String filePath;
    private final Extension fileExtension;

    public SlangSource(String content, String name) {
        Validate.notNull(content, "Source cannot be null");
        this.content = content;
        this.name = name;
        this.filePath = null;
        this.fileExtension = null;
    }

    private SlangSource(String content, String name, String filePath, Extension fileExtension) {
        Validate.notNull(content, "Source cannot be null");

        this.content = content;
        this.name = name;
        this.filePath = filePath;
        this.fileExtension = fileExtension;
    }

    public static SlangSource fromFile(File file) {
        Validate.notNull(file, "File cannot be null");
        Validate.isTrue(file.isFile(), "File content: " + file.getName() +
                " doesn't lead to a file, directories are not supported");

        String content;
        try {
            content = readFileToString(file);
        } catch (IOException e) {
            throw new RuntimeException("There was a problem reading the file: " + file.getName(), e);
        }

        String fileName = file.getName();
        String filePath = file.getPath();
        Extension extension = Extension.findExtension(fileName);
        return new SlangSource(content, fileName, filePath, extension);
    }

    public static SlangSource fromFile(URI uri) {
        return fromFile(new File(uri));
    }

    public static SlangSource fromBytes(byte[] bytes, String name) {
        return new SlangSource(new String(bytes, getCloudSlangCharset()), name);
    }

    public static Charset getCloudSlangCharset() {
        String cslangEncoding = System.getProperty(SlangSystemPropertyConstant.CSLANG_ENCODING.getValue());
        return StringUtils.isEmpty(cslangEncoding) ?
                StandardCharsets.UTF_8 :
                Charset.forName(cslangEncoding);
    }

    private static String readFileToString(File file) throws IOException {
        Charset charset = getCloudSlangCharset();
        return FileUtils.readFileToString(file, charset);
    }

    public String getContent() {
        return content;
    }

    public String getName() {
        return name;
    }

    public Extension getFileExtension() {
        return fileExtension;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public String toString() {
        return "SlangSource{" +
                "content='" + content + '\'' +
                ", name='" + name + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileExtension=" + fileExtension +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SlangSource that = (SlangSource) o;

        return new EqualsBuilder()
                .append(content, that.content)
                .append(name, that.name)
                .append(filePath, that.filePath)
                .append(fileExtension, that.fileExtension)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(content)
                .append(name)
                .append(filePath)
                .append(fileExtension)
                .toHashCode();
    }
}