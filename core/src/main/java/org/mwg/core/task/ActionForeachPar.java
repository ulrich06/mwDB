package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.DeferCounter;
import org.mwg.core.utility.GenericIterable;
import org.mwg.plugin.Job;
import org.mwg.task.Task;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionForeachPar implements TaskAction {

    private final Task _subTask;

    ActionForeachPar(final Task p_subTask) {
        _subTask = p_subTask;
    }

    @Override
    public void eval(final TaskContext context) {
        final Object previousResult = context.result();
        final GenericIterable genericIterable = new GenericIterable(previousResult);
        int index = 0;
        int plainEstimation = genericIterable.estimate();
        if (plainEstimation == -1) {
            throw new RuntimeException("Foreach on non array structure are not supported yet!");
        }
        final Object[] flatResult = new Object[plainEstimation];
        final DeferCounter waiter = context.graph().newCounter(plainEstimation);

        Object loopObj = genericIterable.next();
        while (loopObj != null) {
            final int finalIndex = index;
            index++;
            Object finalLoopObj = loopObj;
            _subTask.executeFrom(context, loopObj, new Callback<Object>() {
                @Override
                public void on(Object result) {
                    flatResult[finalIndex] = result;
                    context.cleanObj(finalLoopObj);
                    waiter.count();
                }
            });
            loopObj = genericIterable.next();
        }
        waiter.then(new Job() {
            @Override
            public void run() {
                context.setUnsafeResult(flatResult);
            }
        });
    }

    @Override
    public String toString() {
        return "foreachPar()";
    }

}
