package org.mwg.ml.algorithm.classifier;

import org.mwg.Graph;
import org.mwg.Type;
import org.mwg.ml.common.AbstractSlidingWindowManagingNode;

import java.util.Arrays;
import java.util.Objects;

/**
 * Created by andre on 4/26/2016.
 */
public abstract class AbstractGaussianClassifierNode extends AbstractSlidingWindowManagingNode {

    /**
     * Internal keys - those attributes are only for internal use within the node.
     * They are not supposed to be accessed from outside (although it is not banned).
     */

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

    /**
     * Attribute key - List of known classes
     */
    private static final String INTERNAL_KNOWN_CLASSES_LIST = "_knownClassesList";

    //TODO Not allow setting?

    /**
     * {@inheritDoc}
     */
    public AbstractGaussianClassifierNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    protected void addToKnownClassesList(int classLabel) {
        int[] knownClasses = getKnownClasses();
        int[] newKnownClasses = new int[knownClasses.length + 1];
        for (int i = 0; i < knownClasses.length; i++) {
            if (classLabel == knownClasses[i]) {
                return; //Already known. No need to add
            }
            newKnownClasses[i] = knownClasses[i];
        }
        newKnownClasses[knownClasses.length] = classLabel;
        unphasedState().setFromKey(INTERNAL_KNOWN_CLASSES_LIST, Type.INT_ARRAY, newKnownClasses);
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

    /**
     * Initializes map values for class num: sum, sum of squares and total.
     *
     * @param classNum Number of class
     */
    protected abstract void initializeClassIfNecessary(int classNum);

    /**
     * @param value
     * @param classNum
     * @return
     */
    protected abstract double getLikelihoodForClass(double value[], int classNum);

    protected abstract int predictValue(double value[]);

    protected int[] getKnownClasses() {
        return unphasedState().getFromKeyWithDefault(INTERNAL_KNOWN_CLASSES_LIST, new int[0]);
    }


    public int[] getPredictedBufferClasses() {
        //For each value in value buffer
        int startIndex = 0;
        final int dims = getInputDimensions();

        double valueBuffer[] = getValueBuffer();
        final int numValues = valueBuffer.length / dims;//TODO What if there are not enough values?
        if (numValues == 0) {
            return new int[0];
        }

        int result[] = new int[numValues];

        final int clIndex = getResponseIndex();
        int i = 0;
        while (startIndex + dims < valueBuffer.length) {
            double curValue[] = Arrays.copyOfRange(valueBuffer, startIndex, startIndex + dims);
            result[i] = predictValue(curValue);
            //Continue the loop
            startIndex += dims;
            i++;
        }
        return result;
    }

    public int[] getRealBufferClasses() {
        //For each value in value buffer
        int startIndex = 0;
        final int dims = getInputDimensions();

        double valueBuffer[] = getValueBuffer();
        final int numValues = valueBuffer.length / dims;//TODO What if there are not enough values?
        if (numValues == 0) {
            return new int[0];
        }

        int result[] = new int[numValues];

        final int clIndex = getResponseIndex();
        int i = 0;
        while (startIndex + dims < valueBuffer.length) {
            double curValue[] = Arrays.copyOfRange(valueBuffer, startIndex, startIndex + dims);
            result[i] = (int) curValue[clIndex];

            //Continue the loop
            startIndex += dims;
            i++;
        }
        return result;
    }

    public int getBufferErrorCount() {
        //For each value in value buffer
        int startIndex = 0;
        final int dims = getInputDimensions();

        double valueBuffer[] = getValueBuffer();
        final int numValues = valueBuffer.length / dims;//TODO What if there are not enough values?
        if (numValues == 0) {
            return 0;
        }

        final int clIndex = getResponseIndex();
        int errorCount = 0;
        while (startIndex + dims <= valueBuffer.length) {
            double curValue[] = Arrays.copyOfRange(valueBuffer, startIndex, startIndex + dims);
            int realClass = (int) curValue[clIndex];
            int predictedClass = predictValue(curValue);
            errorCount += (realClass != predictedClass) ? 1 : 0;

            //Continue the loop
            startIndex += dims;
        }
        return errorCount;
    }

    /**
     * @return Prediction accuracy for data in the buffer. {@code NaN} if not applicable.
     */
    @Override
    public double getBufferError() {
        return ((double) getBufferErrorCount()) / getCurrentBufferLength();
    }


    @Override
    protected void setBootstrapModeHook() {
        //It would have been easy if not for keeping the buffers
        removeAllClasses();

        //Now step-by-step build new models
        double valueBuffer[] = getValueBuffer();
        int startIndex = 0;
        final int dims = getInputDimensions();
        while (startIndex + dims < valueBuffer.length) {
            double curValue[] = Arrays.copyOfRange(valueBuffer, startIndex, startIndex + dims);
            updateModelParameters(curValue);
            startIndex += dims;
        }
    }

    private void removeAllClasses() {
        int classes[] = getKnownClasses();
        for (int curClass : classes) {
            unphasedState().setFromKey(INTERNAL_TOTAL_KEY_PREFIX + curClass, Type.INT, 0);
            unphasedState().setFromKey(INTERNAL_SUM_KEY_PREFIX + curClass, Type.DOUBLE_ARRAY, new double[0]);
            unphasedState().setFromKey(INTERNAL_SUMSQUARE_KEY_PREFIX + curClass, Type.DOUBLE_ARRAY, new double[0]);
        }
        unphasedState().setFromKey(INTERNAL_KNOWN_CLASSES_LIST, Type.INT_ARRAY, new int[0]);
    }

}
