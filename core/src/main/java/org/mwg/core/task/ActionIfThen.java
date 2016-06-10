package org.mwg.core.task;

import org.mwg.task.TaskAction;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskFunctionConditional;

class ActionIfThen implements TaskAction {
    private TaskFunctionConditional _condition;
    private org.mwg.task.Task _action;

    ActionIfThen(TaskFunctionConditional cond, org.mwg.task.Task action) {
        _condition = cond;
        _action = action;
    }

    @Override
    public void eval(TaskContext context) {
        if (_condition.eval(context)) {
            _action.executeThenAsync(context, context.result(), new Action() {
                @Override
                public void eval(TaskContext subTaskFinalContext) {
                    context.setResult(subTaskFinalContext);
                    context.next();
                }
            });
        } else {
            context.setResult(context.result());
            context.next();
        }
    }
}
