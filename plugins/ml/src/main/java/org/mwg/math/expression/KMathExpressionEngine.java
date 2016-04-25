package org.mwg.math.expression;

import org.mwg.Node;

public interface KMathExpressionEngine {
    
    void setVarResolver(KMathVariableResolver resolver);

    double eval(Node context);

}
