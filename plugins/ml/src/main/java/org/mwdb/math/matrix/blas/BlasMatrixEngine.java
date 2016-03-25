package org.mwdb.math.matrix.blas;

import org.mwdb.math.matrix.KMatrix;
import org.mwdb.math.matrix.KMatrixEngine;
import org.mwdb.math.matrix.Matrix;
import org.mwdb.math.matrix.solver.LU;
import org.mwdb.math.matrix.solver.PInvSVD;
import org.mwdb.math.matrix.solver.QR;
import org.mwdb.math.matrix.solver.SVD;

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

    public KBlas getBlas() {
        return _blas;
    }


    //C=alpha*A + beta * B (with possible transpose for A or B)
    @Override
    public KMatrix multiplyTransposeAlphaBeta(KBlasTransposeType transA, double alpha, KMatrix matA, KBlasTransposeType transB, double beta, KMatrix matB) {

        if (testDimensionsAB(transA, transB, matA, matB)) {
            int k = 0;
            int[] dimC = new int[2];
            if (transA.equals(KBlasTransposeType.NOTRANSPOSE)) {
                k = matA.columns();
                if (transB.equals(KBlasTransposeType.NOTRANSPOSE)) {
                    dimC[0] = matA.rows();
                    dimC[1] = matB.columns();
                } else {
                    dimC[0] = matA.rows();
                    dimC[1] = matB.rows();
                }
            } else {
                k = matA.rows();
                if (transB.equals(KBlasTransposeType.NOTRANSPOSE)) {
                    dimC[0] = matA.columns();
                    dimC[1] = matB.columns();
                } else {
                    dimC[0] = matA.columns();
                    dimC[1] = matB.rows();
                }
            }


            Matrix matC = new Matrix(null, dimC[0], dimC[1]);
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
            KMatrix result = new Matrix(null, mat.rows(), mat.columns());
            LU dlu = new LU(mat.rows(), mat.columns(), _blas);
            if (dlu.invert(mat)) {
                return mat;
            } else {
                return null;
            }

        } else {
            LU alg = new LU(mat.rows(), mat.columns(), _blas);
            KMatrix result = new Matrix(null, mat.rows(), mat.columns());
            Matrix A_temp = new Matrix(null, mat.rows(), mat.columns());
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
        PInvSVD pinvsvd = new PInvSVD(mat.rows(), mat.columns(), _blas);
        pinvsvd.factor(mat, invertInPlace);
        return pinvsvd.getPInv();

    }

    @Override
    public KMatrix solveQR(KMatrix matA, KMatrix matB, boolean workInPlace, KBlasTransposeType transB) {
        if (workInPlace) {
            QR solver = QR.factorize(matA, true, _blas);
            KMatrix coef = new Matrix(null, matA.columns(), matB.columns());
            if (transB != KBlasTransposeType.NOTRANSPOSE) {
                matB = Matrix.transpose(matB);
            }
            solver.solve(matB, coef);
            return coef;
        } else {
            QR solver = QR.factorize(matA.clone(), true, _blas);
            KMatrix coef = new Matrix(null, matA.columns(), matB.columns());
            if (transB != KBlasTransposeType.NOTRANSPOSE) {
                matB = Matrix.transpose(matB);
            }
            solver.solve(matB.clone(), coef);
            return coef;
        }
    }

    @Override
    public KMatrix[] decomposeSVD(KMatrix matA, boolean workInPlace) {
        SVD svd = new SVD(matA.rows(), matA.columns(), _blas);
        svd.factor(matA,workInPlace);

        KMatrix[] result = new Matrix[3];
        result[0] = svd.getU();
        result[1] = svd.getSMatrix();
        result[2] = svd.getVt();
        return result;
    }


    @Override
    public KMatrix solveLU(KMatrix matA, KMatrix matB, boolean workInPlace, KBlasTransposeType transB) {
        if (!workInPlace) {
            Matrix A_temp = new Matrix(null, matA.rows(), matA.columns());
            System.arraycopy(matA.data(), 0, A_temp.data(), 0, matA.columns() * matA.rows());

            LU dlu = new LU(A_temp.rows(), A_temp.columns(), _blas);
            dlu.factor(A_temp, true);

            if (dlu.isSingular()) {
                return null;
            }
            Matrix B_temp = new Matrix(null, matB.rows(), matB.columns());
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


    private static boolean testDimensionsAB(KBlasTransposeType transA, KBlasTransposeType transB, KMatrix matA, KMatrix matB) {
        if (transA.equals(KBlasTransposeType.NOTRANSPOSE)) {
            if (transB.equals(KBlasTransposeType.NOTRANSPOSE)) {
                return (matA.columns() == matB.rows());
            } else {
                return (matA.columns() == matB.columns());
            }
        } else {
            if (transB.equals(KBlasTransposeType.NOTRANSPOSE)) {
                return (matA.rows() == matB.rows());
            } else {
                return (matA.rows() == matB.columns());
            }
        }
    }

}
