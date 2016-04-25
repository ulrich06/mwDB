package org.mwg.regression;

import org.mwg.Node;

public interface KPolynomialNode extends Node {

    void setPrecision (double precision);
    double getPrecision();
    double[] getWeight();

    void set(double value);
    double get();

    int getDegree();

}
