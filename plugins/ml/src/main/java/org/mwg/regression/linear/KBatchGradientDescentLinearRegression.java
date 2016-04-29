package org.mwg.regression.linear;

/**
 * Created by andre on 4/26/2016.
 */
public interface KBatchGradientDescentLinearRegression extends KGradientDescentLinearRegression {

    String GD_ERROR_THRESH_KEY = "gdErrorThreshold";
    String GD_ITERATION_THRESH_KEY = "gdIterationThreshold";

    int DEFAULT_GD_ITERATIONS_COUNT = 10000;

    /**
     * @return When next iteration of GD improves error for less than specified amount, algorithm stops.
     */
    double getIterationErrorThreshold();

    /**
     * Sets iteration threshold - when next iteration of GD improves error for less than specified amount, algorithm stops
     * (NaN or non-positive value means that the criterion is not used)
     * @param errorThreshold new learning rate
     */
    void setIterationErrorThreshold(double errorThreshold);

    /**
     * Sets iteration threshold - when next iteration of GD improves error for less than specified amount, algorithm stops.
     */
    void removeIterationErrorThreshold();

    /**
     * @return Maximum number of iterations for each gradient descent (0 or negative - not used, which is not recommended)
     */
    int getIterationCountThreshold();

    /**
     * Sets the maximum number .
     * @param iterationCountThreshold new learning rate
     */
    void setIterationCountThreshold(int iterationCountThreshold);

    /**
     * Sets iteration threshold - when next iteration of GD improves error for less than specified amount, algorithm stops.
     */
    void removeIterationCountThreshold();
}
