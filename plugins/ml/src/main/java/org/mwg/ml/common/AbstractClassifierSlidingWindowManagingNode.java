package org.mwg.ml.common;

import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.Type;
import org.mwg.ml.ClassificationNode;

import java.util.Arrays;
import java.util.Objects;

/**
 * Created by andre on 5/4/2016.
 */
public abstract class AbstractClassifierSlidingWindowManagingNode extends AbstractSlidingWindowManagingNode implements ClassificationNode {

    protected static final int[] INTERNAL_RESULTS_BUFFER_DEF = new int[0];

    public AbstractClassifierSlidingWindowManagingNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    protected abstract int predictValue(double value[]);

    /**
     * {@inheritDoc}
     */
    @Override
    public void classify(Callback<Integer> callback){
        extractFeatures(new Callback<double[]>(){
            @Override
            public void on(double[] result) {
                int predictedClass = predictValue(result);
                callback.on(predictedClass);
            }
        });
    }

    public int[] getRealBufferClasses() {
        return unphasedState().getFromKeyWithDefault(INTERNAL_RESULTS_BUFFER_KEY, INTERNAL_RESULTS_BUFFER_DEF);
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

        final int[] realClasses = getRealBufferClasses();

        int errorCount = 0;
        int index = 0;
        while (startIndex + dims <= valueBuffer.length) {
            double curValue[] = Arrays.copyOfRange(valueBuffer, startIndex, startIndex + dims);
            int realClass = realClasses[index];
            int predictedClass = predictValue(curValue);
            errorCount += (realClass != predictedClass) ? 1 : 0;

            //Continue the loop
            startIndex += dims;
            index++;
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

    /**
     * Adds value's contribution to total, sum and sum of squares of new model.
     * Does NOT build model yet.
     *
     * @param value New value
     */
    protected abstract void updateModelParameters(double value[], int classNumber);

    @Override
    protected void setBootstrapModeHook() {
        //It would have been easy if not for keeping the buffers
        removeAllClasses();

        //Now step-by-step build new models
        double valueBuffer[] = getValueBuffer();
        int resultBuffer[] = getRealBufferClasses();
        int startIndex = 0;
        final int dims = getInputDimensions();
        int i = 0;
        while (startIndex + dims < valueBuffer.length) {
            double curValue[] = Arrays.copyOfRange(valueBuffer, startIndex, startIndex + dims);
            updateModelParameters(curValue, resultBuffer[i]);
            startIndex += dims;
            i++;
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


    /**
     * Adds new value to the buffer. Connotations change depending on whether the node is in bootstrap mode or not.
     *
     * @param value New value to add; {@code null} disallowed
     */
    public boolean addValue(double value[], int result) {
        illegalArgumentIfFalse(value != null, "Value must be not null");
        illegalArgumentIfFalse(value.length == getInputDimensions(), "Class index is not included in the value");

        if (isInBootstrapMode()) {
            addValueBootstrap(value, result);
        } else {
            addValueNoBootstrap(value, result);
        }
        return isInBootstrapMode(); //Can change since last time
    }

    protected void addValueToBuffer(double[] value, int result) {
        double valueBuffer[] = getValueBuffer();
        int resultBuffer[] = getRealBufferClasses();
        double newBuffer[] = new double[valueBuffer.length + value.length];
        int newResultBuffer[] = new int[resultBuffer.length + 1];
        for (int i = 0; i < valueBuffer.length; i++) {
            newBuffer[i] = valueBuffer[i];
        }
        for (int i=0;i<resultBuffer.length;i++){
            newResultBuffer[i] = resultBuffer[i];
        }
        for (int i = valueBuffer.length; i < newBuffer.length; i++) {
            newBuffer[i] = value[i - valueBuffer.length];
        }
        newResultBuffer[resultBuffer.length] = result;
        setValueBuffer(newBuffer);
        setResultBuffer(newResultBuffer);
    }

    protected final void setResultBuffer(int[] resBuffer) {
        Objects.requireNonNull(resBuffer,"result buffer must be not null");
        unphasedState().set(_resolver.stringToLongKey(INTERNAL_RESULTS_BUFFER_KEY), Type.INT_ARRAY, resBuffer);
    }

    @Override
    protected void removeFirstValueFromResultBuffer(){
        int resultBuffer[] = getRealBufferClasses();
        if (resultBuffer.length == 0) {
            return;
        }
        int newResultBuffer[] = Arrays.copyOfRange(resultBuffer, 1, resultBuffer.length-1);
        setResultBuffer(newResultBuffer);
    }

    protected void addValueNoBootstrap(double value[], int result) {
        addValueToBuffer(value, result);
        while (getCurrentBufferLength() > getMaxBufferLength()) {
            removeFirstValueFromBuffer();
        }

        //Predict for each value in the buffer. Calculate percentage of errors.
        double errorInBuffer = getBufferError();
        if (errorInBuffer > getHigherErrorThreshold()) {
            setBootstrapMode(true); //If number of errors is above higher threshold, get into the bootstrap
        }
    }

    /**
     * Adds new value to the buffer. Gaussian model is regenerated.
     *
     * @param value New value to add; {@code null} disallowed
     */
    private void addValueBootstrap(double value[], int result) {
        addValueToBuffer(value, result); //In bootstrap - no need to account for length

        if (getNumValuesInBuffer() >= getMaxBufferLength()) {
            //Predict for each value in the buffer. Calculate percentage of errors.
            double errorInBuffer = getBufferError();
            if (errorInBuffer <= getLowerErrorThreshold()) {
                setBootstrapMode(false); //If number of errors is below lower threshold, get out of bootstrap
            }
        }

        updateModelParameters(value, result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void learn(int expectedClass, Callback<Boolean> callback){
        extractFeatures(new Callback<double[]>(){
            @Override
            public void on(double[] result) {
                boolean outcome = addValue(result, expectedClass);
                callback.on(outcome);
            }
        });
    }

}
