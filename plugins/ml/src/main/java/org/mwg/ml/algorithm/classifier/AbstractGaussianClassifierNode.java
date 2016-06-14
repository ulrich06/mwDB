package org.mwg.ml.algorithm.classifier;

import org.mwg.Graph;
import org.mwg.Type;
import org.mwg.ml.common.AbstractClassifierSlidingWindowManagingNode;

import java.util.Objects;

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
    private static final String INTERNAL_SUMSQUARE_KEY_PREFIX = "_sumSquare_";

    /**
     * Prefix for number of measurements attribute. For each class its class label will be appended to
     * this key prefix.
     */
    private static final String INTERNAL_TOTAL_KEY_PREFIX = "_total_";

    public AbstractGaussianClassifierNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    protected final void setTotal(int classNum, int val) {
        assert val >= 0;
        unphasedState().setFromKey(INTERNAL_TOTAL_KEY_PREFIX + classNum, Type.INT, val);
    }

    protected final void setSums(int classNum, double[] vals) {
        assert vals != null;
        unphasedState().setFromKey(INTERNAL_SUM_KEY_PREFIX + classNum, Type.DOUBLE_ARRAY, vals);
    }

    protected final void setSumsSquared(int classNum, double[] vals) {
        assert vals != null;
        unphasedState().setFromKey(INTERNAL_SUMSQUARE_KEY_PREFIX + classNum, Type.DOUBLE_ARRAY, vals);
    }

    protected final int getClassTotal(int classNum) {
        Object objClassTotal = unphasedState().getFromKey(INTERNAL_TOTAL_KEY_PREFIX + classNum);
        Objects.requireNonNull(objClassTotal, "Class total must be not null (class " + classNum + ")");
        return ((Integer) objClassTotal).intValue();
    }

    protected double[] getSums(int classNum) {
        Object objSum = unphasedState().getFromKey(INTERNAL_SUM_KEY_PREFIX + classNum);
        Objects.requireNonNull(objSum, "Sums must be not null (class " + classNum + ")");
        return (double[]) objSum;
    }

    protected double[] getSumSquares(int classNum) {
        Object objSumSq = unphasedState().getFromKey(INTERNAL_SUMSQUARE_KEY_PREFIX + classNum);
        Objects.requireNonNull(objSumSq, "Sums of squares must be not null (class " + classNum + ")");
        return (double[]) objSumSq;
    }

    @Override
    protected void removeAllClassesHook() {
        int classes[] = getKnownClasses();
        for (int curClass : classes) {
            unphasedState().setFromKey(INTERNAL_TOTAL_KEY_PREFIX + curClass, Type.INT, 0);
            unphasedState().setFromKey(INTERNAL_SUM_KEY_PREFIX + curClass, Type.DOUBLE_ARRAY, new double[0]);
            unphasedState().setFromKey(INTERNAL_SUMSQUARE_KEY_PREFIX + curClass, Type.DOUBLE_ARRAY, new double[0]);
        }
    }


}
