package org.mwdb.math.matrix.operation;

import org.mwdb.math.matrix.KMatrix;

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

    public double density(double[] features, boolean normalizeOnAvg) {

        if(normalizeOnAvg){
            return getExponentTerm(features);
        }
        else{
            return Math.pow(2 * Math.PI, -0.5 * rank) *
                    Math.pow(det, -0.5) * getExponentTerm(features);
        }


    }

    private double getExponentTerm(double[] features) {
        double[] f= features.clone();

        for(int i=0;i<features.length;i++){
          f[i]=f[i]-avg[i];
        }

        KMatrix ft = new KMatrix(f,1,f.length);
        KMatrix ftt = new KMatrix(f,f.length,1);



        KMatrix res= KMatrix.multiply(ft,inv);
        KMatrix res2= KMatrix.multiply(res,ftt);

        double d=Math.exp(-0.5 *res2.get(0,0));

        return d;
    }
}
