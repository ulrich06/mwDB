package org.mwg.ml.common.matrix;

import org.mwg.ml.common.matrix.blassolver.BlasMatrixEngine;
import org.mwg.ml.common.matrix.jamasolver.JamaMatrixEngine;

/**
 * Created by assaad on 08/06/16.
 */
public class HybridMatrixEngine implements MatrixEngine {

    private MatrixEngine blas;
    private MatrixEngine jama;

    public HybridMatrixEngine(){
        blas=new BlasMatrixEngine();
        jama=new JamaMatrixEngine();
    }


    @Override
    public Matrix multiplyTransposeAlphaBeta(TransposeType transA, double alpha, Matrix matA, TransposeType transB, double beta, Matrix matB) {
        if(matA.leadingDimension()<9&&matB.leadingDimension()<9){
            return jama.multiplyTransposeAlphaBeta(transA,alpha,matA,transB,beta,matB);
        }
        else {
            return blas.multiplyTransposeAlphaBeta(transA,alpha,matA,transB,beta,matB);
        }
    }

    @Override
    public Matrix invert(Matrix mat, boolean invertInPlace) {
        if(mat.rows()<10){
            return jama.invert(mat,invertInPlace);
        }
        else {
            return blas.invert(mat,invertInPlace);
        }
    }

    @Override
    public Matrix pinv(Matrix mat, boolean invertInPlace) {
        if(mat.rows()<8){
            return jama.pinv(mat,invertInPlace);
        }
        else {
            return blas.pinv(mat,invertInPlace);
        }
    }

    @Override
    public Matrix solveLU(Matrix matA, Matrix matB, boolean workInPlace, TransposeType transB) {
        if(matA.leadingDimension()<10&&matB.leadingDimension()<10){
            return jama.solveLU(matA, matB, workInPlace, transB);
        }
        else {
            return blas.solveLU(matA, matB, workInPlace, transB);
        }
    }

    @Override
    public Matrix solveQR(Matrix matA, Matrix matB, boolean workInPlace, TransposeType transB) {
        if(matA.leadingDimension()<17 &&matB.leadingDimension()<17){
            return jama.solveQR(matA, matB, workInPlace, transB);
        }
        else {
            return blas.solveQR(matA, matB, workInPlace, transB);
        }
    }

    @Override
    public SVDDecompose decomposeSVD(Matrix matA, boolean workInPlace) {
        if(matA.leadingDimension()<35){
            return jama.decomposeSVD(matA,workInPlace);
        }
        else {
            return blas.decomposeSVD(matA,workInPlace);
        }
    }
}
