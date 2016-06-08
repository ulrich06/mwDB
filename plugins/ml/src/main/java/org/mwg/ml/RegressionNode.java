package org.mwg.ml;

import org.mwg.Callback;

public interface RegressionNode {
    /**
     * Main training function to learn from the the expected output,
     * The input features are defined through features extractions.
     *
     * @param output   Expected output of the regression in a supervised manner
     * @param callback Called when the learning is completed with the status of learning true/false
     */
    void learn(double output, Callback<Boolean> callback);

    /**
     * Main infer function to give a cluster ID,
     * The input features are defined through features extractions.
     *
     * @param callback Called when the infer is completed with the result of the extrapolation
     */
    void extrapolate(Callback<Double> callback);
}
