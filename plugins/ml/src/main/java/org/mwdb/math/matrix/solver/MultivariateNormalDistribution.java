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
    int rank;
    double det;

    public MultivariateNormalDistribution(double[] avg, KMatrix conv) {
        this.avg=avg;
        this.conv=conv;
        pinvsvd = new PInvSVD();
        pinvsvd.factor(conv,true);
        inv=pinvsvd.getPInv();
        det=pinvsvd.getDeterminant(); //todo test if we need to do 1/det
        rank=pinvsvd.getRank();

    }

    public double density(double[] features) {
        double d= getExponentTerm(features);

        double p=Math.pow(2 * Math.PI, -0.5 * rank) *
                Math.pow(det, -0.5);
        System.out.println("coef: "+p);


        return p*d;
    }

    private double getExponentTerm(double[] features) {
        double[] f= features.clone();

        for(int i=0;i<features.length;i++){
          f[i]=f[i]-avg[i];
        }

        Matrix ft = new Matrix(f,1,f.length);
        Matrix ftt = new Matrix(f,f.length,1);



        KMatrix res= Matrix.multiply(ft,inv);
        KMatrix res2= Matrix.multiply(res,ftt);

        double d=Math.exp(-0.5 *res2.get(0,0));
        System.out.println("exponent "+ d);

        return d;
    }
}
