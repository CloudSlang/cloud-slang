package io.cloudslang.lang.compiler.modeller.result;

import io.cloudslang.lang.compiler.modeller.model.Step;
import java.util.List;

/**
 * Created by Ifat Gavish on 24/02/2016
 */
public class StepModellingResult implements ModellingResult {

    private final Step step;
    private final List<RuntimeException> errors;

    public StepModellingResult(Step step, List<RuntimeException> errors) {
        this.step = step;
        this.errors = errors;
    }

    public Step getStep() {
        return step;
    }

    public List<RuntimeException> getErrors() {
        return errors;
    }
}
