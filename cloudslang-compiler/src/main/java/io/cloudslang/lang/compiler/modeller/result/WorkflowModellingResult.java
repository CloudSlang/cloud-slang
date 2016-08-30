package io.cloudslang.lang.compiler.modeller.result;

import io.cloudslang.lang.compiler.modeller.model.Workflow;
import java.util.List;

/**
 * Created by Ifat Gavish on 24/02/2016
 */
public class WorkflowModellingResult implements ModellingResult {

    private final Workflow workflow;
    private final List<RuntimeException> errors;

    public WorkflowModellingResult(Workflow workflow, List<RuntimeException> errors) {
        this.workflow = workflow;
        this.errors = errors;
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public List<RuntimeException> getErrors() {
        return errors;
    }
}
