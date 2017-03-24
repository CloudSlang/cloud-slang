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
