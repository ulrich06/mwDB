package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.task.Action;
import org.mwg.task.Task;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

import java.util.concurrent.atomic.AtomicInteger;

class ActionRepeat implements TaskAction {

    private final Task _subTask;

    private final int _iteration;

    ActionRepeat(int p_iteration, final Task p_subTask) {
        this._subTask = p_subTask;
        this._iteration = p_iteration;
    }

    @Override
    public void eval(final TaskContext context) {
        final ActionRepeat selfPointer = this;
        final AtomicInteger cursor = new AtomicInteger(0);
        final Object[] results = new Object[_iteration];
        if (_iteration > 0) {
            final Callback[] recursiveAction = new Callback[1];
            recursiveAction[0] = new Callback() {
                @Override
                public void on(final Object res) {
                    int current = cursor.getAndIncrement();
                    results[current] = res;
                    int nextCursor = current + 1;
                    if (nextCursor == results.length) {
                        context.setResult(results);
                    } else {
                        //recursive call
                        selfPointer._subTask.executeWith(context.graph(), context, nextCursor, recursiveAction[0]);
                    }
                }
            };
            _subTask.executeWith(context.graph(), context, cursor.get(), recursiveAction[0]);
        } else {
            context.setResult(results);
        }
    }

}
