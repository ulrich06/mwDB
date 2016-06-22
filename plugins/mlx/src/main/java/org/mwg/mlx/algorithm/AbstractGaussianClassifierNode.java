package org.mwg.mlx.algorithm;

import org.mwg.Graph;
import org.mwg.Type;
import org.mwg.plugin.NodeState;

/**
 * Created by andrey.boytsov on 17/05/16.
 */
public abstract class AbstractGaussianClassifierNode extends AbstractClassifierSlidingWindowManagingNode {

    /**
     * Prefix for sum attribute. For each class its class label will be appended to
     * this key prefix.
     */
    protected static final String INTERNAL_SUM_KEY_PREFIX = "_sum_";

    /**
     * Prefix for sum of squares attribute. For each class its class label will be appended to
     * this key prefix.
     */
    protected static final String INTERNAL_SUMSQUARE_KEY_PREFIX = "_sumSquare_";

    /**
     * Prefix for number of measurements attribute. For each class its class label will be appended to
     * this key prefix.
     */
    protected static final String INTERNAL_TOTAL_KEY_PREFIX = "_total_";

    public AbstractGaussianClassifierNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    @Override
    protected void removeAllClassesHook(NodeState state) {
        int classes[] = state.getFromKeyWithDefault(KNOWN_CLASSES_LIST_KEY, new int[0]);
        for (int curClass : classes) {
            state.setFromKey(INTERNAL_TOTAL_KEY_PREFIX + curClass, Type.INT, 0);
            state.setFromKey(INTERNAL_SUM_KEY_PREFIX + curClass, Type.DOUBLE_ARRAY, new double[0]);
            state.setFromKey(INTERNAL_SUMSQUARE_KEY_PREFIX + curClass, Type.DOUBLE_ARRAY, new double[0]);
        }
    }


}
