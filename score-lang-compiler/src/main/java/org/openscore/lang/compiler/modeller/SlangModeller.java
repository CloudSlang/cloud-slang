package org.openscore.lang.compiler.modeller;

import org.openscore.lang.compiler.modeller.model.Executable;
import org.openscore.lang.compiler.parser.model.ParsedSlang;

/**
 * Created by stoneo on 2/2/2015.
 */
public interface SlangModeller {
    /**
     * Pre-compile a Slang file into a list of Executables
     * @param parsedSlang the parsed Slang source file
     * @return a list of Executable objects, containing either a flow or a list of all the operations in the file
     */
    public Executable createModel(ParsedSlang parsedSlang);
}
