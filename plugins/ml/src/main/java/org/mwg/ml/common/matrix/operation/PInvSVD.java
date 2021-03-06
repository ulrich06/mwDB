package org.mwg.ml.common.matrix.operation;

import org.mwg.ml.common.matrix.Matrix;
import org.mwg.ml.common.matrix.SVDDecompose;
import org.mwg.ml.common.matrix.TransposeType;

public class PInvSVD {

    private SVDDecompose _svd;
    private Matrix pinv;
    private Matrix S;
    private int rank;
    private double det;

    public int getRank(){
        return rank;
    }

    public double getDeterminant(){
        return det;
    }

    public PInvSVD()
    {
    }

    public PInvSVD factor(Matrix A, boolean invertInPlace){
        _svd = Matrix.defaultEngine().decomposeSVD(A,invertInPlace);

        //We get UxSxVt
        Matrix[] svd= new Matrix[3];
        svd[0] = _svd.getU();
        svd[1] = _svd.getSMatrix();
        svd[2] = _svd.getVt();

        //  debug purpose
        //  KMatrix t1= Matrix.multiply(svd[0],svd[1]);
        //  KMatrix t2= Matrix.multiply(t1,svd[2]);


        Matrix V=_svd.getVt();

        S= _svd.getSMatrix().clone();

        // copy pasted from EJML
        double maxSingular = 0;
        int dim=Math.min(S.columns(),S.rows());
        for( int i = 0; i < dim; i++ ) {
            if( S.get(i,i) > maxSingular )
                maxSingular = S.get(i,i);
        }
        double tau = Math.pow(2,-46)*Math.max(A.columns(),A.rows())*maxSingular;

        rank=0;
        det=1;
        // computer the pseudo inverse of A
        if( maxSingular != 0.0 ) {
            for (int i = 0; i < dim; i++) {
                double s = S.get(i,i);
                if (s < tau)
                    S.set(i,i,0);
                else {
                    S.set(i, i, 1/s);
                    det=det*s;
                    rank++;
                }
            }
        }

        // V*W
        Matrix temp = Matrix.multiplyTranspose(TransposeType.TRANSPOSE,V, TransposeType.TRANSPOSE,S);


        //V*W*Ut

        pinv= Matrix.multiplyTranspose(TransposeType.NOTRANSPOSE,temp, TransposeType.TRANSPOSE,_svd.getU());
        return this;
    }

    public SVDDecompose getSvd(){
        return _svd;
    }

    public Matrix getInvDeterminant(){
        return S;
    }

    public Matrix getPInv(){
        return pinv;
    }
}
