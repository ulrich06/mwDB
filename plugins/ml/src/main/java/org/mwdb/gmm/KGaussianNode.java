package org.mwdb.gmm;

import org.mwdb.KNode;
import org.mwdb.math.matrix.KMatrix;

public interface KGaussianNode extends KNode {

    void configMixture(int levels,int maxPerLevel);

    int getLevel();

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

    long[] getSubGraph();

    boolean checkInside(double[] feature, int level);

}