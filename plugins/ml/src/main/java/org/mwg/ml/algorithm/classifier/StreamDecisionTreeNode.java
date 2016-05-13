package org.mwg.ml.algorithm.classifier;

import org.mwg.Graph;
import org.mwg.ml.common.AbstractClassifierSlidingWindowManagingNode;

/**
 * Created by andre on 5/9/2016.
 */
public class StreamDecisionTreeNode extends AbstractClassifierSlidingWindowManagingNode{

    public StreamDecisionTreeNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    @Override
    protected int predictValue(double[] value) {
        return 0;
    }

    @Override
    protected double getLikelihoodForClass(double[] value, int classNum) {
        final int predictedClass = predictValue(value);
        //No real likelihood. Just yes or no.
        return (classNum==predictedClass)?1.0:0.0;
    }

    @Override
    protected void updateModelParameters(double[] value, int classNumber) {
        //TODO No tree? Initialize with the leaf.

        //TODO If there is a tree already:
          //TODO Go to the leaf
    }
}
