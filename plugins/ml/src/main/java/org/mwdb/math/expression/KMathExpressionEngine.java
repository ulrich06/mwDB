package org.mwdb.math.expression;

import org.mwdb.KNode;

public interface KMathExpressionEngine {

    KMathExpressionEngine parse(String p_expression);

    void setVarResolver(KMathVariableResolver resolver);

    double eval(KNode context);

}
