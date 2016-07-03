package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.DeferCounter;
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
        final Object[] castedResult = ActionForeach.convert(context.result());
        final Object[] results = new Object[castedResult.length];
        final DeferCounter counter = context.graph().newCounter(castedResult.length);
        counter.then(new Job() {
            @Override
            public void run() {
                context.setUnsafeResult(results);
            }
        });
        for (int i = 0; i < castedResult.length; i++) {
            final int finalI = i;
            final Object loopInput = castedResult[finalI];
            _subTask.executeWith(context.graph(), context, castedResult[finalI], new Callback<Object>() {
                @Override
                public void on(final Object result) {
                    context.cleanObj(loopInput);
                    results[finalI] = result;
                    counter.count();
                }
            });
        }
    }

}
