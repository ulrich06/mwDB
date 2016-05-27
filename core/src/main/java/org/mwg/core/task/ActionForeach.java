package org.mwg.core.task;

import org.mwg.task.Task;
import org.mwg.task.TaskAction;
import org.mwg.task.Action;
import org.mwg.task.TaskContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

class ActionForeach implements TaskAction {

    private final Task _subTask;

    ActionForeach(final Task p_subTask) {
        _subTask = p_subTask;
    }

    @Override
    public void eval(TaskContext context) {
        final ActionForeach selfPointer = this;
        final Object[] castedResult = convert(context.getPreviousResult());
        AtomicInteger cursor = new AtomicInteger(0);
        final TaskContext[] results = new CoreTaskContext[castedResult.length];
        Action[] recursiveAction = new Action[1];
        recursiveAction[0] = new Action() {
            @Override
            public void eval(final TaskContext subTaskFinalContext) {
                int current = cursor.getAndIncrement();
                results[current] = subTaskFinalContext;
                int nextCursot = current + 1;
                if (nextCursot == results.length) {
                    context.setResult(results);
                    context.next();
                } else {
                    //recursive call
                    selfPointer._subTask.executeThenAsync(context, castedResult[nextCursot], recursiveAction[0]);
                }
            }
        };
        _subTask.executeThenAsync(context, castedResult[0], recursiveAction[0]);
    }

    /**
     * @native ts
     * var result : any[] = [];
     * for(var p in elem){
     * result.push(elem[p]);
     * }
     * return result;
     */
    static Object[] convert(Object elem) {
        if (elem instanceof Object[]) {
            return (Object[]) elem;
        } else if (elem instanceof boolean[]) {
            boolean[] casted = (boolean[]) elem;
            Boolean[] sub = new Boolean[casted.length];
            for (int i = 0; i < casted.length; i++) {
                sub[i] = casted[i];
            }
            return sub;
        } else if (elem instanceof byte[]) {
            byte[] casted = (byte[]) elem;
            Byte[] sub = new Byte[casted.length];
            for (int i = 0; i < casted.length; i++) {
                sub[i] = casted[i];
            }
            return sub;
        } else if (elem instanceof short[]) {
            short[] casted = (short[]) elem;
            Short[] sub = new Short[casted.length];
            for (int i = 0; i < casted.length; i++) {
                sub[i] = casted[i];
            }
            return sub;
        } else if (elem instanceof char[]) {
            char[] casted = (char[]) elem;
            Character[] sub = new Character[casted.length];
            for (int i = 0; i < casted.length; i++) {
                sub[i] = casted[i];
            }
            return sub;
        } else if (elem instanceof int[]) {
            int[] casted = (int[]) elem;
            Integer[] sub = new Integer[casted.length];
            for (int i = 0; i < casted.length; i++) {
                sub[i] = casted[i];
            }
            return sub;
        } else if (elem instanceof long[]) {
            long[] casted = (long[]) elem;
            Long[] sub = new Long[casted.length];
            for (int i = 0; i < casted.length; i++) {
                sub[i] = casted[i];
            }
            return sub;
        } else if (elem instanceof float[]) {
            float[] casted = (float[]) elem;
            Float[] sub = new Float[casted.length];
            for (int i = 0; i < casted.length; i++) {
                sub[i] = casted[i];
            }
            return sub;
        } else if (elem instanceof double[]) {
            double[] casted = (double[]) elem;
            Double[] sub = new Double[casted.length];
            for (int i = 0; i < casted.length; i++) {
                sub[i] = casted[i];
            }
            return sub;
        } else if (elem instanceof Iterable) {
            List<Object> temp = new ArrayList<Object>();
            Iterator it = ((Iterable) elem).iterator();
            while (it.hasNext()) {
                temp.add(it.next());
            }
            return temp.toArray(new Object[temp.size()]);
        } else {
            Object[] obj = new Object[1];
            obj[0] = elem;
            return obj;
        }
    }


}
