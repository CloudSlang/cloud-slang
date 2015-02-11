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

import org.openscore.lang.tools.verifier.configuration.VerifierSpringConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/*
 * Created by stoneo on 1/11/2015.
 */
public class SlangFilesVerifier {

    public static void main(String[] args) {
        String repositoryPath = args[0];
        ApplicationContext context = new AnnotationConfigApplicationContext(VerifierSpringConfiguration.class);
        VerifierHelper verifierHelper = context.getBean(VerifierHelper.class);
        try {
            verifierHelper.verifyAllSlangFilesInDirAreValid(repositoryPath);
            System.out.println("SUCCESS: All slang files under directory: \"" + repositoryPath + "\" are valid.");
            System.exit(0);
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n\nFAILURE: Validation of slang files under directory: \"" + repositoryPath + "\" failed.");
            System.exit(1);
        }
    }

}
