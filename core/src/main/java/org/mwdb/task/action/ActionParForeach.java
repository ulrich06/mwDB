package org.mwdb.task.action;

import org.mwdb.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ActionParForeach implements KTaskAction {

    private final KTask _subTask;

    public ActionParForeach(final KTask p_subTask) {
        _subTask = p_subTask;
    }

    @Override
    public void eval(KTaskContext context) {
        final Object[] castedResult = convert(context.getPreviousResult());
        final KTaskContext[] results = new KTaskContext[castedResult.length];
        final KDeferCounter counter = context.graph().counter(castedResult.length);
        counter.then(new KCallback() {
            @Override
            public void on(Object ignored) {
                context.setResult(results);
                context.next();
            }
        });
        for (int i = 0; i < castedResult.length; i++) {
            final int finalI = i;
            _subTask.executeThenAsync(context, castedResult[finalI], new KTaskAction() {
                @Override
                public void eval(final KTaskContext subTaskFinalContext) {
                    results[finalI] = subTaskFinalContext;
                    counter.count();
                }
            });
        }
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
