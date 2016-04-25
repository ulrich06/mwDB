package org.mwg.core.task;

import org.mwg.task.Task;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionTrigger implements TaskAction {

    private final Task _subTask;

    ActionTrigger(final Task p_subTask) {
        _subTask = p_subTask;
    }

    @Override
    public void eval(TaskContext context) {
        _subTask.executeThenAsync(context, context.getPreviousResult(), new TaskAction() {
            @Override
            public void eval(TaskContext subTaskFinalContext) {
                context.setResult(subTaskFinalContext);
                context.next();
            }
        });
    }

}
