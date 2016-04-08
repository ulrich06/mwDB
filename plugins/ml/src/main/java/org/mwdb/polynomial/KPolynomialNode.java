package org.mwdb.polynomial;

import org.mwdb.KNode;

public interface KPolynomialNode extends KNode {

    void setPrecision (double precision);
    double getPrecision();
    double[] getWeight();

    void set(double value);
    double get();

    int getDegree();

}
