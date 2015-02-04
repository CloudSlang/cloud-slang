package org.openscore.lang.compiler.modeller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.openscore.lang.compiler.SlangTextualKeys;
import org.openscore.lang.compiler.modeller.model.Executable;
import org.openscore.lang.compiler.parser.model.ParsedSlang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by stoneo on 2/2/2015.
 */
@Component
public class SlangModellerImpl implements SlangModeller{

    @Autowired
    private ExecutableBuilder executableBuilder;

    @Override
    public Executable createModel(ParsedSlang parsedSlang) {
        Validate.notNull(parsedSlang, "You must supply a parsed Slang source to compile");

        try {
            //then we transform those maps to model objects
            return transformToExecutable(parsedSlang);
        } catch (Throwable ex){
            throw new RuntimeException("Error compiling source: " + parsedSlang.getName() + ". " + ex.getMessage(), ex);
        }
    }

    /**
     * Utility method that transform a {@link org.openscore.lang.compiler.parser.model.ParsedSlang}
     * into a list of {@link org.openscore.lang.compiler.modeller.model.Executable}
     * also handles operations files
     *
     * @param parsedSlang the source to transform
     * @return List of {@link org.openscore.lang.compiler.modeller.model.Executable}  of the requested flow or operation
     */
    private Executable transformToExecutable(ParsedSlang parsedSlang) {
        switch (parsedSlang.getType()) {
            case OPERATION:
                return transformOperation(parsedSlang);
            case FLOW:
                return transformFlow(parsedSlang);
            default:
                throw new RuntimeException("source: " + parsedSlang.getName() + " is not of flow type or operations");
        }
    }

    /**
     * transform an operation {@link org.openscore.lang.compiler.parser.model.ParsedSlang} to a List of {@link org.openscore.lang.compiler.modeller.model.Executable}
     *
     * @param parsedSlang the source to transform the operations from
     * @return {@link org.openscore.lang.compiler.modeller.model.Executable} representing the operation in the source
     */
    private Executable transformOperation(ParsedSlang parsedSlang) {
        Map<String, Object> operationRawData = parsedSlang.getOperation();
        String operationName = (String) operationRawData.get(SlangTextualKeys.EXECUTABLE_NAME_KEY);
        if (StringUtils.isBlank(operationName)) {
            throw new RuntimeException("Operation in source: " + parsedSlang.getName() + " has no name");
        }
        return executableBuilder.transformToExecutable(parsedSlang, operationName, operationRawData);
    }

    /**
     * transform an flow {@link org.openscore.lang.compiler.parser.model.ParsedSlang} to a {@link org.openscore.lang.compiler.modeller.model.Executable}
     *
     * @param parsedSlang the source to transform the flow from
     * @return {@link org.openscore.lang.compiler.modeller.model.Executable} representing the flow in the source
     */
    private Executable transformFlow(ParsedSlang parsedSlang) {
        Map<String, Object> flowRawData = parsedSlang.getFlow();
        String flowName = (String) flowRawData.get(SlangTextualKeys.EXECUTABLE_NAME_KEY);
        if (StringUtils.isBlank(flowName)) {
            throw new RuntimeException("Flow in source: " + parsedSlang.getName() + " has no name");
        }
        return executableBuilder.transformToExecutable(parsedSlang, flowName, flowRawData);
    }

}
