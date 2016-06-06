package org.mwg.core.task;

import org.mwg.*;
import org.mwg.task.Task;
import org.mwg.task.TaskAction;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;

class ActionForeachPar implements TaskAction {

    private final Task _subTask;

    ActionForeachPar(final Task p_subTask) {
        _subTask = p_subTask;
    }

    @Override
    public void eval(TaskContext context) {
        final Object[] castedResult = ActionForeach.convert(context.getPreviousResult());
        final TaskContext[] results = new CoreTaskContext[castedResult.length];
        final DeferCounter counter = context.graph().counter(castedResult.length);
        counter.then(new Callback() {
            @Override
            public void on(Object ignored) {
                context.setResult(results);
                context.next();
            }
        });
        for (int i = 0; i < castedResult.length; i++) {
            final int finalI = i;
            _subTask.executeThenAsync(context, castedResult[finalI], new Action() {
                @Override
                public void eval(final TaskContext subTaskFinalContext) {
                    results[finalI] = subTaskFinalContext;
                    counter.count();
                }
            });
        }
    }

}
