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

import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Bonczidai Levente
 * @since 11/11/2016
 */
public class CacheResult {

    private CacheValueState state;
    private ExecutableModellingResult executableModellingResult;

    public CacheResult(CacheValueState state, ExecutableModellingResult executableModellingResult) {
        this.state = state;
        this.executableModellingResult = executableModellingResult;
    }

    public CacheValueState getState() {
        return state;
    }

    public ExecutableModellingResult getExecutableModellingResult() {
        return executableModellingResult;
    }

    @Override
    public String toString() {
        return "CacheResult{" +
                "state=" + state +
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

        CacheResult that = (CacheResult) o;

        return new EqualsBuilder()
                .append(state, that.state)
                .append(executableModellingResult, that.executableModellingResult)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(state)
                .append(executableModellingResult)
                .toHashCode();
    }
}
