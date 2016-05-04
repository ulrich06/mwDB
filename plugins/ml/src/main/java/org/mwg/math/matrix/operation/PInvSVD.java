package org.mwg.math.matrix.operation;

import org.mwg.math.matrix.KMatrix;
import org.mwg.math.matrix.KSVDDecompose;
import org.mwg.math.matrix.KTransposeType;

/**
 * Created by assaad on 24/03/16.
 */
public class PInvSVD {

    private KSVDDecompose _svd;
    private KMatrix pinv;
    private KMatrix S;
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

    public PInvSVD factor(KMatrix A, boolean invertInPlace){
        _svd = KMatrix.defaultEngine().decomposeSVD(A,invertInPlace);

        //We get UxSxVt
        KMatrix[] svd= new KMatrix[3];
        svd[0] = _svd.getU();
        svd[1] = _svd.getSMatrix();
        svd[2] = _svd.getVt();

        //  debug purpose
        //  KMatrix t1= Matrix.multiply(svd[0],svd[1]);
        //  KMatrix t2= Matrix.multiply(t1,svd[2]);


        KMatrix V=_svd.getVt();

        S= _svd.getSMatrix().clone();

        // copy pasted from EJML
        double maxSingular = 0;
        int dim=Math.min(S.columns(),S.rows());
        for( int i = 0; i < dim; i++ ) {
            if( S.get(i,i) > maxSingular )
                maxSingular = S.get(i,i);
        }
        double tau = Math.pow(2,-40)*Math.max(A.columns(),A.rows())*maxSingular;

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
        KMatrix temp = KMatrix.multiplyTransposeAlphaBeta(KTransposeType.TRANSPOSE,1,V, KTransposeType.TRANSPOSE,1,S);


        //V*W*Ut

        pinv= KMatrix.multiplyTransposeAlphaBeta(KTransposeType.NOTRANSPOSE,1,temp, KTransposeType.TRANSPOSE,1,_svd.getU());
        return this;
    }

    public KSVDDecompose getSvd(){
        return _svd;
    }

    public KMatrix getInvDeterminant(){
        return S;
    }

    public KMatrix getPInv(){
        return pinv;
    }
}
