package io.cloudslang.maven.compiler;

import io.cloudslang.lang.compiler.SlangCompiler;
import io.cloudslang.lang.compiler.SlangCompilerImpl;
import io.cloudslang.lang.compiler.SlangSource;
import io.cloudslang.lang.compiler.configuration.SlangCompilerSpringConfig;
import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by hanael on 10/07/2016.
 */

@Component(role = org.codehaus.plexus.compiler.Compiler.class , hint = "cloudslang" )
public class CloudSlangMavenCompiler extends AbstractCompiler{

    private SlangCompiler slangCompiler = new SlangCompilerImpl();

    public CloudSlangMavenCompiler() {
        super(CompilerOutputStyle.ONE_OUTPUT_FILE_FOR_ALL_INPUT_FILES, null, null, null);
        ApplicationContext ctx = new AnnotationConfigApplicationContext(SlangCompilerSpringConfig.class);
        slangCompiler = ctx.getBean(SlangCompiler.class);
    }


    public boolean canUpdateTarget(CompilerConfiguration configuration) throws CompilerException {
        return false;
    }

    public CompilerResult performCompile(CompilerConfiguration config) throws CompilerException {
        CompilerResult compilerResult = new CompilerResult();
        List<CompilerMessage> compilerMessage = new ArrayList();

        //we do not want the source files that were calculated because we have multiple suffix
        //and the framework support only one via the inputFileEnding
        config.setSourceFiles(null);
        String[] sourceFiles = getSourceFiles(config);
        if(sourceFiles.length > 0) {
            System.out.println("Compiling " + sourceFiles.length + " " + "source file" + (sourceFiles.length == 1?"":"s"));
            for (String sourceFile : sourceFiles) {
                compilerMessage.addAll(compileFile(sourceFile));
            }

            if (compilerMessage.size() > 0){
                compilerResult.setCompilerMessages(compilerMessage);
                compilerResult.setSuccess(false);
            }
        }

        return compilerResult;
    }

    private List<CompilerMessage> compileFile(String sourceFile){
        ExecutableModellingResult executableModellingResult ;
        List<CompilerMessage> compilerMessage = new ArrayList();


        try {
            SlangSource slangSource = SlangSource.fromFile(new File(sourceFile));
            executableModellingResult = slangCompiler.preCompileSource(slangSource);
            if(executableModellingResult.getErrors() != null && !executableModellingResult.getErrors().isEmpty()){
                for (RuntimeException runtimeException : executableModellingResult.getErrors()) {
                    compilerMessage.add(new CompilerMessage(sourceFile + ": " + runtimeException.getMessage(), CompilerMessage.Kind.ERROR)) ;
                }
            }
        } catch (Exception e) {
            compilerMessage.add(new CompilerMessage(sourceFile + ": " + e.getMessage(), CompilerMessage.Kind.ERROR)) ;
        }

        return compilerMessage;
    }

    public String[] createCommandLine(CompilerConfiguration config) throws CompilerException {
        return null ;
    }

    protected static String[] getSourceFiles( CompilerConfiguration config ){
        Set sources = new HashSet();

        for(Iterator it = config.getSourceLocations().iterator(); it.hasNext();){
            String sourceLocation = (String) it.next();
            sources.addAll(getSourceFilesForSourceRoot(config, sourceLocation ));
        }

        String[] result;
        if ( sources.isEmpty() ){
            result = new String[0];
        } else {
            result = (String[]) sources.toArray( new String[sources.size()] );
        }

        return result;
    }

    // we need to override this as it is hard coded java file extensions
    protected static Set getSourceFilesForSourceRoot(CompilerConfiguration config, String sourceLocation) {
        Path path = Paths.get(sourceLocation);
        if(!Files.exists(path)) return Collections.EMPTY_SET;

        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir(sourceLocation);
        Set includes = config.getIncludes();
        if(includes != null && !includes.isEmpty()) {
            String[] excludes = (String[])(includes.toArray(new String[includes.size()]));
            scanner.setIncludes(excludes);
        } else {
            scanner.setIncludes(new String[]{"**/*.yaml","**/*.sl","**/*.yml"});
        }

        Set<String> configExcludes = config.getExcludes();
        String[] sourceDirectorySources;
        if(configExcludes != null && !configExcludes.isEmpty()) {
            sourceDirectorySources = (configExcludes.toArray(new String[configExcludes.size()]));
            scanner.setIncludes(sourceDirectorySources);
        }

        scanner.scan();
        sourceDirectorySources = scanner.getIncludedFiles();
        HashSet sources = new HashSet();

        for (String sourceDirectorySource : sourceDirectorySources) {
            File f = new File(sourceLocation, sourceDirectorySource);
            sources.add(f.getPath());
        }

        return sources;
    }
}
