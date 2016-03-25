package org.mwdb;

import org.mwdb.math.matrix.KMatrix;

public interface KGaussianNode extends KNode {

    void learn(double[] value);

    double[] avg();

    double[][] getCovariance(double[] avg);

    KMatrix getCovarianceMatrix(double[] avg);

    double[] min();

    double[] max();

}