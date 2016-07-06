package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.Constants;
import org.mwg.task.Task;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionTrigger implements TaskAction {

    private final Task _subTask;

    ActionTrigger(final Task p_subTask) {
        _subTask = p_subTask;
    }

    @Override
    public void eval(final TaskContext context) {
        Object previous = context.result();
        _subTask.executeFrom(context, previous, new Callback<Object>() {
            @Override
            public void on(Object result) {
                context.cleanObj(previous);
                context.setUnsafeResult(result);
            }
        });
    }

    @Override
    public String toString() {
        return "trigger()";
    }

}
