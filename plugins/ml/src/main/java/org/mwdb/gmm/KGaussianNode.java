package org.mwdb.gmm;

import org.mwdb.KMLNode;
import org.mwdb.math.matrix.KMatrix;

public interface KGaussianNode extends KMLNode<KGaussianNode> {

    void configMixture(int levels,int maxPerLevel);

    int getSubLevels();

    int getMaxPerLevel();

    void learnBatch(double[][] values);

    void learn(double[] value);

    int getNumberOfFeatures();

    double[] getSum();

    double[] getSumSquares();

    double getProbability(double[] featArray, double[] err, boolean normalizeOnAvg);

    double[] getProbabilityArray(double[][] featArray, double[] err, boolean normalizeOnAvg);

    Integer getTotal();

    Double getWeight();

    double[] getAvg();

    double[][] getCovariance(double[] avg);

    KMatrix getCovarianceMatrix(double[] avg);

    double[] getMin();

    double[] getMax();

}