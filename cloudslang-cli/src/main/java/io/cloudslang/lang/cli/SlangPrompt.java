/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.cli;

import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.PromptProvider;
import org.springframework.stereotype.Component;

/**
 * Date: 11/7/2014
 *
 * @author lesant
 */
@Component
@Order(Integer.MIN_VALUE)
public class SlangPrompt extends SlangNamedProvider implements PromptProvider {

    @Override
    public String getPrompt() {
        return "cslang>";
    }

}
