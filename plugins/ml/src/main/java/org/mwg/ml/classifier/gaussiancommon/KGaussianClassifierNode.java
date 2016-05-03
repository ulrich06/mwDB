package org.mwg.ml.classifier.gaussiancommon;

import org.mwg.ml.classifier.common.KSlidingWindowManagingNode;

public interface KGaussianClassifierNode extends KSlidingWindowManagingNode {

    //Methods below are for debugging mainly. Might be unmaiontained
    /**
     *
     * @return
     */
    int getBufferErrorCount();


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
