package io.cloudslang.lang.compiler.validator;

import ch.lambdaj.Lambda;
import io.cloudslang.lang.compiler.Extension;
import io.cloudslang.lang.compiler.SlangTextualKeys;
import io.cloudslang.lang.compiler.modeller.TransformersHandler;
import io.cloudslang.lang.compiler.modeller.model.Flow;
import io.cloudslang.lang.compiler.modeller.model.Step;
import io.cloudslang.lang.compiler.modeller.result.ExecutableModellingResult;
import io.cloudslang.lang.compiler.modeller.transformers.Transformer;
import io.cloudslang.lang.compiler.parser.model.ParsedSlang;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.Result;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;

import static ch.lambdaj.Lambda.exists;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;

/**
 * User: bancl
 * Date: 6/17/2016
 */
@Component
public class PreCompileValidatorImpl extends AbstractValidator implements PreCompileValidator {

    @Override
    public String validateExecutableRawData(ParsedSlang parsedSlang, Map<String, Object> executableRawData, List<RuntimeException> errors) {
        if (executableRawData == null) {
            errors.add(new IllegalArgumentException("Error compiling " + parsedSlang.getName() + ". Executable data is null"));
            return "";
        } else {
            String executableName = getExecutableName(executableRawData);
            if (parsedSlang == null) {
                errors.add(new IllegalArgumentException("Slang source for: \'" + executableName + "\' is null"));
            } else {
                if (StringUtils.isBlank(executableName)) {
                    errors.add(new RuntimeException("Executable in source: " + parsedSlang.getName() + " has no name"));
                }
                if (executableRawData.size() == 0) {
                    errors.add(new IllegalArgumentException("Error compiling " + parsedSlang.getName() + ". Executable data for: \'" + executableName + "\' is empty"));
                }
            }

            return executableName;
        }
    }

    @Override
    public List<Map<String, Map<String, Object>>> validateWorkflowRawData(ParsedSlang parsedSlang, Map<String, Object> executableRawData, List<RuntimeException> errors) {
        String executableName = getExecutableName(executableRawData);
        Object rawData = executableRawData.get(SlangTextualKeys.WORKFLOW_KEY);
        if (rawData == null) {
            rawData = new ArrayList<>();
            errors.add(new RuntimeException("Error compiling " + parsedSlang.getName() + ". Flow: " + executableName + " has no workflow property"));
        }
        List<Map<String, Map<String, Object>>> workFlowRawData;
        try {
            //noinspection unchecked
            workFlowRawData = (List<Map<String, Map<String, Object>>>) rawData;
        } catch (ClassCastException ex) {
            workFlowRawData = new ArrayList<>();
            errors.add(new RuntimeException("Flow: '" + executableName + "' syntax is illegal.\nBelow 'workflow' property there should be a list of steps and not a map"));
        }
        if (CollectionUtils.isEmpty(workFlowRawData)) {
            errors.add(new RuntimeException("Error compiling source '" + parsedSlang.getName() + "'. Flow: '" + executableName + "' has no workflow data"));
        }
        for (Map<String, Map<String, Object>> step : workFlowRawData) {
            if (step.size() > 1) {
                errors.add(new RuntimeException("Error compiling source '" + parsedSlang.getName() + "'. Flow: '" + executableName +
                        "' has steps with keyword on the same indentation as the step name."));
            }
        }

        return workFlowRawData;
    }

    @Override
    public ExecutableModellingResult validateResult(ParsedSlang parsedSlang, Map<String, Object> executableRawData, ExecutableModellingResult result) {
        String executableName = getExecutableName(executableRawData);
        validateFileName(executableName, parsedSlang, result);
        validateNamespace(result);
        validateInputNamesDifferentFromOutputNames(result);

        if (SlangTextualKeys.FLOW_TYPE.equals(result.getExecutable().getType())) {
            validateFlow((Flow) result.getExecutable(), result);
        }

        return result;
    }

