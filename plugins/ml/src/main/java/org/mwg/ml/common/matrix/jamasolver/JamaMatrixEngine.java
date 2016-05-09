package org.mwg.ml.common.matrix.jamasolver;


import org.mwg.ml.common.matrix.Matrix;
import org.mwg.ml.common.matrix.MatrixEngine;
import org.mwg.ml.common.matrix.SVDDecompose;
import org.mwg.ml.common.matrix.TransposeType;

public class JamaMatrixEngine implements MatrixEngine {

    @Override
    public Matrix multiplyTransposeAlphaBeta(TransposeType transA, double alpha, Matrix matA, TransposeType transB, double beta, Matrix matB) {
        if (Matrix.testDimensionsAB(transA, transB, matA, matB)) {
            int[] dimC = new int[3];
            if (transA.equals(TransposeType.NOTRANSPOSE)) {
                if (transB.equals(TransposeType.NOTRANSPOSE)) {
                    dimC[0] = matA.rows();
                    dimC[1] = matB.columns();
                    dimC[2]= matA.columns();
                } else {
                    dimC[0] = matA.rows();
                    dimC[1] = matB.rows();
                    dimC[2]= matA.columns();
                }
            } else {
                if (transB.equals(TransposeType.NOTRANSPOSE)) {
                    dimC[0] = matA.columns();
                    dimC[1] = matB.columns();
                    dimC[2]= matA.rows();
                } else {
                    dimC[0] = matA.columns();
                    dimC[1] = matB.rows();
                    dimC[2]= matA.rows();
                }
            }
            Matrix matC = new Matrix(null, dimC[0], dimC[1]);
            //perform mult here

            if(transA== TransposeType.NOTRANSPOSE && transB== TransposeType.NOTRANSPOSE){
                for(int i=0;i<dimC[0];i++){
                    for(int j=0;j<dimC[1];j++){
                        for(int k=0;k<dimC[2];k++){
                         matC.add(i,j,alpha*matA.get(i,k)*beta*matB.get(k,j))   ;
                        }
                    }
                }

            }
            else if(transA== TransposeType.NOTRANSPOSE && transB== TransposeType.TRANSPOSE){
                for(int i=0;i<dimC[0];i++){
                    for(int j=0;j<dimC[1];j++){
                        for(int k=0;k<dimC[2];k++){
                            matC.add(i,j,alpha*matA.get(i,k)*beta*matB.get(j,k))   ;
                        }
                    }
                }
            }
            else if(transA== TransposeType.TRANSPOSE && transB== TransposeType.NOTRANSPOSE){
                for(int i=0;i<dimC[0];i++){
                    for(int j=0;j<dimC[1];j++){
                        for(int k=0;k<dimC[2];k++){
                            matC.add(i,j,alpha*matA.get(k,i)*beta*matB.get(k,j))   ;
                        }
                    }
                }
            }
            else if(transA== TransposeType.TRANSPOSE && transB== TransposeType.TRANSPOSE){
                for(int i=0;i<dimC[0];i++){
                    for(int j=0;j<dimC[1];j++){
                        for(int k=0;k<dimC[2];k++){
                            matC.add(i,j,alpha*matA.get(k,i)*beta*matB.get(j,k))   ;
                        }
                    }
                }
            }

            return matC;
        } else {
            throw new RuntimeException("Dimensions mismatch between A,B and C");
        }
    }

    @Override
    public Matrix invert(Matrix mat, boolean invertInPlace) {
        return solve(mat, mat.identity(mat.rows(),mat.rows()));
    }

    @Override
    public Matrix pinv(Matrix mat, boolean invertInPlace) {
        return solve(mat, mat.identity(mat.rows(),mat.rows()));
    }

    @Override
    public Matrix solveLU(Matrix matA, Matrix matB, boolean workInPlace, TransposeType transB) {
        Matrix btem;
        if(transB== TransposeType.TRANSPOSE){
            btem= Matrix.transpose(matB);
        }
        else {
            btem=matB;
        }
        return (new LU(matA)).solve(btem);

    }

    @Override
    public Matrix solveQR(Matrix matA, Matrix matB, boolean workInPlace, TransposeType transB) {
        Matrix btem;
        if(transB== TransposeType.TRANSPOSE){
            btem= Matrix.transpose(matB);
        }
        else {
            btem=matB;
        }
        return (new QR(matA)).solve(btem);
    }

    @Override
    public SVDDecompose decomposeSVD(Matrix matA, boolean workInPlace) {
        return null;
    }


    /** Solve A*X = B
     @param B    right hand side
     @return     solution if A is square, least squares solution otherwise
     */

    public static Matrix solve (Matrix A, Matrix B) {
        return (A.rows() == A.columns() ? (new LU(A)).solve(B) :
                (new QR(A)).solve(B));
    }
}
