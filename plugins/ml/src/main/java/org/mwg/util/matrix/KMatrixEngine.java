package org.mwg.util.matrix;

public interface KMatrixEngine {
    KMatrix multiplyTransposeAlphaBeta(KTransposeType transA, double alpha, KMatrix matA, KTransposeType transB, double beta, KMatrix matB);

    KMatrix invert(KMatrix mat, boolean invertInPlace);

    KMatrix pinv(KMatrix mat, boolean invertInPlace);

    //Solve AX=B -> return X as a result
    KMatrix solveLU(KMatrix matA, KMatrix matB, boolean workInPlace, KTransposeType transB);

    KMatrix solveQR(KMatrix matA, KMatrix matB, boolean workInPlace, KTransposeType transB);

    KSVDDecompose decomposeSVD(KMatrix matA, boolean workInPlace);
}
