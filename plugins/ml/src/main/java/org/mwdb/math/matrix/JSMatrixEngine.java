package org.mwdb.math.matrix;


import org.mwdb.math.matrix.blas.KBlasTransposeType;

public class JSMatrixEngine implements KMatrixEngine {

    @Override
    public KMatrix multiplyTransposeAlphaBeta(KBlasTransposeType transA, double alpha, KMatrix matA, KBlasTransposeType transB, double beta, KMatrix matB) {
        return null;
    }

    @Override
    public KMatrix invert(KMatrix mat, boolean invertInPlace) {
        return null;
    }

    @Override
    public KMatrix pinv(KMatrix mat, boolean invertInPlace) {
        return null;
    }

    @Override
    public KMatrix solveLU(KMatrix matA, KMatrix matB, boolean workInPlace, KBlasTransposeType transB) {
        return null;
    }

    @Override
    public KMatrix solveQR(KMatrix matA, KMatrix matB, boolean workInPlace, KBlasTransposeType transB) {
        return null;
    }

    @Override
    public KSVDDecompose decomposeSVD(KMatrix matA, boolean workInPlace) {
        return null;
    }


}
