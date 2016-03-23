package org.mwdb.math.matrix.solver;


import org.mwdb.math.matrix.Matrix;
import org.mwdb.math.matrix.blas.KBlas;

/**
 * Created by assaad on 16/12/15.
 */
public class PolynomialFitBlas {
    private Matrix coef;
    private int degree = 0;

    private KBlas blas;

    public PolynomialFitBlas(int degree, KBlas blas) {
        this.degree = degree;
        this.blas = blas;
    }

    public double[] getCoef() {
        return coef.data();
    }

    public void fit(double samplePoints[], double[] observations) {

        Matrix y = new Matrix(observations, observations.length, 1, blas.matrixType());

        Matrix a = new Matrix(null, y.rows(), degree + 1, blas.matrixType());

        // cset up the A matrix
        for (int i = 0; i < observations.length; i++) {
            double obs = 1;
            for (int j = 0; j < degree + 1; j++) {
                a.set(i, j, obs);
                obs *= samplePoints[i];
            }
        }
        // processValues the A matrix and see if it failed
        QR solver = QR.factorize(a, true, blas);
        coef = new Matrix(null, degree + 1, 1, blas.matrixType());
        solver.solve(y, coef);
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