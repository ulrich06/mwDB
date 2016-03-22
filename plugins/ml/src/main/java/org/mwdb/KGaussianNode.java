package org.mwdb;

public interface KGaussianNode extends KNode {

    void learn(double[] value);

    double[] avg();

    double[][] cov(double[] avg);

    double[] min();

    double[] max();

}