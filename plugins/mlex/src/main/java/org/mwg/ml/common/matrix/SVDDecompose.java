package org.mwg.ml.common.matrix;

/**
 * Created by assaad on 25/03/16.
 */
public interface SVDDecompose {
    SVDDecompose factor(Matrix A, boolean workInPlace);

    Matrix getU();

    Matrix getVt();

    double[] getS();

    Matrix getSMatrix();
}
