/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.compiler.parser.model;

import java.util.Map;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Bonczidai Levente
 * @since 3/22/2017
 */
public class ParsedDescriptionSection {
    private final Map<String, String> data;
    // line: #!!
    private final int startLineNumber;

    public ParsedDescriptionSection(Map<String, String> data, int startLineNumber) {
        this.data = data;
        this.startLineNumber = startLineNumber;
    }

    public Map<String, String> getData() {
        return data;
    }

    public int getStartLineNumber() {
        return startLineNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ParsedDescriptionSection that = (ParsedDescriptionSection) o;

        return new EqualsBuilder()
                .append(startLineNumber, that.startLineNumber)
                .append(data, that.data)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(data)
                .append(startLineNumber)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "ParsedDescriptionSection{" +
                "data=" + data +
                ", startLineNumber=" + startLineNumber +
                '}';
    }
}
