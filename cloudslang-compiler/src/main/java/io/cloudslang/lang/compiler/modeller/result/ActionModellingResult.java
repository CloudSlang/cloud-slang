package io.cloudslang.lang.compiler.modeller.result;

import io.cloudslang.lang.compiler.modeller.model.Action;
import java.util.List;

/**
 * Created by Ifat Gavish on 24/02/2016
 */
public class ActionModellingResult implements ModellingResult {

    private final Action action;
    private final List<RuntimeException> errors;

    public ActionModellingResult(Action action, List<RuntimeException> errors) {
        this.action = action;
        this.errors = errors;
    }

    public Action getAction() {
        return action;
    }

    public List<RuntimeException> getErrors() {
        return errors;
    }
}
