package org.mwg.core.task;

import org.mwg.Node;
import org.mwg.core.task.math.CoreMathExpressionEngine;
import org.mwg.core.task.math.MathExpressionEngine;
import org.mwg.plugin.AbstractNode;
import org.mwg.plugin.AbstractTaskAction;
import org.mwg.task.TaskContext;
import org.mwg.task.TaskResult;

import java.util.HashMap;
import java.util.Map;

class ActionMath extends AbstractTaskAction {

    final private MathExpressionEngine _engine;
    final private String _expression;

    ActionMath(final String mathExpression) {
        super();
        this._expression = mathExpression;
        this._engine = CoreMathExpressionEngine.parse(mathExpression);
    }

    @Override
    public void eval(final TaskContext context) {
        final TaskResult previous = context.result();
        final TaskResult<Double> next = context.newResult();
        final int previousSize = previous.size();
        for (int i = 0; i < previousSize; i++) {
            final Object loop = previous.get(i);
            if (loop instanceof AbstractNode) {
                Map<String, Double> variables = new HashMap<String, Double>();
                variables.put("PI", Math.PI);
                variables.put("TRUE", 1.0);
                variables.put("FALSE", 0.0);
                next.add(_engine.eval((Node) loop, context, variables));
                ((AbstractNode) loop).free();
            }
        }
        //optimization to avoid iteration on previous result for free
        previous.clear();
        context.continueWith(next);
    }

    @Override
    public String toString() {
        return "math(\'" + _expression + "\')";
    }

}
