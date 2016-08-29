package io.cloudslang.lang.compiler.validator;

import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.entities.bindings.InOutParam;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.lang.entities.constants.RegexConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * User: bancl
 * Date: 6/17/2016
 */
public class AbstractValidator {
    public static final String NAME_PLACEHOLDER = "name_placeholder01";

    protected Pattern namespacePattern;
    protected Pattern simpleNamePattern;
    protected Pattern resultNamePattern;

    public AbstractValidator() {
        namespacePattern = Pattern.compile(RegexConstants.NAMESPACE_CHARS);
        simpleNamePattern = Pattern.compile(RegexConstants.SIMPLE_NAME_CHARS);
        resultNamePattern = Pattern.compile(RegexConstants.RESULT_NAME_CHARS);
    }

    protected void validateNamespaceRules(String input) {
        validateChars(namespacePattern, input);
        validateDelimiter(input);
    }

    protected void validateSimpleNameRules(String input) {
        validateChars(simpleNamePattern, input);
    }

    protected void validateResultNameRules(String input) {
        validateChars(resultNamePattern, input);
        validateKeywords(input);
    }

    protected void validateListsHaveMutuallyExclusiveNames(List<? extends InOutParam> inOutParams, List<Output> outputs, String errorMessage) {
        for (InOutParam inOutParam : CollectionUtils.emptyIfNull(inOutParams)) {
            for (Output output : CollectionUtils.emptyIfNull(outputs)) {
                if (StringUtils.equalsIgnoreCase(inOutParam.getName(), output.getName())) {
                    throw new IllegalArgumentException(errorMessage.replace(NAME_PLACEHOLDER, inOutParam.getName()));
                }
            }
        }
    }

    protected List<String> getResultNames(Executable executable) {
        List<String> resultNames = new ArrayList<>();
        for (Result result : executable.getResults()) {
            resultNames.add(result.getName());
        }
        return resultNames;
    }

    private void validateKeywords(String resultName) {
        if (SlangTextualKeys.ON_FAILURE_KEY.equalsIgnoreCase(resultName)) {
            throw new RuntimeException("Result cannot be called '" + SlangTextualKeys.ON_FAILURE_KEY + "'.");
        }
    }

    private void validateChars(Pattern pattern, String input) {
        if (!pattern.matcher(input).matches()) {
            throw new RuntimeException("Argument[" + input +"] contains invalid characters.");
        }
    }

    private void validateDelimiter(String input) {
        if (input.startsWith(RegexConstants.NAMESPACE_PROPERTY_DELIMITER)) {
            throw new RuntimeException(
                    "Argument[" + input +"] cannot start with delimiter[" + RegexConstants.NAMESPACE_PROPERTY_DELIMITER + "]."
            );
        }
        if (input.endsWith(RegexConstants.NAMESPACE_PROPERTY_DELIMITER)) {
            throw new RuntimeException(
                    "Argument[" + input +"] cannot end with delimiter[" + RegexConstants.NAMESPACE_PROPERTY_DELIMITER + "]."
            );
        }
        String[] parts = input.split(RegexConstants.NAMESPACE_DELIMITER_ESCAPED);
        for (String part : parts) {
            if ("".equals(part)) {
                throw new RuntimeException(
                        "Argument[" + input + "] cannot contain multiple delimiters["
                                + RegexConstants.NAMESPACE_PROPERTY_DELIMITER + "] without content."
                );
            }
        }
    }
}
