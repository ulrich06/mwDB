package org.mwdb.math.matrix.jamasolver;


import org.mwdb.math.matrix.KMatrix;
import org.mwdb.math.matrix.KMatrixEngine;
import org.mwdb.math.matrix.KSVDDecompose;
import org.mwdb.math.matrix.KTransposeType;

public class JamaMatrixEngine implements KMatrixEngine {

    @Override
    public KMatrix multiplyTransposeAlphaBeta(KTransposeType transA, double alpha, KMatrix matA, KTransposeType transB, double beta, KMatrix matB) {
        if (KMatrix.testDimensionsAB(transA, transB, matA, matB)) {
            int[] dimC = new int[3];
            if (transA.equals(KTransposeType.NOTRANSPOSE)) {
                if (transB.equals(KTransposeType.NOTRANSPOSE)) {
                    dimC[0] = matA.rows();
                    dimC[1] = matB.columns();
                    dimC[2]= matA.columns();
                } else {
                    dimC[0] = matA.rows();
                    dimC[1] = matB.rows();
                    dimC[2]= matA.columns();
                }
            } else {
                if (transB.equals(KTransposeType.NOTRANSPOSE)) {
                    dimC[0] = matA.columns();
                    dimC[1] = matB.columns();
                    dimC[2]= matA.rows();
                } else {
                    dimC[0] = matA.columns();
                    dimC[1] = matB.rows();
                    dimC[2]= matA.rows();
                }
            }
            KMatrix matC = new KMatrix(null, dimC[0], dimC[1]);
            //perform mult here

            if(transA== KTransposeType.NOTRANSPOSE && transB== KTransposeType.NOTRANSPOSE){
                for(int i=0;i<dimC[0];i++){
                    for(int j=0;j<dimC[1];j++){
                        for(int k=0;k<dimC[2];k++){
                         matC.add(i,j,alpha*matA.get(i,k)*beta*matB.get(k,j))   ;
                        }
                    }
                }

            }
            else if(transA== KTransposeType.NOTRANSPOSE && transB== KTransposeType.TRANSPOSE){
                for(int i=0;i<dimC[0];i++){
                    for(int j=0;j<dimC[1];j++){
                        for(int k=0;k<dimC[2];k++){
                            matC.add(i,j,alpha*matA.get(i,k)*beta*matB.get(j,k))   ;
                        }
                    }
                }
            }
            else if(transA== KTransposeType.TRANSPOSE && transB== KTransposeType.NOTRANSPOSE){
                for(int i=0;i<dimC[0];i++){
                    for(int j=0;j<dimC[1];j++){
                        for(int k=0;k<dimC[2];k++){
                            matC.add(i,j,alpha*matA.get(k,i)*beta*matB.get(k,j))   ;
                        }
                    }
                }
            }
            else if(transA== KTransposeType.TRANSPOSE && transB== KTransposeType.TRANSPOSE){
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
    public KMatrix invert(KMatrix mat, boolean invertInPlace) {
        return solve(mat, mat.identity(mat.rows(),mat.rows()));
    }

    @Override
    public KMatrix pinv(KMatrix mat, boolean invertInPlace) {
        return solve(mat, mat.identity(mat.rows(),mat.rows()));
    }

    @Override
    public KMatrix solveLU(KMatrix matA, KMatrix matB, boolean workInPlace, KTransposeType transB) {
        KMatrix btem;
        if(transB== KTransposeType.TRANSPOSE){
            btem=KMatrix.transpose(matB);
        }
        else {
            btem=matB;
        }
        return (new LU(matA)).solve(btem);

    }

    @Override
    public KMatrix solveQR(KMatrix matA, KMatrix matB, boolean workInPlace, KTransposeType transB) {
        KMatrix btem;
        if(transB== KTransposeType.TRANSPOSE){
            btem=KMatrix.transpose(matB);
        }
        else {
            btem=matB;
        }
        return (new QR(matA)).solve(btem);
    }

    @Override
    public KSVDDecompose decomposeSVD(KMatrix matA, boolean workInPlace) {
        return null;
    }


    /** Solve A*X = B
     @param B    right hand side
     @return     solution if A is square, least squares solution otherwise
     */

    public static KMatrix solve (KMatrix A, KMatrix B) {
        return (A.rows() == A.columns() ? (new LU(A)).solve(B) :
                (new QR(A)).solve(B));
    }
}
