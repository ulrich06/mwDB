package org.mwdb.gaussiannb;

import org.mwdb.KNode;

/**
 * Created by Andrey Boytsov on 4/14/2016.
 */
public interface KGaussianNaiveBayesianNode extends KNode {

    /**
     * Public keys - node parameters, values, etc.
     */
    String VALUE_KEY = "value";
    String CLASS_INDEX_KEY = "classIndex";
    String BUFFER_SIZE_KEY = "bufferSize";
    String INPUT_DIM_KEY = "inputDimensions";
    String LOW_ERROR_THRESH_KEY = "lowerErrorThreshold";
    String HIGH_ERROR_THRESH_KEY = "higherErrorThreshold";

    /**
     * @return Whether the node is in bootstrap (i.e.
     * re-learning) mode
     */
    boolean isInBootstrapMode();

    /**
     * @return Fraction of errors in current value buffer.
     */
    double getBufferErrorFraction();

    /**
     * Adds new vector of values, recalculates if necessary.
     * @param value New value vector
     */
    void addValue(double[] value);

    /**
     * Initialize should be called before using the node. Only once.
     *
     * @param inputDimension
     * @param classIndex
     * @param bufferSize
     * @param highErrorThreshold
     * @param lowErrorThreshold
     */
    void initialize(int inputDimension, int classIndex, int bufferSize, double highErrorThreshold, double lowErrorThreshold);

    //Methods below are for debugging mainly. Might be unmaiontained
    /**
     *
     * @return
     */
    int getBufferErrorCount();

    /**
     * @return
     */
    int getCurrentBufferLength();

    /**
     *
     * @return
     */
    int[] getPredictedBufferClasses();

    /**
     *
     * @return
     */
    int[] getRealBufferClasses();

    /**
     * @return All the distributions
     */
    String allDistributionsToString();
}
