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

import io.cloudslang.lang.commons.services.api.UserConfigurationService;
import io.cloudslang.lang.commons.services.impl.UserConfigurationServiceImpl;
import java.io.IOException;
import org.springframework.shell.Bootstrap;

/**
 * @author Bonczidai Levente
 * @since 12/16/2015
 */
public class SlangBootstrap {

    public static void main(String[] args) throws IOException {
        loadUserProperties();
        System.out.println("Loading..");
        Bootstrap.main(args);
    }

    @SuppressWarnings("Duplicates")
    private static void loadUserProperties() {
        try {
            UserConfigurationService userConfigurationService = new UserConfigurationServiceImpl();
            userConfigurationService.loadUserProperties();
        } catch (Exception ex) {
            System.out.println("Error occurred while loading user configuration: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

}
