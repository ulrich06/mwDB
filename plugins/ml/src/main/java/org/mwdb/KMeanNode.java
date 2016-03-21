package org.mwdb;

public interface KMeanNode extends KNode {

    void learn(double value);

    double mean();

    double min();

    double max();

}