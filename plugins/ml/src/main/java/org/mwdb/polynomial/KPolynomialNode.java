package org.mwdb.polynomial;

import org.mwdb.KNode;

public interface KPolynomialNode extends KNode {

    void learn(double value);

    double infer();

}
