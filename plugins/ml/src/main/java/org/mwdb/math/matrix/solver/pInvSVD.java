package org.mwdb.math.matrix.solver;

import org.mwdb.math.matrix.KMatrix;
import org.mwdb.math.matrix.Matrix;
import org.mwdb.math.matrix.blas.KBlas;
import org.mwdb.math.matrix.blas.KBlasTransposeType;

/**
 * Created by assaad on 24/03/16.
 */
public class PInvSVD {

    private SVD _svd;
    private KMatrix pinv;
    private double[] S;

    public PInvSVD(int m, int n, KBlas blas){
        _svd=new SVD(m,n,blas);
    }

    public PInvSVD factor(KMatrix A, boolean invertInPlace){
        _svd.factor(A,invertInPlace);

        //We get UxSxVt
        KMatrix[] svd= new KMatrix[3];
        svd[0] = _svd.getU();
        svd[1] = _svd.getSMatrix();
        svd[2] = _svd.getVt();

        //  debug purpose
        //  KMatrix t1= Matrix.multiply(svd[0],svd[1]);
        //  KMatrix t2= Matrix.multiply(t1,svd[2]);


        KMatrix V=Matrix.transpose(_svd.getVt());

        S= _svd.getS().clone();

        // copy pasted from EJML
        double maxSingular = 0;
        for( int i = 0; i < S.length; i++ ) {
            if( S[i] > maxSingular )
                maxSingular = S[i];
        }
        double tau = Math.pow(2,-52)*Math.max(A.columns(),A.rows())*maxSingular;

        // computer the pseudo inverse of A
        if( maxSingular != 0.0 ) {
            for (int i = 0; i < S.length; i++) {
                double s = S[i];
                if (s < tau)
                    S[i] = 0;
                else
                    S[i] = 1.0 / S[i];
            }
        }

        // V*W
        double Sj;
        for(int j=0;j<V.columns();j++){
            int index=j*V.rows();
            if(j<S.length){
                Sj=S[j];
            }
            else {
                Sj=0;
            }
            for(int i=0;i<V.rows();i++){
                V.setAtIndex(index,V.getAtIndex(index)*Sj);
                index++;
            }
        }

        //V*W*Ut

        pinv= Matrix.multiplyTransposeAlphaBeta(KBlasTransposeType.NOTRANSPOSE,1,V,KBlasTransposeType.TRANSPOSE,1,_svd.getU());
        return this;
    }

    public SVD getSvd(){
        return _svd;
    }

    public double[] getInvDeterminant(){
        return S;
    }

    public KMatrix getPInv(){
        return pinv;
    }
}
