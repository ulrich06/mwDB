package org.mwdb.math.matrix._blas;

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


    public  int BLOCK_WIDTH = 60;
    public  int TRANSPOSE_SWITCH = 375;

    public  int leadingDimension(KMatrix matA){
        return Math.max(matA.columns(),matA.rows());
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


    public KMatrix multiply(KMatrix matA, KMatrix matB) {
        return multiplyTransposeAlphaBeta(KBlasTransposeType.NOTRANSPOSE,1,matA,KBlasTransposeType.NOTRANSPOSE,1,matB);
    }
    public  boolean testDimensionsAB(KBlasTransposeType transA, KBlasTransposeType transB, KMatrix matA, KMatrix matB) {

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
    public  void initMatrix(KMatrix matA, boolean random){
        Random rand = new Random();
        int k=0;
        for (int j = 0; j < matA.columns(); j++) {
            for (int i = 0; i < matA.rows(); i++) {
                if(random){
                    matA.set(i, j, rand.nextDouble()*100-50);
                }
                else {
                    matA.set(i, j, k);
                }
                k++;
            }
        }
    }
    public  KMatrix random(int rows, int columns){
        KMatrix res= new Matrix(null,rows,columns, _blas.matrixType());
        Random rand = new Random();
        for(int i=0;i<rows*columns;i++){
            res.setAtIndex(i,rand.nextDouble()*100-50);
        }
        return res;
    }
    public  void scale(double alpha, KMatrix matA) {
        if (alpha == 0) {
            matA.setAll(0);
            return;
        }
        for (int i = 0; i < matA.rows() * matA.columns(); i++) {
            matA.setAtIndex(i, alpha * matA.getAtIndex(i));
        }
    }
    public  KMatrix transpose(KMatrix matA) {
        KMatrix result=new Matrix(null, matA.columns(),matA.rows(),_blas.matrixType());
        if (matA.columns() == matA.rows()) {
            transposeSquare(matA, result);
        } else if (matA.columns() > TRANSPOSE_SWITCH && matA.rows() > TRANSPOSE_SWITCH) {
            transposeBlock(matA, result);
        } else {
            transposeStandard(matA, result);
        }
        return result;
    }
    private  void transposeSquare(KMatrix matA, KMatrix result) {
        int index = 1;
        int indexEnd = matA.columns();
        for (int i = 0; i < matA.rows(); i++) {
            int indexOther = (i + 1) * matA.columns() + i;
            int n = i * (matA.columns() + 1);
            result.setAtIndex(n, matA.getAtIndex(n));
            for (; index < indexEnd; index++) {
                result.setAtIndex(index, matA.getAtIndex(indexOther));
                result.setAtIndex(indexOther, matA.getAtIndex(index));
                indexOther += matA.columns();
            }
            index += i + 2;
            indexEnd += matA.columns();
        }
    }
    private  void transposeStandard(KMatrix matA, KMatrix result) {
        int index = 0;
        for (int i = 0; i < result.columns(); i++) {
            int index2 = i;
            int end = index + result.rows();
            while (index < end) {
                result.setAtIndex(index++, matA.getAtIndex(index2));
                index2 += matA.rows();
            }
        }
    }
    private  void transposeBlock(KMatrix matA, KMatrix result) {
        for (int j = 0; j < matA.columns(); j += BLOCK_WIDTH) {
            int blockWidth = Math.min(BLOCK_WIDTH, matA.columns() - j);
            int indexSrc = j * matA.rows();
            int indexDst = j;

            for (int i = 0; i < matA.rows(); i += BLOCK_WIDTH) {
                int blockHeight = Math.min(BLOCK_WIDTH, matA.rows() - i);
                int indexSrcEnd = indexSrc + blockHeight;

                for (; indexSrc < indexSrcEnd; indexSrc++) {
                    int colSrc = indexSrc;
                    int colDst = indexDst;
                    int end = colDst + blockWidth;
                    for (; colDst < end; colDst ++) {
                        result.setAtIndex(colDst, matA.getAtIndex(colSrc));
                        colSrc+=matA.rows();
                    }
                    indexDst += result.rows();
                }
            }
        }
    }
    public  KMatrix createIdentity(int width) {
        KMatrix ret = new Matrix(null, width, width,_blas.matrixType());
        for (int i = 0; i < width; i++) {
            ret.set(i, i, 1);
        }
        return ret;
    }
    public  double compareMatrix(KMatrix matA, KMatrix matB){
        double err=0;

        for (int i = 0; i < matA.rows(); i++) {
            for (int j = 0; j < matA.columns(); j++) {
                if(err< Math.abs(matA.get(i,j)-matB.get(i,j))) {
                    err = Math.abs(matA.get(i, j) - matB.get(i, j));
                    // System.out.println(i+" , "+ j+" , "+ err);
                }

            }
        }
        return err;
    }

}
