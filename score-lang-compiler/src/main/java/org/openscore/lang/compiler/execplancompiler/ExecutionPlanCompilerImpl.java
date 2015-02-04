package org.openscore.lang.compiler.execplancompiler;

import org.openscore.lang.compiler.modeller.model.Executable;
import org.openscore.lang.entities.CompilationArtifact;

import java.util.Set;

/**
 * Created by stoneo on 2/2/2015.
 */
public class ExecutionPlanCompilerImpl implements ExecutionPlanCompiler{

    public ExecutionPlanCompilerImpl() {
        super();
    }

    public CompilationArtifact compile(Executable executable, Set<Executable> dependencies) {
        return null;
    }
}
