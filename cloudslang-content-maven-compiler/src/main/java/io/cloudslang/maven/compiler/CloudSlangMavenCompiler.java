/*******************************************************************************
 * (c) Copyright 2016 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package io.cloudslang.maven.compiler;

import io.cloudslang.lang.compiler.SlangCompiler;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.compiler.AbstractCompiler;
import org.codehaus.plexus.compiler.CompilerConfiguration;
import org.codehaus.plexus.compiler.CompilerException;
import org.codehaus.plexus.compiler.CompilerMessage;
import org.codehaus.plexus.compiler.CompilerOutputStyle;
import org.codehaus.plexus.compiler.CompilerResult;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.DirectoryScanner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.util.Collections.emptySet;

/**
 * Created by hanael on 10/07/2016.
 */

@Component(role = org.codehaus.plexus.compiler.Compiler.class, hint = "cloudslang")
public class CloudSlangMavenCompiler extends AbstractCompiler {

    private static String IGNORE_DEPENDENCIES = "ignore-dependencies";
    private static String IGNORE_ERRORS = "ignore-errors";

    private SlangCompiler slangCompiler;

    private boolean compileWithDependencies;

    private CompilerMessage.Kind errorLevel;

    public CloudSlangMavenCompiler() {
        super(CompilerOutputStyle.ONE_OUTPUT_FILE_FOR_ALL_INPUT_FILES, null, null, null);
        ApplicationContext ctx = new AnnotationConfigApplicationContext(SlangCompilerSpringConfig.class);
        slangCompiler = ctx.getBean(SlangCompiler.class);
    }


    @Override
    public boolean canUpdateTarget(CompilerConfiguration configuration) throws CompilerException {
        return false;
    }

    @Override
    public CompilerResult performCompile(CompilerConfiguration config) throws CompilerException {
        init(config);
        CompilerResult compilerResult = new CompilerResult();
        List<CompilerMessage> compilerMessage = new ArrayList<>();

        //we do not want the source files that were calculated because we have multiple suffix
        //and the framework support only one via the inputFileEnding
        config.setSourceFiles(null);
        String[] sourceFiles = getSourceFiles(config);
        Map<String, byte[]> dependenciesSourceFiles = getDependenciesSourceFiles(config);
        if (sourceFiles.length > 0) {
            System.out.println("Compiling " + sourceFiles.length + " " +
                    "source file" + (sourceFiles.length == 1 ? "" : "s"));
            for (String sourceFile : sourceFiles) {
                compilerMessage.addAll(compileFile(sourceFile, sourceFiles, dependenciesSourceFiles));
            }

            if (compilerMessage.size() > 0) {
                compilerResult.setCompilerMessages(compilerMessage);
                //we want to set it to false only in case we want to fail the build
                if (errorLevel.equals(CompilerMessage.Kind.ERROR)) {
                    compilerResult.setSuccess(false);
                }
            }
        }

        return compilerResult;
    }

    private void init(CompilerConfiguration config) {
        //This parameter is passed in the compiler plugin whether to compile the flow with its dependencies
        compileWithDependencies = !config.getCustomCompilerArgumentsAsMap().containsKey(IGNORE_DEPENDENCIES);
        //This parameter is used to control the error level. if not set only warnings will be shown
        errorLevel = config.getCustomCompilerArgumentsAsMap().containsKey(IGNORE_ERRORS) ?
                CompilerMessage.Kind.WARNING : CompilerMessage.Kind.ERROR;
    }

    private List<CompilerMessage> compileFile(String sourceFile, String[] sourceFiles,
                                              Map<String, byte[]> dependenciesSourceFiles) {
        ExecutableModellingResult executableModellingResult;
        List<CompilerMessage> compilerMessages = new ArrayList<>();


        try {
            SlangSource slangSource = SlangSource.fromFile(new File(sourceFile));
            executableModellingResult = slangCompiler.preCompileSource(slangSource);
            if (!CollectionUtils.isEmpty(executableModellingResult.getErrors())) {
                for (RuntimeException runtimeException : executableModellingResult.getErrors()) {
                    compilerMessages.add(new CompilerMessage(sourceFile + ": " +
                            runtimeException.getMessage(), errorLevel));
                }
            } else {
                if (compileWithDependencies) {
                    compilerMessages.addAll(validateSlangModelWithDependencies(executableModellingResult,
                            sourceFiles, dependenciesSourceFiles, sourceFile));
                }
            }
        } catch (Exception e) {
            compilerMessages.add(new CompilerMessage(sourceFile + ": " + e.getMessage(), errorLevel));
        }

        return compilerMessages;
    }

