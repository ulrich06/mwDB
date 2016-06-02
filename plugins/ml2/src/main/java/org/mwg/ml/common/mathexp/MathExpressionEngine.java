package org.mwg.ml.common.mathexp;

import org.mwg.Node;

import java.util.Map;

public interface MathExpressionEngine {

    double eval(Node context, Map<String, Double> variables);

}
