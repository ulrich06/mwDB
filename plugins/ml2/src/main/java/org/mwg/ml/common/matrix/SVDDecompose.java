package org.mwg.ml.common.matrix;

public interface SVDDecompose {
    SVDDecompose factor(Matrix A, boolean workInPlace);

    Matrix getU();

    Matrix getVt();

    double[] getS();

    Matrix getSMatrix();
}
