package org.mwg.polynomial;

import org.mwg.Node;

public interface KPolynomialNode extends Node {

    void setPrecision (double precision);
    double getPrecision();
    double[] getWeight();

    void set(double value);
    double get();

    int getDegree();

}
