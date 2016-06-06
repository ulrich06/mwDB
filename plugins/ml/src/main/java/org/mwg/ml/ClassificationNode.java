package org.mwg.ml;

import org.mwg.Callback;
import org.mwg.Node;

public interface ClassificationNode extends Node {
    /**
     * Main training function to learn from the the expected output,
     * The input features are defined through features extractions.
     *
     * @param expectedClass The output supervised class of the classification
     * @param callback      Called when the learning is completed with the status of learning true/false
     */
    void learn(int expectedClass, Callback<Boolean> callback);

    /**
     * Main infer function to classify the current example
     * The input features are defined through features extractions.
     *
     * @param callback Called when the classification is completed with the integer as the result of the classification
     */
    void classify(Callback<Integer> callback);
}
