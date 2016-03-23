package org.mwdb.math.expression;

public interface KMathExpressionEngine {

    KMathExpressionEngine parse(String p_expression);

    void setVarResolver(KMathVariableResolver resolver);

    double eval(KObject context);

}
