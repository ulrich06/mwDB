package org.mwg.maths.expression;

import org.mwg.Node;

import java.util.Map;

public interface KMathExpressionEngine {
    
    void setVarResolver(KMathVariableResolver resolver);

    double eval(Node context, Map<String,Object> result);

}
