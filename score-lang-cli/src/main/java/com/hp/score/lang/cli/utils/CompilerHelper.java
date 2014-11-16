package com.hp.score.lang.cli.utils;


import com.hp.score.lang.compiler.SlangCompiler;
import com.hp.score.lang.entities.CompilationArtifact;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Date: 11/13/2014
 *
 * @author lesant
 */

@Component
public class CompilerHelper {
    //todo - tests

    @Autowired
    private SlangCompiler compiler;

    private String[] SLANG_FILE_EXTENSIONS = {"yml","yaml"};

    public CompilationArtifact compile(String filePath, String opName, String dependencies) throws IOException{

        File file = new File(filePath);

        Collection<File> dependenciesFiles = FileUtils.listFiles(new File(dependencies), SLANG_FILE_EXTENSIONS, true);

        //todo - suppport compile of op too?
        CompilationArtifact compilationArtifact = compiler.compile(file,null,new ArrayList<>(dependenciesFiles));

        return compilationArtifact;
    }


}
