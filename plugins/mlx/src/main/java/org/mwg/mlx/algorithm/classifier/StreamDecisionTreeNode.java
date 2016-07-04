package org.mwg.mlx.algorithm.classifier;

import org.mwg.Graph;
import org.mwg.mlx.algorithm.AbstractClassifierSlidingWindowManagingNode;
import org.mwg.plugin.NodeState;

/**
 * Created by andre on 5/9/2016.
 */
public class StreamDecisionTreeNode extends AbstractClassifierSlidingWindowManagingNode{

    //TODO We BADLY need to keep the tree between received data points.

    public StreamDecisionTreeNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    @Override
    protected int predictValue(NodeState state, double[] value) {
        return 0;
    }

    @Override
    protected double getLikelihoodForClass(NodeState state, double[] value, int classNum) {
        final int predictedClass = predictValue(state, value);
        //No real likelihood. Just yes or no.
        return (classNum==predictedClass)?1.0:0.0;
    }

    @Override
    protected void updateModelParameters(NodeState state, double[] valueBuffer, int[] resultBuffer, double[] value, int classNumber) {
        //TODO No tree? Initialize with the leaf.

        //TODO If there is a tree already:
        //TODO Go to the leaf
    }

    @Override
    protected boolean addValueBootstrap(NodeState state, double[] value, int classNum){
        //-1 because we will add 1 value to the buffer later.
        //while (getCurrentBufferLength() > (getMaxBufferLength()-1)) {
          //  removeFirstValueFromBuffer();
        //}
        return super.addValueBootstrap(state, value, classNum);
    }

    @Override
    protected void removeAllClassesHook(NodeState state) {
        //Nothing
    }
}
