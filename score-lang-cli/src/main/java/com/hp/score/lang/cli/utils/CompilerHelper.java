package com.hp.score.lang.cli.utils;


import com.hp.score.lang.compiler.SlangCompiler;
import com.hp.score.lang.entities.CompilationArtifact;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

        List<File> dependenciesFilesList = new ArrayList<>();
        File file = new File(filePath);
        Validate.isTrue(file.isFile(),"filePath must lead to a file");

        if(StringUtils.isEmpty(dependencies)){
            dependencies = file.getParent();//default behavior is taking the parent dir
        }
        if(dependencies != null){
            Collection<File> dependenciesFiles = FileUtils.listFiles(new File(dependencies), SLANG_FILE_EXTENSIONS, false);
            dependenciesFilesList.addAll(dependenciesFiles);
        }

        //todo - support compile of op too?
        CompilationArtifact compilationArtifact = compiler.compile(file,null,dependenciesFilesList);

        return compilationArtifact;
    }


}
