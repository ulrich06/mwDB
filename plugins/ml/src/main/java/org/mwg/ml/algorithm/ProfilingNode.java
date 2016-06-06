package org.mwg.ml.algorithm;

import org.mwg.Callback;

/**
 * Created by assaad on 04/05/16.
 */
public interface ProfilingNode {
    /**
     * Main training function to learn from the the expected output,
     * The input features are defined through features extractions.
     *
     * @param callback Called when the learning is completed with the status of learning true/false
     */
    void learn(Callback<Boolean> callback);

    /**
     * Main infer function to give a cluster ID,
     * The input features are defined through features extractions.
     *
     * @param callback Called when the infer is completed with the result of the predictions
     */
    void predict(Callback<double[]> callback);
}
