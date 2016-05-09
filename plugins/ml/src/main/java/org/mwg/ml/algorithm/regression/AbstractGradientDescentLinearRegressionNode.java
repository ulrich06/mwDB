package org.mwg.ml.algorithm.regression;

import org.mwg.Graph;
import org.mwg.Type;

/**
 * Created by andre on 4/29/2016.
 */
public abstract class AbstractGradientDescentLinearRegressionNode extends AbstractLinearRegressionNode{

    public static final String GD_ERROR_THRESH_KEY = "gdErrorThreshold";
    public static final String GD_ITERATION_THRESH_KEY = "gdIterationThreshold";

    public static final int DEFAULT_GD_ITERATIONS_COUNT = 10000;

    public static final double DEFAULT_LEARNING_RATE = 0.0001;

    /**
     * Attribute key - Learning rate
     */
    protected static final String INTERNAL_VALUE_LEARNING_RATE_KEY = "_LearningRate";

    public AbstractGradientDescentLinearRegressionNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    public double getLearningRate(){
        return unphasedState().getFromKeyWithDefault(INTERNAL_VALUE_LEARNING_RATE_KEY, DEFAULT_LEARNING_RATE);
    }

    public void setLearningRate(double newLearningRate){
        illegalArgumentIfFalse(newLearningRate > 0, "Learning rate should be positive");
        unphasedState().setFromKey(INTERNAL_VALUE_LEARNING_RATE_KEY, Type.DOUBLE, newLearningRate);
    }

}
