package org.mwg.ml;

/**
 * Created by assaad on 04/05/16.
 */
public interface RegressionNode {
    void learn(double output);
    double extrapolate();
}
