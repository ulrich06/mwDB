package org.mwg.core.task;

import org.mwg.Callback;
import org.mwg.core.utility.GenericIterable;
import org.mwg.plugin.Job;
import org.mwg.task.Task;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

class ActionForeach implements TaskAction {

    private final Task _subTask;

    ActionForeach(final Task p_subTask) {
        _subTask = p_subTask;
    }


    /*
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
            final DeferCounterSync waiter = context.graph().newSyncCounter(1);
            _subTask.executeFrom(context, loop, waiter.wrap());

            if (index >= results.length) {
                Object[] doubled_res = new Object[results.length * 2];
                System.arraycopy(results, 0, doubled_res, 0, results.length);
                results = doubled_res;
            }
            //TODO rewrite with a pure iteration, DeferWaiter are slow
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

    }*/


    @Override
    public void eval(final TaskContext context) {
        final ActionForeach selfPointer = this;
        final Object previousResult = context.result();
        if (previousResult == null) {
            context.setUnsafeResult(null);
        } else {
            final GenericIterable genericIterable = new GenericIterable(previousResult);
            int plainEstimation = genericIterable.estimate();
            final Object[] plainResults;
            final Map<Integer, Object> dynResults;
            if (plainEstimation != -1) {
                plainResults = new Object[plainEstimation];
                dynResults = null;
            } else {
                plainResults = null;
                dynResults = new HashMap<Integer, Object>();
            }
            final AtomicInteger cursor = new AtomicInteger(0);
            final Callback[] recursiveAction = new Callback[1];
            final Object[] loopRes = new Object[1];
            recursiveAction[0] = new Callback() {
                @Override
                public void on(final Object res) {
                    int current = cursor.getAndIncrement();
                    if (plainResults != null) {
                        plainResults[current] = res;
                    } else {
                        dynResults.put(current, res);
                    }
                    context.cleanObj(loopRes);
                    loopRes[0] = genericIterable.next();
                    if (loopRes[0] == null) {
                        Object[] finalResults;
                        if (plainResults != null) {
                            finalResults = new Object[current + 1];
                            System.arraycopy(plainResults, 0, finalResults, 0, current + 1);
                        } else {
                            int mapSize = dynResults.size();
                            finalResults = new Object[mapSize];
                            for (int i = 0; i < mapSize; i++) {
                                finalResults[i] = dynResults.get(i);
                            }
                        }
                        context.setUnsafeResult(finalResults);
                    } else {
                        selfPointer._subTask.executeFrom(context, loopRes[0], recursiveAction[0]);
                    }
                }
            };
            loopRes[0] = genericIterable.next();
            context.graph().scheduler().dispatch(new Job() {
                @Override
                public void run() {
                    _subTask.executeFrom(context, loopRes[0], recursiveAction[0]);
                }
            });
        }
    }

    @Override
    public String toString() {
        return "foreach()";
    }


}
