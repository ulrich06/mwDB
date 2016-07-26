package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.DeferCounter;
import org.mwg.plugin.AbstractTaskAction;
import org.mwg.plugin.Job;
import org.mwg.plugin.SchedulerAffinity;
import org.mwg.task.Task;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

class ActionRepeatPar extends AbstractTaskAction {

    private final Task _subTask;

    private final String _iterationTemplate;

    ActionRepeatPar(final String p_iteration, final Task p_subTask) {
        super();
        this._subTask = p_subTask;
        this._iterationTemplate = p_iteration;
    }

    @Override
    public void eval(final TaskContext context) {
        final int nbIteration = TaskHelper.parseInt(context.template(_iterationTemplate));
        final TaskResult next = context.wrap(null);
        next.allocate(nbIteration);
        if (nbIteration > 0) {
            DeferCounter waiter = context.graph().newCounter(nbIteration);
            for (int i = 0; i < nbIteration; i++) {
                final int finalI = i;
                _subTask.executeFrom(context, context.wrap(finalI), SchedulerAffinity.ANY_LOCAL_THREAD, new Callback<TaskResult>() {
                    @Override
                    public void on(TaskResult result) {
                        if (result != null && result.size() == 1) {
                            next.add(result.get(0));
                        } else {
                            next.add(result);
                        }
                        waiter.count();
                    }
                });
            }
            waiter.then(new Job() {
                @Override
                public void run() {
                    context.continueWith(next);
                }
            });
        } else {
            context.continueWith(next);
        }
    }

    @Override
    public String toString() {
        return "repeatPar(\'" + _iterationTemplate + "\')";
    }

}
