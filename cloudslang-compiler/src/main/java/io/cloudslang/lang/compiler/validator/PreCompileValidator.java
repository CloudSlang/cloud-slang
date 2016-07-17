package io.cloudslang.lang.compiler.validator;

import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import io.cloudslang.lang.compiler.modeller.transformers.Transformer;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;

import io.cloudslang.lang.entities.bindings.InOutParam;

import java.util.List;
import java.util.Map;

/**
 * User: bancl
 * Date: 6/17/2016
 */
public interface PreCompileValidator {

    String validateExecutableRawData(ParsedSlang parsedSlang, Map<String, Object> executableRawData, List<RuntimeException> errors);

    List<Map<String, Map<String, Object>>> validateWorkflowRawData(ParsedSlang parsedSlang, Map<String, Object> executableRawData,
                                                                   List<RuntimeException> errors);

    ExecutableModellingResult validateResult(ParsedSlang parsedSlang, Map<String, Object> executableRawData, ExecutableModellingResult result);

    List<RuntimeException> checkKeyWords(
            String dataLogicalName,
            String parentProperty,
            Map<String, Object> rawData,
            List<Transformer> allRelevantTransformers,
            List<String> additionalValidKeyWords,
            List<List<String>> constraintGroups);

    Map<String, Map<String, Object>> validateOnFailurePosition(
            List<Map<String, Map<String, Object>>> workFlowRawData,
            String execName,
            List<RuntimeException> errors);

    void validateResultsSection(Map<String, Object> executableRawData, String artifact, List<RuntimeException> errors);
    
    void validateNoDuplicateInOutParams(List<? extends InOutParam> inputs, InOutParam element);

}
