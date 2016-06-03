package org.mwg.ml.common.matrix.blassolver;

import org.mwg.ml.common.matrix.Matrix;
import org.mwg.ml.common.matrix.MatrixEngine;
import org.mwg.ml.common.matrix.SVDDecompose;
import org.mwg.ml.common.matrix.TransposeType;
import org.mwg.ml.common.matrix.blassolver.blas.KBlas;
import org.mwg.ml.common.matrix.blassolver.blas.NetlibBlas;
import org.mwg.ml.common.matrix.operation.PInvSVD;

/**
 * @ignore ts
 */
public class BlasMatrixEngine implements MatrixEngine {

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
    public Matrix multiplyTransposeAlphaBeta(TransposeType transA, double alpha, Matrix matA, TransposeType transB, double beta, Matrix matB) {

        if (Matrix.testDimensionsAB(transA, transB, matA, matB)) {
            int k = 0;
            int[] dimC = new int[2];
            if (transA.equals(TransposeType.NOTRANSPOSE)) {
                k = matA.columns();
                if (transB.equals(TransposeType.NOTRANSPOSE)) {
                    dimC[0] = matA.rows();
                    dimC[1] = matB.columns();
                } else {
                    dimC[0] = matA.rows();
                    dimC[1] = matB.rows();
                }
            } else {
                k = matA.rows();
                if (transB.equals(TransposeType.NOTRANSPOSE)) {
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
    public Matrix invert(Matrix mat, boolean invertInPlace) {
        if (mat.rows() != mat.columns()) {
            return null;
        }

        if (invertInPlace) {
            LU alg = new LU(mat.rows(), mat.columns(), _blas);
            Matrix result = new Matrix(null, mat.rows(), mat.columns());
            LU dlu = new LU(mat.rows(), mat.columns(), _blas);
            if (dlu.invert(mat)) {
                return mat;
            } else {
                return null;
            }

        } else {
            LU alg = new LU(mat.rows(), mat.columns(), _blas);
            Matrix result = new Matrix(null, mat.rows(), mat.columns());
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
    public Matrix pinv(Matrix mat, boolean invertInPlace) {
        SVD svd = new SVD(mat.rows(), mat.columns(), _blas);
        PInvSVD pinvsvd = new PInvSVD();
        pinvsvd.factor(mat, invertInPlace);
        return pinvsvd.getPInv();

    }

    @Override
    public Matrix solveQR(Matrix matA, Matrix matB, boolean workInPlace, TransposeType transB) {
        QR solver = QR.factorize(matA, workInPlace, _blas);
        Matrix coef = new Matrix(null, matA.columns(), matB.columns());
        if (transB != TransposeType.NOTRANSPOSE) {
            matB = Matrix.transpose(matB);
        }
        solver.solve(matB, coef);
        return coef;
    }

    @Override
    public SVDDecompose decomposeSVD(Matrix matA, boolean workInPlace) {
        SVD svd = new SVD(matA.rows(), matA.columns(), _blas);
        svd.factor(matA, workInPlace);
        return svd;
    }


    @Override
    public Matrix solveLU(Matrix matA, Matrix matB, boolean workInPlace, TransposeType transB) {
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


}
