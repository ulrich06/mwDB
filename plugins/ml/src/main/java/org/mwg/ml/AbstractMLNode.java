package org.mwg.ml;

import org.mwg.Callback;
import org.mwg.Constants;
import org.mwg.DeferCounter;
import org.mwg.Graph;
import org.mwg.plugin.AbstractNode;
import org.mwg.plugin.Job;
import org.mwg.task.Action;
import org.mwg.task.Task;
import org.mwg.task.TaskContext;

public abstract class AbstractMLNode extends AbstractNode {

    public static String FROM_SEPARATOR = ";";
    public static String FROM = "from";

    public AbstractMLNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    /**
     * If {@code obj} is null, throws {@code NullPointerException} with a {@code message}
     *
     * @param obj
     * @param message
     */
    protected static void requireNotNull(Object obj, String message) {
        if (obj == null) {
            throw new RuntimeException(message);
        }
    }

    /**
     * Asserts that condition is true. If not - throws {@code IllegalArgumentException} with a specified error message
     *
     * @param condition    Condition to test
     * @param errorMessage Error message thrown with {@code IllegalArgumentException} (if thrown)
     * @throws IllegalArgumentException if condition is false
     */
    protected void illegalArgumentIfFalse(boolean condition, String errorMessage) {
        assert errorMessage != null;
        if (!condition) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    protected void extractFeatures(final Callback<double[]> callback) {
        String query = (String) super.get(FROM);
        if (query != null) {
            //TODO CACHE TO AVOID PARSING EVERY TIME
            String[] split = query.split(FROM_SEPARATOR);
            Task[] tasks = new Task[split.length];
            for (int i = 0; i < split.length; i++) {
                Task t = graph().newTask();
                t.setWorld(world());
                t.setTime(time());
                t.parse(split[i].trim());
                tasks[i] = t;
            }
            //END TODO IN CACHE
            final double[] result = new double[tasks.length];
            final DeferCounter waiter = graph().newCounter(tasks.length);
            for (int i = 0; i < split.length; i++) {
                final int taskIndex = i;
                tasks[i].executeThenAsync(null, this, new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Object current = context.result();
                        if (current == null) {
                            result[taskIndex] = Constants.NULL_LONG;
                        } else {
                            if (current instanceof Double) {
                                result[taskIndex] = (Double) current;
                            } else if (current instanceof Object[]) {
                                Object[] currentArr = (Object[]) current;
                                if (currentArr.length == 1) {
                                    result[taskIndex] = parseDouble(currentArr[0].toString());
                                } else {
                                    throw new RuntimeException("Bad Extractor");
                                }
                            } else {
                                result[taskIndex] = parseDouble(current.toString());
                                throw new RuntimeException("Bad Extractor");
                            }
                        }
                        waiter.count();
                        context.next();
                    }
                });
            }
            waiter.then(new Job() {
                @Override
                public void run() {
                    callback.on(result);
                }
            });
        } else {
            callback.on(null);
        }
    }

    /**
     * @native ts
     * return parseFloat(payload);
     */
    public double parseDouble(String payload) {
        return Double.parseDouble(payload);
    }


}
