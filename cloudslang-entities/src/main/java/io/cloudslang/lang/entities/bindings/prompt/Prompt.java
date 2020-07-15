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

public class Prompt implements Serializable {

    private static final long serialVersionUID = -4553343224625767773L;

    private final PromptType promptType;
    private final String promptOptions;
    private final String promptDelimiter;
    private String promptMessage;

    public Prompt(PromptType promptType, String promptMessage, String promptOptions, String promptDelimiter) {
        this.promptType = promptType;
        this.promptMessage = promptMessage;
        this.promptOptions = promptOptions;
        this.promptDelimiter = promptDelimiter;
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

}
