package org.mwdb.math.matrix.solver;

import org.mwdb.math.matrix.KMatrix;
import org.mwdb.math.matrix.Matrix;

/**
 * Created by assaad on 25/03/16.
 */
public class MultivariateNormalDistribution {
    double[] avg;
    KMatrix inv;
    KMatrix conv;
    PInvSVD pinvsvd;
    double det;

    public MultivariateNormalDistribution(double[] avg, KMatrix conv) {
        this.avg=avg;
        this.conv=conv;
        pinvsvd = new PInvSVD();
        pinvsvd.factor(conv,true);
        inv=pinvsvd.getPInv();
        det=pinvsvd.getDeterminant(); //todo test if we need to do 1/det

    }

    public double density(double[] features) {
        return Math.pow(2 * Math.PI, -0.5 * features.length) *
                Math.pow(det, -0.5) * getExponentTerm(features);
    }

    private double getExponentTerm(double[] features) {
        Matrix ft = new Matrix(features,1,features.length);
        Matrix ftt = new Matrix(features,features.length,1);

        for(int i=0;i<features.length;i++){
            ft.set(0,i,features[i]-avg[i]);
        }

        KMatrix res= Matrix.multiply(ft,inv);
        KMatrix res2= Matrix.multiply(res,ftt);

        return Math.exp(-0.5 *res2.get(0,0));
    }
}
