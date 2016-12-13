/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.caching;

import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Bonczidai Levente
 * @since 11/11/2016
 */
public class CacheValue {
    /**
     * to calculate changes
     */
    private SlangSource source;
    /**
     * actual value
     */
    private ExecutableModellingResult executableModellingResult;

    public CacheValue(SlangSource source, ExecutableModellingResult executableModellingResult) {
        this.source = source;
        this.executableModellingResult = executableModellingResult;
    }

    public SlangSource getSource() {
        return source;
    }

    public ExecutableModellingResult getExecutableModellingResult() {
        return executableModellingResult;
    }

    @Override
    public String toString() {
        return "CacheValue{" +
                "source=" + source +
                ", executableModellingResult=" + executableModellingResult +
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

        CacheValue that = (CacheValue) o;

        return new EqualsBuilder()
                .append(source, that.source)
                .append(executableModellingResult, that.executableModellingResult)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(source)
                .append(executableModellingResult)
                .toHashCode();
    }
}
