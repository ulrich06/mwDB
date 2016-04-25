package org.mwg.util.matrix.operation;


import org.mwg.util.matrix.KMatrix;
import org.mwg.util.matrix.KTransposeType;

/**
 * Created by assaad on 16/12/15.
 */
public class PolynomialFit {
    private KMatrix coef;
    private int degree = 0;

    public PolynomialFit(int degree) {
        this.degree = degree;
    }

    public double[] getCoef() {
        return coef.data();
    }

    public void fit(double samplePoints[], double[] observations) {

        KMatrix y = new KMatrix(observations, observations.length, 1);

        KMatrix a = new KMatrix(null, y.rows(), degree + 1);

        // cset up the A matrix
        for (int i = 0; i < observations.length; i++) {
            double obs = 1;
            for (int j = 0; j < degree + 1; j++) {
                a.set(i, j, obs);
                obs *= samplePoints[i];
            }
        }
        // processValues the A matrix and see if it failed

        coef = KMatrix.defaultEngine().solveQR(a, y, true, KTransposeType.NOTRANSPOSE);


    }

    public static double extrapolate(double time, double[] weights) {
        double result = 0;
        double power = 1;
        for (int j = 0; j < weights.length; j++) {
            result += weights[j] * power;
            power = power * time;
        }
        return result;
    }

}