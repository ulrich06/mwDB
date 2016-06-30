package org.mwg.core.task;

import org.mwg.task.Action;
import org.mwg.task.Task;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

import java.util.concurrent.atomic.AtomicInteger;

class ActionLoop implements TaskAction {

    private final Task _subTask;

    private final int _iteration;

    ActionLoop(int p_iteration, final Task p_subTask) {
        this._subTask = p_subTask;
        this._iteration = p_iteration;
    }

    @Override
    public void eval(final TaskContext context) {
        final ActionLoop selfPointer = this;
        final AtomicInteger cursor = new AtomicInteger(0);
        final TaskContext[] results = new CoreTaskContext[_iteration];
        if (_iteration > 0) {
            final Action[] recursiveAction = new Action[1];
            recursiveAction[0] = new Action() {
                @Override
                public void eval(final TaskContext subTaskFinalContext) {
                    int current = cursor.getAndIncrement();
                    results[current] = subTaskFinalContext;
                    int nextCursor = current + 1;
                    if (nextCursor == results.length) {
                        context.setResult(results);
                        context.next();
                    } else {
                        //recursive call
                        selfPointer._subTask.executeThenAsync(context.graph(), context, nextCursor, recursiveAction[0]);
                    }
                }
            };
            _subTask.executeThenAsync(context.graph(), context, cursor.get(), recursiveAction[0]);
        } else {
            context.setResult(results);
            context.next();
        }
    }

}
