package io.cloudslang.lang.compiler.modeller.model;

import io.cloudslang.lang.entities.bindings.Argument;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ExternalStep extends Step {
    public ExternalStep(
            String name,
            Map<String, Serializable> preStepActionData,
            Map<String, Serializable> postStepActionData,
            List<Argument> arguments,
            List<Map<String, String>> navigationStrings,
            String refId,
            boolean parallelLoop,
            boolean onFailureStep
    ) {
        super(
                name,
                preStepActionData,
                postStepActionData,
                arguments,
                navigationStrings,
                refId,
                parallelLoop,
                onFailureStep
        );
    }
}
