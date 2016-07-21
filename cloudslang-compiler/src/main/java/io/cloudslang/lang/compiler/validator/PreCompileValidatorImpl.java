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
import io.cloudslang.lang.entities.bindings.Argument;
import io.cloudslang.lang.entities.bindings.InOutParam;
import io.cloudslang.lang.entities.bindings.Input;
import io.cloudslang.lang.entities.bindings.Output;
import io.cloudslang.lang.entities.bindings.Result;
import io.cloudslang.lang.entities.utils.SetUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;

import static ch.lambdaj.Lambda.exists;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static io.cloudslang.lang.compiler.SlangTextualKeys.ON_FAILURE_KEY;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;

/**
 * User: bancl
 * Date: 6/17/2016
 */
@Component
public class PreCompileValidatorImpl extends AbstractValidator implements PreCompileValidator {

    public static final String MULTIPLE_ON_FAILURE_MESSAGE_SUFFIX = "Multiple 'on_failure' steps found";
    public static final String ON_FAILURE_LAST_STEP_MESSAGE_SUFFIX = "'on_failure' should be last step in the workflow";

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
                errors.add(new RuntimeException("Error compiling source '" + parsedSlang.getName() + "'.\nFlow: '" + executableName +
                        "' has steps with keyword on the same indentation as the step name or there is no space between step name and hyphen."));
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
                String lastKeyFound = null;
                for (String key : group) {
                    if (rawDataKeySet.contains(key)) {
                        if (lastKeyFound != null) {
                            // one key from this group was already found in action data
                            errors.add(new RuntimeException("Conflicting keys[" + lastKeyFound + ", " + key + "] at: " + dataLogicalName));
                        } else {
                            lastKeyFound = key;
                        }
                    }
                }
            }
        }
        return errors;
    }

    @Override
    public Map<String, Map<String, Object>> validateOnFailurePosition(
            List<Map<String, Map<String, Object>>> workFlowRawData,
            String execName,
            List<RuntimeException> errors) {
        Iterator<Map<String, Map<String, Object>>> stepsIterator = workFlowRawData.iterator();
        int onFailureCount = 0;
        String latestStepName = null;
        List<RuntimeException> onFailureErrors = new ArrayList<>();
        Map<String, Map<String, Object>> onFailureData = null;

        while (stepsIterator.hasNext()) {
            Map<String, Map<String, Object>> stepData = stepsIterator.next();
            latestStepName = stepData.keySet().iterator().next();
            if (latestStepName.equals(ON_FAILURE_KEY)) {
                if (onFailureCount == 1) {
                    onFailureErrors.add(new RuntimeException("Flow: '" + execName + "' syntax is illegal.\n" + MULTIPLE_ON_FAILURE_MESSAGE_SUFFIX));
                }
                ++onFailureCount;
                onFailureData = stepData;
                stepsIterator.remove();
            }
        }
        // exactly one on_failure -> need to be last step
        if (onFailureCount == 1) {
            if (!ON_FAILURE_KEY.equals(latestStepName)) {
                onFailureErrors.add(new RuntimeException("Flow: '" + execName + "' syntax is illegal.\n" + ON_FAILURE_LAST_STEP_MESSAGE_SUFFIX));
            }
        }

        if (CollectionUtils.isEmpty(onFailureErrors)) {
            return onFailureData;
        } else {
            errors.addAll(onFailureErrors);
            return null;
        }
    }

    @Override
    public void validateResultsSection(
            Map<String, Object> executableRawData,
            String artifact,
            List<RuntimeException> errors) {
        Object resultsValue = executableRawData.get(SlangTextualKeys.RESULTS_KEY);
        if (resultsValue == null || (resultsValue instanceof List && ((List) resultsValue).isEmpty())) {
            errors.add(
                    new RuntimeException(
                            "Artifact {" + artifact + "} syntax is invalid: " +
                                    "'" + SlangTextualKeys.RESULTS_KEY +
                                    "' section cannot be empty for executable type '" +
                                    ParsedSlang.Type.DECISION.key() + "'"
                    )
            );
        }
    }

    @Override
    public void validateNoDuplicateInOutParams(List<? extends InOutParam> inputs, InOutParam element) {
        Collection<InOutParam> inOutParams = new ArrayList<>();
        inOutParams.addAll(inputs);

        String message = "Duplicate " + getMessagePart(element) + " found: " + element.getName();
        validateNotDuplicateInOutParam(inOutParams, element, message);
    }

    private String getMessagePart(InOutParam element) {
        String messagePart = "";
        if (element instanceof Input) {
            messagePart = "input";
        } else if (element instanceof Argument) {
            messagePart = "step input";
        } else if (element instanceof Output) {
            messagePart = "output / publish value";
        } else if (element instanceof Result) {
            messagePart = "result";
        }
        return messagePart;
    }

    private String getExecutableName(Map<String, Object> executableRawData) {
        String execName = (String) executableRawData.get(SlangTextualKeys.EXECUTABLE_NAME_KEY);
        return execName == null ? "" : execName;
    }

    private void validateFlow(Flow compiledFlow, ExecutableModellingResult result) {
        if (CollectionUtils.isEmpty(compiledFlow.getWorkflow().getSteps())) {
            result.getErrors().add(new RuntimeException("Flow: " + compiledFlow.getName() + " has no steps"));
        } else {
            List<RuntimeException> errors = result.getErrors();
            Set<String> reachableStepNames = new HashSet<>();
            Set<String> reachableResultNames = new HashSet<>();
            Set<String> resultNames = getResultNames(compiledFlow);
            Deque<Step> steps = compiledFlow.getWorkflow().getSteps();

            validateNavigation(
                    compiledFlow.getWorkflow().getSteps().getFirst(),
                    steps,
                    resultNames,
                    reachableStepNames,
                    reachableResultNames,
                    errors
            );
            validateStepsAreReachable(reachableStepNames, steps, errors);
            validateResultsAreReachable(reachableResultNames, resultNames, errors);
        }
    }

    private Set<String> getResultNames(Flow compiledFlow) {
        Set<String> resultNames = new HashSet<>();
        for (Result result : compiledFlow.getResults()) {
            resultNames.add(result.getName());
        }
        return resultNames;
    }

    private void validateNavigation(
            Step currentStep,
            Deque<Step> steps,
            Set<String> resultNames,
            Set<String> reachableStepNames,
            Set<String> reachableResultNames,
            List<RuntimeException> errors) {
        String currentStepName = currentStep.getName();
        reachableStepNames.add(currentStepName);
        for (Map<String, String> map : currentStep.getNavigationStrings()) {
            Map.Entry<String, String> entry = map.entrySet().iterator().next();
            String navigationTarget = entry.getValue();
            if (!isProcessed(navigationTarget, reachableStepNames, reachableResultNames)) {
                boolean isResult = resultNames.contains(navigationTarget);
                Step nextStepToCompile = Lambda.selectFirst(steps, having(on(Step.class).getName(), equalTo(navigationTarget)));
                boolean isStep = nextStepToCompile != null;

                if (isStep && isResult) {
                    errors.add(
                            new RuntimeException(
                                "Navigation target: '" + navigationTarget + "' is declared both as step name and flow result."
                            )
                    );
                }
                if (isResult) {
                    reachableResultNames.add(navigationTarget);
                } else {
                    if (isStep) {
                        validateNavigation(nextStepToCompile, steps, resultNames, reachableStepNames, reachableResultNames, errors);
                    } else {
                        errors.add(
                                new RuntimeException(
                                        "Failed to compile step: " + currentStepName + ". The step/result name: " + entry.getValue() +
                                                " of navigation: " + entry.getKey() + " -> " + entry.getValue() + " is missing"
                                )
                        );
                    }
                }
            }
        }
    }

    private boolean isProcessed(String navigationTarget, Set<String> reachableStepNames, Set<String> reachableResultNames) {
        return reachableStepNames.contains(navigationTarget) || reachableResultNames.contains(navigationTarget);
    }

    private void validateStepsAreReachable(
            Set<String> reachableStepNames,
            Deque<Step> steps,
            List<RuntimeException> errors) {
        for (Step step : steps) {
            String stepName = step.getName();
            String messagePrefix = step.isOnFailureStep() ? "on_failure step '" : "Step '";
            if (!reachableStepNames.contains(stepName)) {
                errors.add(new RuntimeException(messagePrefix + stepName + "' is unreachable."));
            }
        }
    }

    private void validateResultsAreReachable(
            Set<String> reachableResultNames,
            Set<String> resultNames,
            List<RuntimeException> errors) {
        Set<String> unreachableResultNames = new HashSet<>(resultNames);
        unreachableResultNames.removeAll(reachableResultNames);
        if (!unreachableResultNames.isEmpty()) {
            errors.add(new RuntimeException("The following results are not wired: " + unreachableResultNames + "."));
        }
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

    private void validateNotDuplicateInOutParam(Collection<InOutParam> inOutParams, InOutParam element, String message) {
        if (SetUtils.containsIgnoreCaseBasedOnName(inOutParams, element)) {
            throw new RuntimeException(message);
        }
    }

}
