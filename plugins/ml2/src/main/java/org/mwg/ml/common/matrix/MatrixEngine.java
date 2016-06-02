package org.mwg.ml.common.matrix;

public interface MatrixEngine {
    Matrix multiplyTransposeAlphaBeta(TransposeType transA, double alpha, Matrix matA, TransposeType transB, double beta, Matrix matB);

    Matrix invert(Matrix mat, boolean invertInPlace);

    Matrix pinv(Matrix mat, boolean invertInPlace);

    //Solve AX=B -> return X as a result
    Matrix solveLU(Matrix matA, Matrix matB, boolean workInPlace, TransposeType transB);

    Matrix solveQR(Matrix matA, Matrix matB, boolean workInPlace, TransposeType transB);

    SVDDecompose decomposeSVD(Matrix matA, boolean workInPlace);
}
