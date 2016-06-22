package org.mwg.ml.algorithm;

import org.mwg.Graph;
import org.mwg.plugin.Enforcer;

/**
 * Created by andre on 4/29/2016.
 */
public abstract class AbstractLRGradientDescentNode extends AbstractLinearRegressionNode{

    public static final String GD_ERROR_THRESH_KEY = "gdErrorThreshold";
    public static final String GD_ITERATION_THRESH_KEY = "gdIterationThreshold";

    public static final int DEFAULT_GD_ITERATIONS_COUNT = 10000;

    public static final double DEFAULT_LEARNING_RATE = 0.0001;

    /**
     * Attribute key - Learning rate
     */
    public static final String LEARNING_RATE_KEY = "_LearningRate";

    public AbstractLRGradientDescentNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    private static final Enforcer agdEnforcer = new Enforcer()
            .asInt(GD_ITERATION_THRESH_KEY)
            .asNonNegativeOrNanDouble(GD_ERROR_THRESH_KEY)
            .asNonNegativeDouble(LEARNING_RATE_KEY);

    @Override
    public void setProperty(String propertyName, byte propertyType, Object propertyValue) {
        agdEnforcer.check(propertyName, propertyType, propertyValue);
        super.setProperty(propertyName, propertyType, propertyValue);
    }
}
