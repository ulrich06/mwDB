package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.plugin.Job;
import org.mwg.task.*;

import java.util.concurrent.atomic.AtomicInteger;

class ActionWhileDo implements TaskAction {

    private final TaskFunctionConditional _cond;

    private final Task _then;

    ActionWhileDo(final TaskFunctionConditional p_cond, final Task p_then) {
        _cond = p_cond;
        _then = p_then;
    }

    @Override
    public void eval(TaskContext context) {
        final ActionWhileDo selfPointer = this;
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
                    finalResult.set(current, res);
                    loopRes[0].free();
                    Object nextResult = it.next();
                    if (_cond.eval(context)) {
                        loopRes[0] = context.wrap(it.next());
                    } else {
                        loopRes[0] = null;
                    }
                    if (nextResult == null) {
                        context.continueWith(finalResult);
                    } else {
                        selfPointer._then.executeFrom(context, context.wrap(loopRes[0]), recursiveAction[0]);
                    }
                }
            };

            if (_cond.eval(context)) {
                loopRes[0] = context.wrap(it.next());
                context.graph().scheduler().dispatch(new Job() {
                    @Override
                    public void run() {
                        _then.executeFrom(context, context.wrap(loopRes[0]), recursiveAction[0]);
                    }
                });
            } else {
                context.continueWith(finalResult);
            }
        }
    }

}
