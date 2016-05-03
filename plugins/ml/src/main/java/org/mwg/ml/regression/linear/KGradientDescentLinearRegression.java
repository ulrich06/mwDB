package org.mwg.ml.regression.linear;

/**
 * Created by andre on 4/26/2016.
 */
public interface KGradientDescentLinearRegression extends KLinearRegression {

    double DEFAULT_LEARNING_RATE = 0.0001;

    /**
     * @return Learning rate. Default value: {@code DEFAULT_LEARNING_RATE}.
     */
    double getLearningRate();

    /**
     * Sets learning rate.
     * @param learningRate new learning rate
     */
    void setLearningRate(double learningRate);
}
