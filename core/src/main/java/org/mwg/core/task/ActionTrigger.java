package org.mwg.core.task;

import org.mwg.Callback;
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
        _subTask.executeWith(context.graph(), context, context.result(), new Callback<Object>() {
            @Override
            public void on(Object result) {
                context.setResult(result);
            }
        });
    }

}
