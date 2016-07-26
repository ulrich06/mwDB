package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.plugin.AbstractTaskAction;
import org.mwg.plugin.SchedulerAffinity;
import org.mwg.task.*;

class ActionWhileDo extends AbstractTaskAction {

    private final TaskFunctionConditional _cond;
    private final Task _then;

    ActionWhileDo(final TaskFunctionConditional p_cond, final Task p_then) {
        super();
        this._cond = p_cond;
        this._then = p_then;
    }

    @Override
    public void eval(final TaskContext context) {
        final ActionWhileDo selfPointer = this;
        final Callback[] recursiveAction = new Callback[1];
        final TaskResult[] loopRes = new TaskResult[1];
        recursiveAction[0] = new Callback<TaskResult>() {
            @Override
            public void on(final TaskResult res) {
                if (_cond.eval(context)) {
                    loopRes[0].free();
                    loopRes[0] = res;
                    selfPointer._then.executeFrom(context, context.wrap(loopRes[0]), SchedulerAffinity.SAME_THREAD, recursiveAction[0]);
                } else {
                    context.continueWith(res);
                }
            }
        };
        if (_cond.eval(context)) {
            loopRes[0] = context.result();
            _then.executeFrom(context, loopRes[0], SchedulerAffinity.SAME_THREAD, recursiveAction[0]);
        } else {
            context.continueTask();
        }
    }

    @Override
    public String toString() {
        return "whileDo()";
    }
}
