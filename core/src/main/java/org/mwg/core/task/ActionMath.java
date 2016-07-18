package org.mwg.core.task;

import org.mwg.Node;
import org.mwg.core.task.math.CoreMathExpressionEngine;
import org.mwg.core.task.math.MathExpressionEngine;
import org.mwg.plugin.AbstractNode;
import org.mwg.task.TaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

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
        final TaskResult previous = context.result();
        TaskResult<Double> next = context.wrap(null);
        final int previousSize = previous.size();
        for (int i = 0; i < previousSize; i++) {
            final Object loop = previous.get(i);
            if (loop instanceof AbstractNode) {
                Map<String, Double> variables = new HashMap<String, Double>();
                variables.put("PI", Math.PI);
                variables.put("TRUE", 1.0);
                variables.put("FALSE", 0.0);
                next.add(_engine.eval((Node) loop, context, variables));
            }
        }
        context.continueWith(next);
    }

    @Override
    public String toString() {
        return "math(\'" + _expression + "\')";
    }

}
