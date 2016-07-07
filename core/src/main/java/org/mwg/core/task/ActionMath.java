package org.mwg.core.task;

import org.mwg.Node;
import org.mwg.core.task.math.CoreMathExpressionEngine;
import org.mwg.core.task.math.MathExpressionEngine;
import org.mwg.plugin.AbstractNode;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ActionMath implements TaskAction {

    final MathExpressionEngine _engine;

    final String _expression;

    ActionMath(String mathExpression) {
        _expression = mathExpression;
        _engine = CoreMathExpressionEngine.parse(mathExpression);
    }

    @Override
    public void eval(TaskContext context) {
        Object previous = context.result();
        List<Double> result = new ArrayList<Double>();
        if (previous instanceof AbstractNode) {
            Map<String, Double> variables = new HashMap<String, Double>();
            variables.put("PI", Math.PI);
            variables.put("TRUE", 1.0);
            variables.put("FALSE", 0.0);
            result.add(_engine.eval((Node) previous, context, variables));
        } else if (previous instanceof Object[]) {
            arrayEval((Object[]) previous, result, context);
        }
        context.cleanObj(previous);
        context.setUnsafeResult(result.toArray(new Double[result.size()]));
    }

    void arrayEval(Object[] objs, List<Double> result, TaskContext context) {
        for (int i = 0; i < objs.length; i++) {
            if (objs[i] instanceof AbstractNode) {
                Map<String, Double> variables = new HashMap<String, Double>();
                variables.put("PI", Math.PI);
                variables.put("TRUE", 1.0);
                variables.put("FALSE", 0.0);
                result.add(_engine.eval((Node) objs[i], context, variables));
            } else if (objs[i] instanceof Object[]) {
                arrayEval((Object[]) objs[i], result, context);
            }
        }
    }

    @Override
    public String toString() {
        return "math(\'" + _expression + "\')";
    }

}
