package org.mwg.util.expression;

import org.mwg.Node;

public interface KMathExpressionEngine {
    
    void setVarResolver(KMathVariableResolver resolver);

    double eval(Node context);

}