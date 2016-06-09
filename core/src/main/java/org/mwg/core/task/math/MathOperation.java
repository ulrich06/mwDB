package org.mwg.core.task.math;

/**
 * Abstract definition of a supported operator. An operator is defined by
 * its name (pattern), precedence and if it is left- or right associative.
 */
class MathOperation implements MathToken {

    private String oper;
    private int precedence;
    private boolean leftAssoc;

    MathOperation(String oper, int precedence, boolean leftAssoc) {
        this.oper = oper;
        this.precedence = precedence;
        this.leftAssoc = leftAssoc;
    }

    public String getOper() {
        return oper;
    }

    int getPrecedence() {
        return precedence;
    }

    boolean isLeftAssoc() {
        return leftAssoc;
    }

    double eval(double v1, double v2) {
        if (oper.equals("+")) {
            return v1 + v2;
        } else if (oper.equals("-")) {
            return v1 - v2;
        } else if (oper.equals("*")) {
            return v1 * v2;
        } else if (oper.equals("/")) {
            return v1 / v2;
        } else if (oper.equals("%")) {
            return v1 % v2;
        } else if (oper.equals("^")) {
            return Math.pow(v1, v2);
        } else if (oper.equals("&&")) {
            boolean b1 = !(v1 == 0);
            boolean b2 = !(v2 == 0);
            return b1 && b2 ? 1 : 0;
        } else if (oper.equals("||")) {
            boolean b1 = !(v1 == 0);
            boolean b2 = !(v2 == 0);
            return b1 || b2 ? 1 : 0;
        } else if (oper.equals(">")) {
            return v1 > v2 ? 1 : 0;
        } else if (oper.equals(">=")) {
            return v1 >= v2 ? 1 : 0;
        } else if (oper.equals("<")) {
            return v1 < v2 ? 1 : 0;
        } else if (oper.equals("<=")) {
            return v1 <= v2 ? 1 : 0;
        } else if (oper.equals("==")) {
            return v1 == v2 ? 1 : 0;
        } else if (oper.equals("!=")) {
            return v1 != v2 ? 1 : 0;
        }
        return 0;
    }

    @Override
    public int type() {
        return 0;
    }

}