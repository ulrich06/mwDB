package org.mwdb.math.matrix;

import org.mwdb.math.matrix.blas.KBlas;
import org.mwdb.math.matrix.blas.KBlasTransposeType;
import org.mwdb.math.matrix.blas.NetlibBlas;

/**
 * @ignore ts
 */
public class BlasMatrixEngine implements KMatrixEngine {

    private KBlas _blas;

    public BlasMatrixEngine() {
        _blas = new NetlibBlas();
    }

    public void setBlas(KBlas p_blas) {
        this._blas = p_blas;
    }


    @Override
    public KMatrix multiply(KMatrix matA, KMatrix matB) {
        KMatrix matC = new Matrix(null, matA.rows(), matB.columns(), KMatrixType.COLUMN_BASED);
        _blas.dgemm(KBlasTransposeType.NOTRANSPOSE, KBlasTransposeType.NOTRANSPOSE, matC.rows(), matC.columns(), matA.columns(), 1.0, matA.data(), 0, matA.rows(), matB.data(), 0, matB.rows(), 0.0, matC.data(), 0, matC.rows());
        return matC;
    }

    @Override
    public void solve() {

    }
}
