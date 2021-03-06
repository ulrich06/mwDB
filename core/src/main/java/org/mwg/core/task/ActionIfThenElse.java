package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.plugin.AbstractTaskAction;
import org.mwg.plugin.SchedulerAffinity;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskFunctionConditional;
import org.mwg.task.TaskResult;

class ActionIfThenElse extends AbstractTaskAction {

    private TaskFunctionConditional _condition;
    private org.mwg.task.Task _thenSub;
    private org.mwg.task.Task _elseSub;

    ActionIfThenElse(final TaskFunctionConditional cond, final org.mwg.task.Task p_thenSub, final org.mwg.task.Task p_elseSub) {
        super();
        this._condition = cond;
        this._thenSub = p_thenSub;
        this._elseSub = p_elseSub;
    }

    @Override
    public void eval(final TaskContext context) {
        if (_condition.eval(context)) {
            _thenSub.executeFrom(context, context.result(), SchedulerAffinity.SAME_THREAD, new Callback<TaskResult>() {
                @Override
                public void on(TaskResult res) {
                    context.continueWith(res);
                }
            });
        } else {
            _elseSub.executeFrom(context, context.result(), SchedulerAffinity.SAME_THREAD, new Callback<TaskResult>() {
                @Override
                public void on(TaskResult res) {
                    context.continueWith(res);
                }
            });
        }
    }

    @Override
    public String toString() {
        return "ifThen()";
    }

}
