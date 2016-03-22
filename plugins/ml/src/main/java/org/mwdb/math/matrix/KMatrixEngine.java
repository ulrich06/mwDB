package org.mwdb.math.matrix;

import org.mwdb.math.matrix.blas.KBlasTransposeType;

public interface KMatrixEngine {
    public  KMatrix multiplyTransposeAlphaBeta(KBlasTransposeType transA, double alpha, KMatrix matA, KBlasTransposeType transB, double beta, KMatrix matB);

    KMatrix invert(KMatrix mat, boolean invertInPlace);

    KMatrix solve(KMatrix matA, KMatrix matB, boolean workInPlace, KBlasTransposeType transB);
}
