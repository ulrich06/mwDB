package org.mwg.ml.classifier.common;

import org.mwg.Node;

/**
 * Created by andre on 4/26/2016.
 */
public interface KSlidingWindowManagingNode  extends Node {

    /**
     * Public keys - node parameters, values, etc.
     */
    String VALUE_KEY = "value";
    String RESPONSE_INDEX_KEY = "responseIndex";
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
     * Adds new vector of values, recalculates if necessary.
     * @param value New value vector
     */
    void addValue(double[] value);

    /**
     * @return
     */
    int getCurrentBufferLength();

    /**
     * @return Errors in current value buffer (measure can be implementation-dependent).
     */
    double getBufferError();

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

}
