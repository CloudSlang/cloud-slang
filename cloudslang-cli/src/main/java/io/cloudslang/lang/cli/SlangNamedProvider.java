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

import org.springframework.shell.plugin.NamedProvider;

/**
 * @author moradi
 * @version $Id$
 * @since 18/11/2014
 */
public abstract class SlangNamedProvider implements NamedProvider {

    @Override
    public String getProviderName() {
        return "CloudSlang";
    }

}
