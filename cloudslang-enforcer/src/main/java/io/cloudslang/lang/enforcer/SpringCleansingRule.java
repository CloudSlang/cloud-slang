/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.lang.enforcer;

import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.isRegularFile;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static org.apache.commons.io.FileUtils.iterateFiles;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.lang3.StringUtils.countMatches;


public class SpringCleansingRule implements EnforcerRule {

    private static final String JAVA = "java";
    private static final String OPTIONAL_SPACER = "(\\s)*";
    private static final String REGEX_DOT = "\\.";
    private static final String OPTIONAL_STATIC = "(static )?";

    private static final String ORG = "org";
    private static final String SPRINGFRAMEWORK = "springframework";

    private static final String SPACER_DOT_SPACER = OPTIONAL_SPACER + REGEX_DOT + OPTIONAL_SPACER;
    private static final String regexImport = "import " + OPTIONAL_STATIC + OPTIONAL_SPACER + ORG +
            SPACER_DOT_SPACER + SPRINGFRAMEWORK + "[^;]+" + ";" ;

    private static final Pattern patternImport = Pattern.compile(regexImport);
    private static final String regexCodeLine = OPTIONAL_SPACER + ORG + OPTIONAL_SPACER + REGEX_DOT +
            OPTIONAL_SPACER + SPRINGFRAMEWORK + "(.|\\s)+;" ;

    private static final Pattern patternCodeLine = Pattern.compile(regexCodeLine);
    private static final String STARTED_SCANNING_ORG_SPRINGFRAMEWORK = "Scanning file for org.springframework " +
            "in file '%s'";
    private static final String FINISHED_SCANNING_ORG_SPRINGFRAMEWORK = "Finished scanning for org.springframework " +
            "in file '%s'";
    private static final String FOUND_USAGE_OF_ORG_SPRINGFRAMEWORK_IN_IMPORT_AT_LINE = "Found usage of " +
            "org.springframework in import at line ";
    private static final String FOUND_USAGE_OF_ORG_SPRINGFRAMEWORK_IN_CODE_FRAGMENT_AT_LINE = "Found usage of " +
            "org.springframework in code fragment at line ";

    private static final String CONTEXT = "context";
    private static final String ANNOTATION = "annotation";
    private static final String CONFIGURATION = "Configuration";


    private static final String SPRING_CONFIGURATION_CLASS = OPTIONAL_SPACER + ORG + SPACER_DOT_SPACER +
            SPRINGFRAMEWORK + SPACER_DOT_SPACER + CONTEXT + SPACER_DOT_SPACER + ANNOTATION + SPACER_DOT_SPACER +
            CONFIGURATION;

    private static final Pattern patternConfigurationClass = Pattern.compile(SPRING_CONFIGURATION_CLASS);

    private static final String FOUND_UNWANTED_OCCURRENCE_OF_ORG_SPRINGFRAMEWORK = "Found unwanted occurrence of " +
            "org.springframework at line %d in source '%s'";
    private static final String IN_SOURCE = " in source '";
    private static final String NEW_LINE = "\n";

    /**
     * Simple param. This rule will fail if the value is true.
     */
    private boolean shouldFail = false;

    public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException {
        Log log = helper.getLog();

        try {
            // get the various expressions out of the helper.
            MavenProject project = (MavenProject) helper.evaluate("${project}");
            // MavenSession session = (MavenSession) helper.evaluate("${session}");
            // String target = (String) helper.evaluate("${project.build.directory}");
            // String artifactId = (String) helper.evaluate("${project.artifactId}");

            // ArtifactResolver resolver = (ArtifactResolver) helper.getComponent(ArtifactResolver.class);
            // RuntimeInfo rti = (RuntimeInfo) helper.getComponent(RuntimeInfo.class);
            List compileSourceRoots = project.getCompileSourceRoots();

            for (Object compileSourceRoot : compileSourceRoots) {
                String path = (String) compileSourceRoot;
                applyForJavaSourcesInRoot(path, log);
            }
        } catch (ExpressionEvaluationException e) {
            throw new EnforcerRuleException("Unable to lookup an expression " + e.getLocalizedMessage(), e);
        }
    }

