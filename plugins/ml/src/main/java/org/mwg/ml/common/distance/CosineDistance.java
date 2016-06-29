package org.mwg.ml.common.distance;

/**
 * Created by assaad on 29/06/16.
 */
public class CosineDistance implements Distance {
    @Override
    public double measure(double[] x, double[] y) {
        double sumTop = 0;
        double sumOne = 0;
        double sumTwo = 0;
        for (int i = 0; i < x.length; i++) {
            sumTop += x[i] * y[i];
            sumOne += x[i] * x[i];
            sumTwo += y[i] * y[i];
        }
        double cosSim = sumTop / (Math.sqrt(sumOne) * Math.sqrt(sumTwo));
        if (cosSim < 0)
            cosSim = 0;//This should not happen, but does because of rounding errorsl
        return 1- cosSim;
    }

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
