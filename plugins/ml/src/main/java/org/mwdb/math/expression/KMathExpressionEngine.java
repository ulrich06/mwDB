package org.mwdb.math.expression;

import org.mwdb.KNode;

public interface KMathExpressionEngine {
    
    void setVarResolver(KMathVariableResolver resolver);

    double eval(KNode context);

}
