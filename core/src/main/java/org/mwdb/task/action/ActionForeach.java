package org.mwdb.task.action;

import org.mwdb.KTask;
import org.mwdb.KTaskAction;
import org.mwdb.KTaskContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ActionForeach implements KTaskAction {

    private final KTask _subTask;

    public ActionForeach(final KTask p_subTask) {
        _subTask = p_subTask;
    }

    @Override
    public void eval(KTaskContext context) {
        final Object[] castedResult = convert(context.getPreviousResult());
        AtomicInteger cursor = new AtomicInteger(0);
        final KTaskContext[] results = new KTaskContext[castedResult.length];
        _subTask.executeAsyncThen(context, castedResult[0], new KTaskAction() {
            @Override
            public void eval(final KTaskContext subTaskFinalContext) {
                int current = cursor.getAndIncrement();
                results[current] = subTaskFinalContext;
                int nextCursot = current + 1;
                if (nextCursot == results.length) {
                    context.setResult(results);
                    context.next();
                } else {
                    //recursive call
                    _subTask.executeAsyncThen(context, castedResult[nextCursot], this);
                }
            }
        });
    }

    private Object[] convert(Object elem) {
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
