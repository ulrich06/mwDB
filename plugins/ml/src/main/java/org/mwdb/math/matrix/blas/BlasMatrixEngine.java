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


    public static int BLOCK_WIDTH = 60;
    public static int TRANSPOSE_SWITCH = 375;
    public static byte matrixType= KMatrixType.COLUMN_BASED;

    public static int leadingDimension(KMatrix matA){
        return Math.max(matA.columns(),matA.rows());
    }


    public static KMatrix multiply(KMatrix matA, KMatrix matB, KBlas blas) {
        Matrix matC = new Matrix(null,matA.rows(), matB.columns(),matrixType);
        blas.dgemm(KBlasTransposeType.NOTRANSPOSE, KBlasTransposeType.NOTRANSPOSE, matC.rows(), matC.columns(), matA.columns(), 1.0, matA.data(), 0, matA.rows(), matB.data(), 0, matB.rows(), 0.0, matC.data(), 0, matC.rows());
        return matC;
    }



    public static KMatrix multiplyTransposeAlpha(KBlasTransposeType transA, KBlasTransposeType transB,KMatrix matA, KMatrix matB, double alpha, KBlas blas) {
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


            Matrix matC = new Matrix(null,dimC[0], dimC[1],matrixType);
            blas.dgemm(transA, transB, matC.rows(), matC.columns(), k, alpha, matA.data(), 0, matA.rows(), matB.data(), 0, matB.rows(), 0, matC.data(), 0, matC.rows());
            return matC;
        }
        else {
            throw new RuntimeException("Dimensions mismatch between A,B and C");
        }
    }

    public static void multiplyAlphaBetaResult(double alpha, KMatrix matA, KMatrix matB, double beta, KMatrix matC, KBlas blas) {
        //  blas.dgemm(KBlasTransposeType.NOTRANSPOSE, KBlasTransposeType.NOTRANSPOSE, alpha, matA, matB, beta, matC);
        if (testDimensionsABC(KBlasTransposeType.NOTRANSPOSE, KBlasTransposeType.NOTRANSPOSE, matA, matB, matC)) {
            blas.dgemm(KBlasTransposeType.NOTRANSPOSE, KBlasTransposeType.NOTRANSPOSE, matC.rows(), matC.columns(), matA.columns(), alpha, matA.data(), 0, matA.rows(), matB.data(), 0, matB.rows(), beta, matC.data(), 0, matC.rows());
        } else {
            throw new RuntimeException("Dimensions mismatch between A,B and C");
        }
    }

    public static void multiplyTransposeAlphaBetaResult(KBlasTransposeType transA, KBlasTransposeType transB, double alpha, KMatrix matA, KMatrix matB, double beta, KMatrix matC, KBlas blas) {
        //  blas.dgemm(KBlasTransposeType.NOTRANSPOSE, KBlasTransposeType.NOTRANSPOSE, alpha, matA, matB, beta, matC);

        if (testDimensionsABC(transA, transB, matA, matB, matC)) {
            int k;
            if (transA.equals(KBlasTransposeType.NOTRANSPOSE)) {
                k = matA.columns();
            } else {
                k = matA.rows();
            }
            blas.dgemm(transA, transB, matC.rows(), matC.columns(), k, alpha, matA.data(), 0, matA.rows(), matB.data(), 0, matB.rows(), beta, matC.data(), 0, matC.rows());
        }
        else {
            throw new RuntimeException("Dimensions mismatch between A,B and C");
        }
    }

    public static boolean testDimensionsABC(KBlasTransposeType transA, KBlasTransposeType transB, KMatrix matA, KMatrix matB, KMatrix matC) {

        if(transA.equals(KBlasTransposeType.NOTRANSPOSE)) {
            if(transB.equals(KBlasTransposeType.NOTRANSPOSE)){
                return (matA.columns()==matB.rows() && matC.rows()==matA.rows() &&matC.columns()==matB.columns());
            }
            else{
                return (matA.columns()==matB.columns() && matC.rows()==matA.rows() &&matC.columns()==matB.rows());
            }
        }
        else {
            if(transB.equals(KBlasTransposeType.NOTRANSPOSE)){
                return (matA.rows()==matB.rows() && matC.rows()==matA.columns() &&matC.columns()==matB.columns());
            }
            else{
                return (matA.rows()==matB.columns() && matC.rows()==matA.columns() &&matC.columns()==matB.rows());
            }
        }
    }

    public static boolean testDimensionsAB(KBlasTransposeType transA, KBlasTransposeType transB, KMatrix matA, KMatrix matB) {

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


    public static void initMatrice(KMatrix matA, boolean random){
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

    public static KMatrix random(int rows, int columns){
        KMatrix res= new NativeArray2D(rows,columns);
        Random rand = new Random();
        for(int i=0;i<rows*columns;i++){
            res.setAtIndex(i,rand.nextDouble()*100-50);
        }
        return res;
    }

    public static KMatrix invert( KMatrix mat, KBlas blas ) {
        if(mat.rows()!=mat.columns()){
            return null;
        }


        LU alg = new LU(mat.rows(),mat.columns());
        KMatrix result = new NativeArray2D(mat.rows(),mat.columns());
        NativeArray2D A_temp=new NativeArray2D(mat.rows(),mat.columns());
        System.arraycopy(mat.data(), 0, A_temp.data(), 0, mat.columns() * mat.rows());

        LU dlu = new LU(A_temp.rows(),A_temp.columns());
        if (dlu.invert(A_temp, blas)){
            result.setData(A_temp.data());
            return result;
        }
        else {
            return null;
        }
    }

    public static boolean invertInPlace( KMatrix mat, KBlas blas ) {
        if(mat.rows()!=mat.columns()){
            return false;
        }

        LU alg = new LU(mat.rows(),mat.columns());
        KMatrix result = new NativeArray2D(mat.rows(),mat.columns());
        LU dlu = new LU(mat.rows(),mat.columns());
        return dlu.invert(mat,blas);
    }


    public static void scale(double alpha, KMatrix matA) {
        if (alpha == 0) {
            matA.setAll(0);
            return;
        }
        for (int i = 0; i < matA.rows() * matA.columns(); i++) {
            matA.setAtIndex(i, alpha * matA.getAtIndex(i));
        }
    }


    public static KMatrix transpose(KMatrix matA) {
        KMatrix result=new NativeArray2D(matA.columns(),matA.rows());
        if (matA.columns() == matA.rows()) {
            transposeSquare(matA, result);
        } else if (matA.columns() > TRANSPOSE_SWITCH && matA.rows() > TRANSPOSE_SWITCH) {
            transposeBlock(matA, result);
        } else {
            transposeStandard(matA, result);
        }
        return result;
    }

    private static void transposeSquare(KMatrix matA, KMatrix result) {
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

    private static void transposeStandard(KMatrix matA, KMatrix result) {
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

    private static void transposeBlock(KMatrix matA, KMatrix result) {
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

    public static KMatrix createIdentity(int width) {
        KMatrix ret = new NativeArray2D(width, width);
        ret.setAll(0);
        for (int i = 0; i < width; i++) {
            ret.set(i, i, 1);
        }
        return ret;
    }

    public static KMatrix solve(KMatrix matA, KMatrix matB, boolean workInPlace, KBlasTransposeType transB, KBlas blas){
        if(!workInPlace) {
            NativeArray2D A_temp = new NativeArray2D(matA.rows(), matA.columns());
            System.arraycopy(matA.data(), 0, A_temp.data(), 0, matA.columns() * matA.rows());

            LU dlu = new LU(A_temp.rows(), A_temp.columns());
            dlu.factor(A_temp, blas);

            if(dlu.isSingular()){
                return null;
            }
            NativeArray2D B_temp = new NativeArray2D(matB.rows(), matB.columns());
            System.arraycopy(matB.data(), 0, B_temp.data(), 0, matB.columns() * matB.rows());
            dlu.transSolve(B_temp,transB,blas);
            return B_temp;
        }
        else {
            LU dlu = new LU(matA.rows(), matA.columns());
            dlu.factor(matA, blas);
            if(dlu.isSingular()){
                return null;
            }
            dlu.transSolve(matB, transB, blas);
            return matB;
        }
    }

    public static double compareMatrix(KMatrix matA, KMatrix matB){
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
