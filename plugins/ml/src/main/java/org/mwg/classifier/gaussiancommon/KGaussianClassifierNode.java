package org.mwg.classifier.gaussiancommon;

import org.mwg.Node;
import org.mwg.classifier.common.KSlidingWindowManagingNode;

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
