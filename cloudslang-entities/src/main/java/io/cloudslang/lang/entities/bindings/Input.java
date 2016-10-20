/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.entities.bindings;

import io.cloudslang.lang.entities.bindings.values.Value;
import io.cloudslang.lang.entities.bindings.values.ValueFactory;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author orius123
 * @version $Id$
 * @since 05/11/14.
 */
public class Input extends InOutParam {

    private static final long serialVersionUID = -2411446962609754342L;

    private boolean required;
    private boolean privateInput;

    private Input(InputBuilder inputBuilder) {
        super(inputBuilder.name,
                inputBuilder.value,
                inputBuilder.functionDependencies,
                inputBuilder.systemPropertyDependencies
        );
        this.required = inputBuilder.required;
        this.privateInput = inputBuilder.privateInput;
    }

    /**
     * only here to satisfy serialization libraries
     */
    @SuppressWarnings("unused")
    private Input() {
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isPrivateInput() {
        return privateInput;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("required", required)
                .append("privateInput", privateInput)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Input input = (Input) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(required, input.required)
                .append(privateInput, input.privateInput)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(required)
                .append(privateInput)
                .toHashCode();
    }

    public static class InputBuilder {
        private String name;
        private Value value;
        private boolean required;
        private boolean privateInput;
        private Set<ScriptFunction> functionDependencies;
        private Set<String> systemPropertyDependencies;

        public InputBuilder(String name, Serializable serializable) {
            this(name, serializable, false);
        }

        public InputBuilder(String name, Serializable serializable, boolean sensitive) {
            this.name = name;
            this.value = ValueFactory.create(serializable, sensitive);
            this.required = true;
            this.privateInput = false;
            this.functionDependencies = new HashSet<>();
            this.systemPropertyDependencies = new HashSet<>();
        }

        public InputBuilder withRequired(boolean required) {
            this.required = required;
            return this;
        }

        public InputBuilder withPrivateInput(boolean privateInput) {
            this.privateInput = privateInput;
            return this;
        }

        public InputBuilder withFunctionDependencies(Set<ScriptFunction> functionDependencies) {
            this.functionDependencies = functionDependencies;
            return this;
        }

        public InputBuilder withSystemPropertyDependencies(Set<String> systemPropertyDependencies) {
            this.systemPropertyDependencies = systemPropertyDependencies;
            return this;
        }

        public Input build() {
            return new Input(this);
        }
    }

}
