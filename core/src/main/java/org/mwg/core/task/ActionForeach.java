package org.mwg.core.task;

import org.mwg.DeferCounter;
import org.mwg.core.utility.GenericIterable;
import org.mwg.task.Task;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

class ActionForeach implements TaskAction {

    private final Task _subTask;

    ActionForeach(final Task p_subTask) {
        _subTask = p_subTask;
    }

    @Override
    public void eval(final TaskContext context) {
        final Object previousResult = context.result();
        final GenericIterable genericIterable = new GenericIterable(previousResult);
        int plainEstimation = genericIterable.estimate();
        if (plainEstimation == -1) {
            plainEstimation = 16;
        }
        Object[] results = new Object[plainEstimation];
        int index = 0;

        Object loop = genericIterable.next();
        while (loop != null) {
            final DeferCounter waiter = context.graph().newCounter(1);
            _subTask.executeFrom(context, loop, waiter.wrap());

            if (index >= results.length) {
                Object[] doubled_res = new Object[results.length * 2];
                System.arraycopy(results, 0, doubled_res, 0, results.length);
                results = doubled_res;
            }

            results[index] = waiter.waitResult();
            index++;
            context.cleanObj(loop);
            loop = genericIterable.next();
        }

        if (index != results.length) {
            Object[] shrinked_res = new Object[index];
            System.arraycopy(results, 0, shrinked_res, 0, index);
            results = shrinked_res;
        }

        context.cleanObj(previousResult);
        context.setUnsafeResult(results);

    }

    @Override
    public String toString() {
        return "foreach()";
    }


}
