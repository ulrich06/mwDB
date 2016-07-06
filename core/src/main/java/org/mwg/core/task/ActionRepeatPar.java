package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.DeferCounter;
import org.mwg.plugin.Job;
import org.mwg.task.Task;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionRepeatPar implements TaskAction {

    private final Task _subTask;

    private final int _iteration;

    ActionRepeatPar(int p_iteration, final Task p_subTask) {
        this._subTask = p_subTask;
        this._iteration = p_iteration;
    }

    @Override
    public void eval(final TaskContext context) {
        final Object previous = context.result();
        context.cleanObj(previous);
        final Object[] results = new Object[_iteration];
        if (_iteration > 0) {
            DeferCounter waiter = context.graph().newCounter(_iteration);
            for (int i = 0; i < _iteration; i++) {
                final int finalI = i;
                _subTask.executeFromPar(context, finalI, new Callback<Object>() {
                    @Override
                    public void on(Object result) {
                        results[finalI] = result;
                        waiter.count();
                    }
                });
            }
            waiter.then(new Job() {
                @Override
                public void run() {
                    context.setUnsafeResult(results);
                }
            });
        } else {
            context.setResult(results);
        }
    }

    @Override
    public String toString() {
        return "repeatPar(\'" + _iteration + "\')";
    }

}
