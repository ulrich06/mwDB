package org.mwdb.math.matrix.blas;

import org.mwdb.math.matrix.KMatrix;
import org.mwdb.math.matrix.KMatrixEngine;
import org.mwdb.math.matrix.KMatrixType;
import org.mwdb.math.matrix.Matrix;
import org.mwdb.math.matrix.blas.KBlas;
import org.mwdb.math.matrix.blas.KBlasTransposeType;
import org.mwdb.math.matrix.blas.NetlibBlas;

import java.util.Random;

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



    //C=alpha*A + beta * B (with possible transpose for A or B)
    @Override
    public  KMatrix multiplyTransposeAlphaBeta(KBlasTransposeType transA, double alpha, KMatrix matA,  KBlasTransposeType transB, double beta, KMatrix matB) {

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


            Matrix matC = new Matrix(null,dimC[0], dimC[1], _blas.matrixType());
            _blas.dgemm(transA, transB, matC.rows(), matC.columns(), k, alpha, matA.data(), 0, matA.rows(), matB.data(), 0, matB.rows(), beta, matC.data(), 0, matC.rows());
            return matC;
        }
        else {
            throw new RuntimeException("Dimensions mismatch between A,B and C");
        }
    }

    @Override
    public  KMatrix invert(KMatrix mat, boolean invertInPlace) {
        if(mat.rows()!=mat.columns()){
            return null;
        }

        if(invertInPlace){
            LU alg = new LU(mat.rows(),mat.columns());
            KMatrix result = new Matrix(null, mat.rows(),mat.columns(),_blas.matrixType());
            LU dlu = new LU(mat.rows(),mat.columns());
            return dlu.invert(mat,_blas);
        }
        else {
            LU alg = new LU(mat.rows(), mat.columns());
            KMatrix result = new Matrix(null, mat.rows(), mat.columns(), _blas.matrixType());
            Matrix A_temp = new Matrix(null, mat.rows(), mat.columns(), _blas.matrixType());
            System.arraycopy(mat.data(), 0, A_temp.data(), 0, mat.columns() * mat.rows());

            LU dlu = new LU(A_temp.rows(), A_temp.columns());
            if (dlu.invert(A_temp, _blas)) {
                result.setData(A_temp.data());
                return result;
            } else {
                return null;
            }
        }
    }

    @Override
    public  KMatrix solve(KMatrix matA, KMatrix matB, boolean workInPlace, KBlasTransposeType transB){
        if(!workInPlace) {
            Matrix A_temp = new Matrix(null,matA.rows(), matA.columns(),_blas.matrixType());
            System.arraycopy(matA.data(), 0, A_temp.data(), 0, matA.columns() * matA.rows());

            LU dlu = new LU(A_temp.rows(), A_temp.columns());
            dlu.factor(A_temp, _blas);

            if(dlu.isSingular()){
                return null;
            }
            Matrix B_temp = new Matrix(null, matB.rows(), matB.columns(),_blas.matrixType());
            System.arraycopy(matB.data(), 0, B_temp.data(), 0, matB.columns() * matB.rows());
            dlu.transSolve(B_temp,transB,_blas);
            return B_temp;
        }
        else {
            LU dlu = new LU(matA.rows(), matA.columns());
            dlu.factor(matA, _blas);
            if(dlu.isSingular()){
                return null;
            }
            dlu.transSolve(matB, transB, _blas);
            return matB;
        }
    }



    private static boolean testDimensionsAB(KBlasTransposeType transA, KBlasTransposeType transB, KMatrix matA, KMatrix matB) {
        if(transA.equals(KBlasTransposeType.NOTRANSPOSE)) {
            if(transB.equals(KBlasTransposeType.NOTRANSPOSE)){
                return (matA.columns()==matB.rows());
            }
            else{
                return (matA.columns()==matB.columns());
            }
        }
        else {
            if(transB.equals(KBlasTransposeType.NOTRANSPOSE)){
                return (matA.rows()==matB.rows());
            }
            else{
                return (matA.rows()==matB.columns());
            }
        }
    }

}
