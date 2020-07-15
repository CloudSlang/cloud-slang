/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.entities.bindings.prompt;

import io.cloudslang.lang.entities.PromptType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import static java.lang.System.lineSeparator;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class Prompt implements Serializable {

    private static final long serialVersionUID = -4553343224625767773L;

    private static final String DEFAULT_DELIMITER = ",";

    private PromptType promptType;
    private String promptOptions;
    private String promptDelimiter;
    private String promptMessage;

    /**
     * for serialization libraries
     */
    @SuppressWarnings("unused")
    private Prompt() {
    }

    private Prompt(PromptBuilder promptBuilder) {
        this.promptType = promptBuilder.promptType;
        this.promptMessage = promptBuilder.promptMessage;
        this.promptOptions = promptBuilder.promptOptions;
        this.promptDelimiter = promptBuilder.promptDelimiter;
    }

    public PromptType getPromptType() {
        return promptType;
    }

    public String getPromptMessage() {
        return promptMessage;
    }

    public void setPromptMessage(String promptMessage) {
        this.promptMessage = promptMessage;
    }

    public String getPromptOptions() {
        return promptOptions;
    }

    public String getPromptDelimiter() {
        return promptDelimiter;
    }

    public static class PromptBuilder {
        private static final String INVALID_CONFIGURATION = "Invalid prompt configuration:" + lineSeparator();

        private PromptType promptType;
        private String promptOptions;
        private String promptDelimiter;
        private String promptMessage;

        public PromptType getPromptType() {
            return promptType;
        }

        public String getPromptOptions() {
            return promptOptions;
        }

        public String getPromptDelimiter() {
            return promptDelimiter;
        }

        public String getPromptMessage() {
            return promptMessage;
        }

        public PromptBuilder setPromptType(PromptType promptType) {
            this.promptType = promptType;
            return this;
        }

        public PromptBuilder setPromptOptions(String promptOptions) {
            this.promptOptions = promptOptions;
            return this;
        }

        public PromptBuilder setPromptDelimiter(String promptDelimiter) {
            this.promptDelimiter = promptDelimiter;
            return this;
        }

        public PromptBuilder setPromptMessage(String promptMessage) {
            this.promptMessage = promptMessage;

            return this;
        }

        public Prompt build() {
            setDefaults();

            Collection<String> validationResult = validate();

            if (isNotEmpty(validationResult)) {
                throw new RuntimeException(INVALID_CONFIGURATION + String.join(lineSeparator(), validationResult));
            } else {
                return new Prompt(this);
            }
        }

        private void setDefaults() {
            switch (promptType) {
                case SINGLE_CHOICE:
                case MULTI_CHOICE:
                    if (isBlank(promptDelimiter)) {
                        promptDelimiter = DEFAULT_DELIMITER;
                    }
                    break;
            }

        }

        private Collection<String> validate() {
            Collection<String> validationResult = new ArrayList<>();
            if (promptType == PromptType.TEXT) {
                if (isNotEmpty(promptOptions)) {
                    validationResult.add("'promptOptions' is not allowed for 'text' prompt");
                }

                if (isNotEmpty(promptDelimiter)) {
                    validationResult.add("'promptDelimiter' is not allowed for 'text' prompt");
                }
            }

            return validationResult;
        }
    }

}
