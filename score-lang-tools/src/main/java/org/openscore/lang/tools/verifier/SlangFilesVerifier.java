/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.openscore.lang.tools.verifier;

import org.apache.log4j.Logger;
import org.openscore.lang.tools.verifier.configuration.VerifierSpringConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;

/*
 * Created by stoneo on 1/11/2015.
 */
public class SlangFilesVerifier {

    private final static Logger log = Logger.getLogger(SlangFilesVerifier.class);

    public static void main(String[] args) {
        String repositoryPath = args[0];
        ApplicationContext context = new AnnotationConfigApplicationContext(VerifierSpringConfiguration.class);
        VerifierHelper verifierHelper = context.getBean(VerifierHelper.class);
        try {
            verifierHelper.createAllSlangModelsFromDirectory(repositoryPath);
            verifierHelper.compileAllSlangModelsInDirectory();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

}
