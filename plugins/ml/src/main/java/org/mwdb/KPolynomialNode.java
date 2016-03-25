package org.mwdb;

public interface KPolynomialNode extends KNode {

    void learn(double value);

    double infer();

}
