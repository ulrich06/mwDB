package org.mwg.core.task.math;

import org.mwg.Node;
import org.mwg.task.TaskContext;

import java.util.Map;

public interface MathExpressionEngine {

    double eval(Node context, TaskContext taskContext, Map<String, Double> variables);

}
