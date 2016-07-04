package org.mwg.ml;

import org.mwg.Callback;
import org.mwg.Node;

/**
 * Outlier detector it predicts true or false (anomaly or not, respectively).
 * Like a clusterer, it accepts only a list of values for learning. No label (unlike classfier).
 *
 */
public interface AnomalyDetectionNode extends Node {
    /**
     * Main training function to learn from the the expected output,
     * The input features are defined through features extractions.
     *
     * @param callback      Called when the learning is completed with the status of learning true/false
     */
    void learn(Callback<Boolean> callback);

    /**
     * Main infer function to classify the current example
     * The input features are defined through features extractions.
     *
     * @param callback Called when the classification is completed with the integer as the result of the classification
     */
    void classify(Callback<Boolean> callback);
}
