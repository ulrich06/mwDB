package org.mwg.ml.common.distance;

/**
 * Created by assaad on 29/06/16.
 */
public class EuclideanDistance implements Distance {
    @Override
    public double measure(double[] x, double[] y) {
        double value=0;
        for(int i=0;i<x.length;i++){
            value=value+(x[i]-y[i])*(x[i]-y[i]);
        }
        return Math.sqrt(value);
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
        return Double.MAX_VALUE;
    }
}
