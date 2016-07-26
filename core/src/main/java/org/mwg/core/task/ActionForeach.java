package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.plugin.AbstractTaskAction;
import org.mwg.plugin.Job;
import org.mwg.plugin.SchedulerAffinity;
import org.mwg.task.*;

import java.util.concurrent.atomic.AtomicInteger;

class ActionForeach extends AbstractTaskAction {

    private final Task _subTask;

    ActionForeach(final Task p_subTask) {
        super();
        _subTask = p_subTask;
    }

    @Override
    public void eval(final TaskContext context) {
        final ActionForeach selfPointer = this;
        final TaskResult previousResult = context.result();
        if (previousResult == null) {
            context.continueTask();
        } else {
            final TaskResultIterator it = previousResult.iterator();
            final TaskResult finalResult = context.wrap(null);
            finalResult.allocate(previousResult.size());
            final AtomicInteger cursor = new AtomicInteger(0);
            final Callback[] recursiveAction = new Callback[1];
            final TaskResult[] loopRes = new TaskResult[1];
            recursiveAction[0] = new Callback<TaskResult>() {
                @Override
                public void on(final TaskResult res) {
                    int current = cursor.getAndIncrement();
                    if (res != null && res.size() == 1) {
                        finalResult.set(current, res.get(0));
                    } else {
                        finalResult.set(current, res);
                    }
                    loopRes[0].free();
                    Object nextResult = it.next();
                    if (nextResult != null) {
                        loopRes[0] = context.wrap(nextResult);
                    } else {
                        loopRes[0] = null;
                    }
                    if (nextResult == null) {
                        context.continueWith(finalResult);
                    } else {
                        selfPointer._subTask.executeFrom(context, loopRes[0], SchedulerAffinity.SAME_THREAD, recursiveAction[0]);
                    }
                }
            };
            Object nextRes = it.next();
            loopRes[0] = context.wrap(nextRes);
            if (nextRes != null) {
                context.graph().scheduler().dispatch(SchedulerAffinity.SAME_THREAD, new Job() {
                    @Override
                    public void run() {
                        _subTask.executeFrom(context, context.wrap(loopRes[0]), SchedulerAffinity.SAME_THREAD, recursiveAction[0]);
                    }
                });
            } else {
                context.continueWith(finalResult);
            }
        }
    }

    @Override
    public String toString() {
        return "foreach()";
    }


}
