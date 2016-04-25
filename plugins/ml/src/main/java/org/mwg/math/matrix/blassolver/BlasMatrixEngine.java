package org.mwg.math.matrix.blassolver;

import org.mwg.math.matrix.KMatrix;
import org.mwg.math.matrix.KMatrixEngine;
import org.mwg.math.matrix.KSVDDecompose;
import org.mwg.math.matrix.blassolver.blas.KBlas;
import org.mwg.math.matrix.KTransposeType;
import org.mwg.math.matrix.blassolver.blas.NetlibBlas;
import org.mwg.math.matrix.operation.PInvSVD;

/**
 * @ignore ts
 */
public class BlasMatrixEngine implements KMatrixEngine {

    private KBlas _blas;

    public BlasMatrixEngine() {
        // _blas = new F2JBlas();
        _blas = new NetlibBlas();
    }

    public void setBlas(KBlas p_blas) {
        this._blas = p_blas;
    }

    public KBlas getBlas() {
        return _blas;
    }


    //C=alpha*A + beta * B (with possible transpose for A or B)
    @Override
    public KMatrix multiplyTransposeAlphaBeta(KTransposeType transA, double alpha, KMatrix matA, KTransposeType transB, double beta, KMatrix matB) {

        if (KMatrix.testDimensionsAB(transA, transB, matA, matB)) {
            int k = 0;
            int[] dimC = new int[2];
            if (transA.equals(KTransposeType.NOTRANSPOSE)) {
                k = matA.columns();
                if (transB.equals(KTransposeType.NOTRANSPOSE)) {
                    dimC[0] = matA.rows();
                    dimC[1] = matB.columns();
                } else {
                    dimC[0] = matA.rows();
                    dimC[1] = matB.rows();
                }
            } else {
                k = matA.rows();
                if (transB.equals(KTransposeType.NOTRANSPOSE)) {
                    dimC[0] = matA.columns();
                    dimC[1] = matB.columns();
                } else {
                    dimC[0] = matA.columns();
                    dimC[1] = matB.rows();
                }
            }


            KMatrix matC = new KMatrix(null, dimC[0], dimC[1]);
            _blas.dgemm(transA, transB, matC.rows(), matC.columns(), k, alpha, matA.data(), 0, matA.rows(), matB.data(), 0, matB.rows(), beta, matC.data(), 0, matC.rows());
            return matC;
        } else {
            throw new RuntimeException("Dimensions mismatch between A,B and C");
        }
    }

    @Override
    public KMatrix invert(KMatrix mat, boolean invertInPlace) {
        if (mat.rows() != mat.columns()) {
            return null;
        }

        if (invertInPlace) {
            LU alg = new LU(mat.rows(), mat.columns(), _blas);
            KMatrix result = new KMatrix(null, mat.rows(), mat.columns());
            LU dlu = new LU(mat.rows(), mat.columns(), _blas);
            if (dlu.invert(mat)) {
                return mat;
            } else {
                return null;
            }

        } else {
            LU alg = new LU(mat.rows(), mat.columns(), _blas);
            KMatrix result = new KMatrix(null, mat.rows(), mat.columns());
            KMatrix A_temp = new KMatrix(null, mat.rows(), mat.columns());
            System.arraycopy(mat.data(), 0, A_temp.data(), 0, mat.columns() * mat.rows());

            LU dlu = new LU(A_temp.rows(), A_temp.columns(), _blas);
            if (dlu.invert(A_temp)) {
                result.setData(A_temp.data());
                return result;
            } else {
                return null;
            }
        }
    }

    @Override
    public KMatrix pinv(KMatrix mat, boolean invertInPlace) {
        SVD svd = new SVD(mat.rows(), mat.columns(), _blas);
        PInvSVD pinvsvd = new PInvSVD();
        pinvsvd.factor(mat, invertInPlace);
        return pinvsvd.getPInv();

    }

    @Override
    public KMatrix solveQR(KMatrix matA, KMatrix matB, boolean workInPlace, KTransposeType transB) {
        if (workInPlace) {
            QR solver = QR.factorize(matA, true, _blas);
            KMatrix coef = new KMatrix(null, matA.columns(), matB.columns());
            if (transB != KTransposeType.NOTRANSPOSE) {
                matB = KMatrix.transpose(matB);
            }
            solver.solve(matB, coef);
            return coef;
        } else {
            QR solver = QR.factorize(matA.clone(), true, _blas);
            KMatrix coef = new KMatrix(null, matA.columns(), matB.columns());
            if (transB != KTransposeType.NOTRANSPOSE) {
                matB = KMatrix.transpose(matB);
            }
            solver.solve(matB.clone(), coef);
            return coef;
        }
    }

    @Override
    public KSVDDecompose decomposeSVD(KMatrix matA, boolean workInPlace) {
        SVD svd = new SVD(matA.rows(), matA.columns(), _blas);
        svd.factor(matA, workInPlace);
        return svd;
    }


    @Override
    public KMatrix solveLU(KMatrix matA, KMatrix matB, boolean workInPlace, KTransposeType transB) {
        if (!workInPlace) {
            KMatrix A_temp = new KMatrix(null, matA.rows(), matA.columns());
            System.arraycopy(matA.data(), 0, A_temp.data(), 0, matA.columns() * matA.rows());

            LU dlu = new LU(A_temp.rows(), A_temp.columns(), _blas);
            dlu.factor(A_temp, true);

            if (dlu.isSingular()) {
                return null;
            }
            KMatrix B_temp = new KMatrix(null, matB.rows(), matB.columns());
            System.arraycopy(matB.data(), 0, B_temp.data(), 0, matB.columns() * matB.rows());
            dlu.transSolve(B_temp, transB);
            return B_temp;
        } else {
            LU dlu = new LU(matA.rows(), matA.columns(), _blas);
            dlu.factor(matA, true);
            if (dlu.isSingular()) {
                return null;
            }
            dlu.transSolve(matB, transB);
            return matB;
        }
    }


}
