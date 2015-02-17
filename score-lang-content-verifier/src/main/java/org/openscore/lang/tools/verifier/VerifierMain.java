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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.Validate;
import org.openscore.lang.tools.verifier.configuration.VerifierSpringConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;

/*
 * Created by stoneo on 1/11/2015.
 */
public class VerifierMain {

    public static void main(String[] args) {
        String repositoryPath = args[0];
        Validate.notNull(repositoryPath, "You must pass a path to your repository");
        repositoryPath = FilenameUtils.separatorsToSystem(repositoryPath);
        Validate.isTrue(new File(repositoryPath).isDirectory(),
                "Directory path argument \'" + repositoryPath + "\' does not lead to a directory");

        ApplicationContext context = new AnnotationConfigApplicationContext(VerifierSpringConfiguration.class);
        SlangContentVerifier slangContentVerifier = context.getBean(SlangContentVerifier.class);
        try {
            int numberOfValidSlangFiles = slangContentVerifier.verifyAllSlangFilesInDirAreValid(repositoryPath);
            System.out.println("SUCCESS: Found " + numberOfValidSlangFiles + " slang files under directory: \"" + repositoryPath + "\" and all are valid.");
            System.exit(0);
        } catch (Exception e) {
            System.out.println(e.getMessage() + "\n\nFAILURE: Validation of slang files under directory: \"" + repositoryPath + "\" failed.");
            // TODO - do we want to throw exception or exit with 1?
            System.exit(1);
        }
    }

}