    private List<CompilerMessage> validateSlangModelWithDependencies(ExecutableModellingResult modellingResult,
                                                                     String[] dependencies,
                                                                     Map<String, byte[]> dependenciesSourceFiles,
                                                                     String sourceFile) {
        List<CompilerMessage> compilerMessages = new ArrayList<>();
        Set<Executable> dependenciesExecutables = new HashSet<>();

        Executable executable = modellingResult.getExecutable();
        //we need to verify only flows
        if (!executable.getType().equals("flow")) {
            return compilerMessages;
        }

        for (String dependency : dependencies) {
            try {
                SlangSource slangSource = SlangSource.fromFile(new File(dependency));
                modellingResult = slangCompiler.preCompileSource(slangSource);
                dependenciesExecutables.add(modellingResult.getExecutable());
            } catch (Exception e) {
                this.getLogger().warn("Could not compile source: " + dependency);
            }
        }

        for (Map.Entry<String, byte[]> dependencyEntry : dependenciesSourceFiles.entrySet()) {
            try {
                SlangSource slangSource = SlangSource.fromBytes(dependencyEntry.getValue(), dependencyEntry.getKey());
                modellingResult = slangCompiler.preCompileSource(slangSource);
                dependenciesExecutables.add(modellingResult.getExecutable());
            } catch (Exception e) {
                this.getLogger().warn("Could not compile source: " + dependencyEntry.getKey());
            }
        }

        List<RuntimeException> exceptions = slangCompiler.validateSlangModelWithDirectDependencies(executable,
                dependenciesExecutables);
        for (RuntimeException runtimeException : exceptions) {
            compilerMessages.add(new CompilerMessage(sourceFile + ": " + runtimeException.getMessage(), errorLevel));
        }

        return compilerMessages;
    }

    public String[] createCommandLine(CompilerConfiguration config) throws CompilerException {
        return null;
    }

    protected static String[] getSourceFiles(CompilerConfiguration config) {
        Set<String> sources = new HashSet<>();

        for (String sourceLocation : config.getSourceLocations()) {
            sources.addAll(getSourceFilesForSourceRoot(config, sourceLocation));
        }

        return sources.toArray(new String[sources.size()]);
    }

    private static Map<String, byte[]> getDependenciesSourceFiles(CompilerConfiguration config)
            throws CompilerException {
        if (config.getClasspathEntries().isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, byte[]> sources = new HashMap<>();
        for (String dependency : config.getClasspathEntries()) {
            try {
                sources.putAll(getSourceFilesForDependencies(dependency));
            } catch (IOException e) {
                throw new CompilerException("Cannot load sources from: " + dependency + ". " + e.getMessage());
            }
        }

        return sources;
    }

    private static Map<String, byte[]> getSourceFilesForDependencies(String dependency) throws IOException {
        Path path = Paths.get(dependency);
        if (!Files.exists(path) || !path.toString().toLowerCase().endsWith(".jar")) {
            return Collections.emptyMap();
        }

        Map<String, byte[]> sources = new HashMap<>();

        try (JarFile jar = new JarFile(dependency)) {
            Enumeration enumEntries = jar.entries();
            while (enumEntries.hasMoreElements()) {
                JarEntry file = (JarEntry) enumEntries.nextElement();
                if ((file == null) || (file.isDirectory()) ||
                        (!file.getName().endsWith(".sl.yaml") &&
                                !file.getName().endsWith(".sl") && !file.getName().endsWith(".sl.yml"))) {
                    continue;
                }

                byte[] bytes;
                try (InputStream is = jar.getInputStream(file)) {
                    bytes = IOUtils.toByteArray(is);
                    sources.put(file.getName(), bytes);
                }
            }
        }

        return sources;
    }

    // we need to override this as it is hard coded java file extensions
    protected static Set<String> getSourceFilesForSourceRoot(CompilerConfiguration config, String sourceLocation) {
        Path path = Paths.get(sourceLocation);
        if (!Files.exists(path)) {
            return emptySet();
        }

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(sourceLocation);
        Set<String> includes = config.getIncludes();
        if (includes != null && !includes.isEmpty()) {
            String[] inclStrs = includes.toArray(new String[includes.size()]);
            scanner.setIncludes(inclStrs);
        } else {
            scanner.setIncludes(new String[]{"**/*.sl.yaml", "**/*.sl", "**/*.sl.yml"});
        }

        Set<String> configExcludes = config.getExcludes();
        if (configExcludes != null && !configExcludes.isEmpty()) {
            String[] exclStrs = configExcludes.toArray(new String[configExcludes.size()]);
            scanner.setExcludes(exclStrs);
        } else {
            scanner.setExcludes(new String[]{"**/*prop.sl"});
        }

        scanner.scan();
        String[] sourceDirectorySources = scanner.getIncludedFiles();
        Set<String> sources = new HashSet<>();

        for (String sourceDirectorySource : sourceDirectorySources) {
            sources.add(new File(sourceLocation, sourceDirectorySource).getPath());
        }

        return sources;
    }
}