    @Override
    public List<RuntimeException> checkKeyWords(
            String dataLogicalName,
            String parentProperty,
            Map<String, Object> rawData,
            List<Transformer> allRelevantTransformers,
            List<String> additionalValidKeyWords,
            List<List<String>> constraintGroups) {
        Set<String> validKeywords = new HashSet<>();

        List<RuntimeException> errors = new ArrayList<>();
        if (additionalValidKeyWords != null) {
            validKeywords.addAll(additionalValidKeyWords);
        }

        for (Transformer transformer : allRelevantTransformers) {
            validKeywords.add(TransformersHandler.keyToTransform(transformer));
        }

        Set<String> rawDataKeySet = rawData.keySet();
        for (String key : rawDataKeySet) {
            if (!(exists(validKeywords, equalToIgnoringCase(key)))) {
                String additionalParentPropertyMessage =
                        StringUtils.isEmpty(parentProperty) ? "" : " under \'" + parentProperty + "\'";
                errors.add(new RuntimeException("Artifact {" + dataLogicalName + "} has unrecognized tag {" + key + "}" +
                        additionalParentPropertyMessage + ". Please take a look at the supported features per versions link"));
            }
        }

        if (constraintGroups != null) {
            for (List<String> group : constraintGroups) {
                boolean found = false;
                for (String key : group) {
                    if (rawDataKeySet.contains(key)) {
                        if (found) {
                            // one key from this group was already found in action data
                            errors.add(new RuntimeException("Conflicting keys at: " + dataLogicalName));
                        } else {
                            found = true;
                        }
                    }
                }
            }
        }
        return errors;
    }

    public String getExecutableName(Map<String, Object> executableRawData) {
        String execName = (String) executableRawData.get(SlangTextualKeys.EXECUTABLE_NAME_KEY);
        return execName == null ? "" : execName;
    }

    private void validateFlow(Flow compiledFlow, ExecutableModellingResult result) {
        if (CollectionUtils.isEmpty(compiledFlow.getWorkflow().getSteps())) {
            result.getErrors().add(new RuntimeException("Flow: " + compiledFlow.getName() + " has no steps"));
        } else {
            RuntimeException exception = validateNavigation(compiledFlow.getWorkflow().getSteps().getFirst(), compiledFlow.getWorkflow().getSteps(),
                    getStepNames(compiledFlow));
            if (exception != null) {
                result.getErrors().add(exception);
            }
        }
    }

    private Set<String> getStepNames(Flow compiledFlow) {
        Set<String> stepNames = new HashSet<>();
        for (Result result : compiledFlow.getResults()) {
            stepNames.add(result.getName());
        }
        return stepNames;
    }

    private RuntimeException validateNavigation(Step step, Deque<Step> steps, Set<String> stepNames) {
        String stepName = step.getName();
        stepNames.add(stepName);
        for (Map<String, String> map : step.getNavigationStrings()) {
            Map.Entry<String, String> entry = map.entrySet().iterator().next();
            String nextStepName = entry.getValue();
            if (!stepNames.contains(nextStepName)) {
                Step nextStepToCompile = Lambda.selectFirst(steps, having(on(Step.class).getName(), equalTo(nextStepName)));
                if (nextStepToCompile == null) {
                    return new RuntimeException("Failed to compile step: " + stepName + ". The step/result name: " + entry.getValue() + " of navigation: " + entry.getKey() + " -> " + entry.getValue() + " is missing");
                }
                return validateNavigation(nextStepToCompile, steps, stepNames);
            }
        }
        return null;
    }

    private void validateFileName(String executableName, ParsedSlang parsedSlang, ExecutableModellingResult result) {
        String fileName = parsedSlang.getName();
        Extension fileExtension = parsedSlang.getFileExtension();
        if (StringUtils.isNotEmpty(executableName) && !executableName.equals(fileName)) {
            if (fileExtension == null) {
                result.getErrors().add(new IllegalArgumentException("Operation/Flow " + executableName +
                        " is declared in a file named \"" + fileName + "\"," +
                        " it should be declared in a file named \"" + executableName + "\" plus a valid " +
                        "extension(" + Extension.getExtensionValuesAsString() + ") separated by \".\""));
            } else {
                result.getErrors().add(new IllegalArgumentException("Operation/Flow " + executableName +
                        " is declared in a file named \"" + fileName + "." + fileExtension.getValue() + "\"" +
                        ", it should be declared in a file named \"" + executableName + "." + fileExtension.getValue() + "\""));
            }
        }
    }

    private void validateNamespace(ExecutableModellingResult result) {
        if (result.getExecutable().getNamespace() == null || result.getExecutable().getNamespace().length() == 0) {
            result.getErrors().add(new IllegalArgumentException("Operation/Flow " + result.getExecutable().getName() + " must have a namespace"));
        }
    }

    private void validateInputNamesDifferentFromOutputNames(ExecutableModellingResult result) {
        List<Input> inputs = result.getExecutable().getInputs();
        List<Output> outputs = result.getExecutable().getOutputs();
        String errorMessage = "Inputs and outputs names should be different for \"" +
                result.getExecutable().getId() + "\". " +
                "Please rename input/output \"" + NAME_PLACEHOLDER + "\"";
        try {
            validateListsHaveMutuallyExclusiveNames(inputs, outputs, errorMessage);
        } catch (RuntimeException e) {
            result.getErrors().add(e);
        }
    }

}
