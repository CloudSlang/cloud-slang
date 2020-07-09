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

import io.cloudslang.lang.entities.PromptType;
import io.cloudslang.lang.entities.SensitivityLevel;
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
    private PromptType promptType;
    private String promptMessage;
    private String promptOptions;
    private String promptDelimiter;

    private Input(InputBuilder inputBuilder) {
        super(inputBuilder.name,
                inputBuilder.value,
                inputBuilder.functionDependencies,
                inputBuilder.systemPropertyDependencies
        );
        this.required = inputBuilder.required;
        this.privateInput = inputBuilder.privateInput;
        this.promptType = inputBuilder.promptType;
        this.promptMessage = inputBuilder.promptMessage;
        this.promptOptions = inputBuilder.promptOptions;
        this.promptDelimiter = inputBuilder.promptDelimiter;
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

    public PromptType getPromptType() {
        return promptType;
    }

    public String getPromptMessage() {
        return promptMessage;
    }

    public String getPromptOptions() {
        return promptOptions;
    }

    public String getPromptDelimiter() {
        return promptDelimiter;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("required", required)
                .append("privateInput", privateInput)
                .append("promptType", promptType)
                .append("promptMessage", promptMessage)
                .append("promptOptions", promptOptions)
                .append("promptDelimiter", promptDelimiter)
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
                .append(promptType, input.promptType)
                .append(promptMessage, input.promptMessage)
                .append(promptOptions, input.promptOptions)
                .append(promptDelimiter, input.promptDelimiter)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(required)
                .append(privateInput)
                .append(promptType)
                .append(promptMessage)
                .append(promptOptions)
                .append(promptDelimiter)
                .toHashCode();
    }

    public static class InputBuilder {
        private String name;
        private Value value;
        private boolean required;
        private boolean privateInput;
        private Set<ScriptFunction> functionDependencies;
        private Set<String> systemPropertyDependencies;
        private PromptType promptType;
        private String promptMessage;
        private String promptOptions;
        private String promptDelimiter;

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

        public InputBuilder(String name, Serializable serializable, boolean sensitive,
                            SensitivityLevel sensitivityLevel) {
            this.name = name;
            this.value = ValueFactory.create(serializable, sensitive, sensitivityLevel);
            this.required = true;
            this.privateInput = false;
            this.functionDependencies = new HashSet<>();
            this.systemPropertyDependencies = new HashSet<>();
        }

        public InputBuilder(Input input, Value value) {
            this.name = input.getName();
            this.value = value;
            this.required = input.isRequired();
            this.privateInput = input.isPrivateInput();
            this.functionDependencies = input.getFunctionDependencies();
            this.systemPropertyDependencies = input.getSystemPropertyDependencies();
            this.promptType = input.getPromptType();
            this.promptOptions = input.getPromptOptions();
            this.promptDelimiter = input.getPromptDelimiter();
            this.promptMessage = input.getPromptMessage();
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

        public InputBuilder withPrompt(PromptType promptType) {
            this.promptType = promptType;
            return this;
        }

        public InputBuilder withPromptMessage(String promptMessage) {
            this.promptMessage = promptMessage;
            return this;
        }

        public InputBuilder withPromptOptions(String promptOptions) {
            this.promptOptions = promptOptions;
            return this;
        }

        public InputBuilder withPromptDelimiter(String promptDelimiter) {
            this.promptDelimiter = promptDelimiter;
            return this;
        }

        public Input build() {
            return new Input(this);
        }
    }

}
