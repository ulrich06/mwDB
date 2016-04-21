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

}
