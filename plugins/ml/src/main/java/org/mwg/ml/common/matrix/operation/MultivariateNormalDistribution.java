package org.mwg.ml.common.matrix.operation;

import org.mwg.ml.common.matrix.Matrix;

/**
 * Created by assaad on 25/03/16.
 */
public class MultivariateNormalDistribution {
    double[] means;
    Matrix inv;
    Matrix covariance;
    PInvSVD pinvsvd;
    int rank;
    double det;

    public MultivariateNormalDistribution(double[] means, Matrix cov) {
        this.means = means;
        this.covariance =cov;
        pinvsvd = new PInvSVD();
        pinvsvd.factor(covariance,false);
        inv=pinvsvd.getPInv();
        det=pinvsvd.getDeterminant(); //todo test if we need to do 1/det
        rank=pinvsvd.getRank();

    }

    public static Matrix getCovariance(double[] sum, double[] sumsquares, int total) {
        if (total < 2) {
            return null;
        }

        int features = sum.length;
        double[] avg = new double[features];

        for (int i = 0; i < features; i++) {
            avg[i] = sum[i] / total;
        }

        double[] covariances = new double[features * features];

        double correction = total;
        correction = correction / (total - 1);

        int count = 0;
        for (int i = 0; i < features; i++) {
            for (int j = i; j < features; j++) {
                covariances[i * features + j] = (sumsquares[count] / total - avg[i] * avg[j]) * correction;
                covariances[j * features + i] = covariances[i * features + j];
                count++;
            }
        }
        Matrix cov = new Matrix(covariances, features, features);
        return cov;
    }


    //Sum is a n-vector sum of features
    //Sum squares is a n(n+1)/2 vector of sumsquares of features, in upper-triangle row shapes
    //Example:   for (int i = 0; i < features; i++) {    for (int j = i; j < features; j++) {  sumsquares[count] + = x[i] * x[j];  count++; } }
    //Total is the number of observations
    public static MultivariateNormalDistribution getDistribution(double[] sum, double[] sumsquares, int total) {
        if (total < 2) {
            return null;
        }

        int features = sum.length;
        double[] avg= new double[features];

        for (int i = 0; i < features; i++) {
            avg[i] = sum[i] / total;
        }

        double[] covariances = new double[features * features];

        double correction = total;
        correction = correction / (total - 1);

        int count = 0;
        for (int i = 0; i < features; i++) {
            for (int j = i; j < features; j++) {
                covariances[i * features + j] = (sumsquares[count] / total - avg[i] * avg[j]) * correction;
                covariances[j * features + i] = covariances[i * features + j];
                count++;
            }
        }
        Matrix cov= new Matrix(covariances, features, features);
        return new MultivariateNormalDistribution(avg,cov);
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
          f[i]=f[i]- means[i];
        }

        Matrix ft = new Matrix(f,1,f.length);
        Matrix ftt = new Matrix(f,f.length,1);



        Matrix res= Matrix.multiply(ft,inv);
        Matrix res2= Matrix.multiply(res,ftt);

        double d=Math.exp(-0.5 *res2.get(0,0));

        return d;
    }
}
