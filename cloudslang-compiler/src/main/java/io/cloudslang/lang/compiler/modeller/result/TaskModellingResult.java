package io.cloudslang.lang.compiler.modeller.result;

import io.cloudslang.lang.compiler.modeller.model.Task;

import java.util.List;

/**
 * Created by Ifat Gavish on 24/02/2016
 */
public class TaskModellingResult implements ModellingResult {

    private final Task task;
    private final List<RuntimeException> errors;

    public TaskModellingResult(Task task, List<RuntimeException> errors) {
        this.task = task;
        this.errors = errors;
    }

    public Task getTask() {
        return task;
    }

    public List<RuntimeException> getErrors() {
        return errors;
    }
}
