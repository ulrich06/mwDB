package org.mwdb;

public interface KStatNode extends KNode {

    void learn(double value);

    double mean();

    double min();

    double max();

}