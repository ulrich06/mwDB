package org.mwdb;

import org.mwdb.math.matrix.KMatrix;

public interface KGaussianNode extends KMLNode<KGaussianNode> {

    void learnBatch(double[][] values);

    void learn(double[] value);

    int getNumberOfFeatures();

    double getProbability(double[] featArray, double[] err);

    double[] getProbabilityArray(double[][] featArray, double[] err);

    Integer getTotal();

    Double getWeight();

    double[] getAvg();

    double[][] getCovariance(double[] avg);

    KMatrix getCovarianceMatrix(double[] avg);

    double[] getMin();

    double[] getMax();

}