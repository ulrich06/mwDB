package org.mwg.ml.common.distance;

/**
 * Created by assaad on 29/06/16.
 */
public class GaussianDistance implements Distance {
    double[] err;
    public GaussianDistance(double[] covariance){
        this.err=covariance;
    }

    @Override
    public double measure(double[] x, double[] y) {
        double max = 0;
        double temp;
        for (int i = 0; i < x.length; i++) {
            temp = (x[i] - y[i]) * (x[i] - y[i]) / err[i];
            if (temp > max) {
                max = temp;
            }
        }
        return Math.sqrt(max);
    }

    @Override
    public boolean compare(double x, double y) {
        return x < y;
    }


    @Override
    public double getMinValue() {
        return 0;
    }

    @Override
    public double getMaxValue() {
        return Double.POSITIVE_INFINITY;
    }
}
