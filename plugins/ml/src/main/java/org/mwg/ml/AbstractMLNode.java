package org.mwg.ml;

import org.mwg.Callback;
import org.mwg.DeferCounter;
import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.ml.common.mathexp.impl.MathExpressionEngine;
import org.mwg.plugin.AbstractNode;
import org.mwg.task.Action;
import org.mwg.task.Task;
import org.mwg.task.TaskContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by assaad on 04/05/16.
 */
public abstract class AbstractMLNode extends AbstractNode {
    public static String FROM_SEPARATOR = ";";
    public static String FROM = "FROM";

    public AbstractMLNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    @Override
    public void setProperty(String propertyName, byte propertyType, Object propertyValue) {
        super.setProperty(propertyName, propertyType, propertyValue);
    }

    @Override
    public Object get(String propertyName) {
        if (propertyName != null && propertyName.length() > 0 && propertyName.charAt(0) == '$') {
            Object expressionObj = super.get(propertyName.substring(1));
            //ToDo this is dangerous for infinite loops or circular dependency, to fix
            org.mwg.ml.common.mathexp.MathExpressionEngine localEngine = MathExpressionEngine.parse(expressionObj.toString());

            Map<String, Double> variables = new HashMap<String, Double>();
            variables.put("PI", Math.PI);
            variables.put("TRUE", 1.0);
            variables.put("FALSE", 0.0);

            return localEngine.eval(this, variables);
        } else {
            return super.get(propertyName);
        }
    }


    public void extractFeatures(Callback<double[]> callback) {

        String query = (String) super.get(FROM);
        if (query != null) {
            //TODO CACHE TO AVOID PARSING EVERY TIME
            String[] split = query.split(FROM_SEPARATOR);
            Task[] tasks = new Task[split.length];
            for (int i = 0; i < split.length; i++) {
                Task t = graph().newTask();
                t.parse(split[i]);
                tasks[i] = t;
            }
            //END TODO IN CACHE
            final double[] result = new double[tasks.length];
            DeferCounter waiter = graph().counter(tasks.length);
            for (int i = 0; i < split.length; i++) {
                final int taskIndex = i;
                tasks[i].executeThenAsync(null, this, new Action() {
                    @Override
                    public void eval(TaskContext context) {
                        Object current = context.getPreviousResult();
                        if (current instanceof Double) {
                            result[taskIndex] = (double) current;
                        } else {
                            throw new RuntimeException("Bad Extractor");
                        }
                        waiter.count();
                        context.next();
                    }
                });
            }
            waiter.then(new Callback() {
                @Override
                public void on(Object ignored) {
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
