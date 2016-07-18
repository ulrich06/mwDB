package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.Constants;
import org.mwg.task.TaskAction;
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
    public void eval(final TaskContext context) {

        /*

        if (_condition.eval(context)) {
            _action.executeFrom(context, context.result(), new Callback<Object>() {
                @Override
                public void on(Object res) {
                    context.setResult(res);
                }
            });
        } else {
            context.setResult(context.result());
        }

        */

    }

    @Override
    public String toString() {
        return "ifThen()";
    }

}