    /**
     * If your rule is cacheable, you must return a unique id when parameters or conditions
     * change that would cause the result to be different. Multiple cached results are stored
     * based on their id.
     * <p>
     * The easiest way to do this is to return a hash computed from the values of your parameters.
     * <p>
     * If your rule is not cacheable, then the result here is not important, you may return anything.
     */
    public String getCacheId() {
        //no hash on boolean...only parameter so no hash is needed.
        return "" + this.shouldFail;
    }

    /**
     * This tells the system if the results are cacheable at all. Keep in mind that during
     * forked builds and other things, a given rule may be executed more than once for the same
     * project. This means that even things that change from project to project may still
     * be cacheable in certain instances.
     */
    public boolean isCacheable() {
        return false;
    }

    /**
     * If the rule is cacheable and the same id is found in the cache, the stored results
     * are passed to this method to allow double checking of the results. Most of the time
     * this can be done by generating unique ids, but sometimes the results of objects returned
     * by the helper need to be queried. You may for example, store certain objects in your rule
     * and then query them later.
     */
    public boolean isResultValid(EnforcerRule arg0) {
        return false;
    }

    private boolean isSpringConfigurationAnnotatedClass(final String contents) {
        Matcher matcher = patternConfigurationClass.matcher(contents);
        return matcher.find();
    }

    private void applyForJavaSourcesInRoot(final String path, final Log log) throws EnforcerRuleException {
        Iterator<File> fileIterator = iterateFiles(new File(path), new String[]{JAVA}, true);
        while (fileIterator.hasNext()) {
            File source = fileIterator.next();

            if (isRegularFile(source.toPath(), NOFOLLOW_LINKS)) {
                if (log.isDebugEnabled()) {
                    log.debug(format(STARTED_SCANNING_ORG_SPRINGFRAMEWORK, source.getAbsolutePath()));
                }
                try {
                    String contents = readFileToString(source, UTF_8.displayName());
                    if (isSpringConfigurationAnnotatedClass(contents)) {
                        log.info(format("Skipping verification for Spring configuration class in file '%s'",
                                source.getAbsolutePath()));
                        continue;
                    }
                    // At this point it is clear this is a regular Java class that is not a Spring Configuration class
                    // and just validate we don't have org.springframework in it

                    findMatchesUsingPattern(source, log, contents, patternImport,
                            FOUND_USAGE_OF_ORG_SPRINGFRAMEWORK_IN_IMPORT_AT_LINE);
                    findMatchesUsingPattern(source, log, contents, patternCodeLine,
                            FOUND_USAGE_OF_ORG_SPRINGFRAMEWORK_IN_CODE_FRAGMENT_AT_LINE);
                } catch (IOException ignore) {
                    log.error(format("Could not process file '%s'", source));
                }
                if (log.isDebugEnabled()) {
                    log.debug(format(FINISHED_SCANNING_ORG_SPRINGFRAMEWORK, source.getAbsolutePath()));
                }
            }
        }
    }

    private void findMatchesUsingPattern(final File javaFile, final Log log, final String contents,
                                         final Pattern pattern, String message) throws EnforcerRuleException {
        Matcher matcherImportForFile = pattern.matcher(contents);
        if (matcherImportForFile.find()) {
            int lineNumber = findLineNumber(contents.substring(0, matcherImportForFile.start()));
            log.info(format(FOUND_UNWANTED_OCCURRENCE_OF_ORG_SPRINGFRAMEWORK, lineNumber, javaFile.getAbsolutePath()));
            if (shouldFail) {
                throw new EnforcerRuleException(message + valueOf(lineNumber) + IN_SOURCE +
                        javaFile.getAbsolutePath() + "'");
            }
        }
    }

    private int findLineNumber(String contents) {
        return countMatches(contents, NEW_LINE) + 1;
    }

}
