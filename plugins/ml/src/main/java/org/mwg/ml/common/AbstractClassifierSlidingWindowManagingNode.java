package org.mwg.ml.common;

import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.Type;
import org.mwg.ml.ClassificationNode;

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
    public void classify(Callback<Integer> callback) {
        extractFeatures(new Callback<double[]>() {
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
            double curValue[] = new double[dims];
            System.arraycopy(valueBuffer, startIndex, curValue, 0, dims);
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

    /**
     * @param value
     * @param classNum
     * @return
     */
    protected abstract double getLikelihoodForClass(double value[], int classNum);

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
            double curValue[] = new double[dims];
            System.arraycopy(valueBuffer, startIndex, curValue, 0, dims);
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
            double curValue[] = new double[dims];
            System.arraycopy(valueBuffer, startIndex, curValue, 0, dims);
            updateModelParameters(curValue, resultBuffer[i]);
            startIndex += dims;
            i++;
        }
    }

    protected abstract void removeAllClassesHook();

    private void removeAllClasses() {
        removeAllClassesHook();
        unphasedState().setFromKey(INTERNAL_KNOWN_CLASSES_LIST, Type.INT_ARRAY, new int[0]);
    }

    /**
     * Adds new value to the buffer. Connotations change depending on whether the node is in bootstrap mode or not.
     *
     * @param value New value to add; {@code null} disallowed
     */
    protected boolean addValue(double value[], int result) {
        illegalArgumentIfFalse(value != null, "Value must be not null");

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
        for (int i = 0; i < resultBuffer.length; i++) {
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
        AbstractClassifierSlidingWindowManagingNode.requireNotNull(resBuffer, "result buffer must be not null");
        unphasedState().setFromKey(INTERNAL_RESULTS_BUFFER_KEY, Type.INT_ARRAY, resBuffer);
    }

    @Override
    protected void removeFirstValueFromResultBuffer() {
        int resultBuffer[] = getRealBufferClasses();
        if (resultBuffer.length == 0) {
            return;
        }
        int newResultBuffer[] = new int[resultBuffer.length-1];
        System.arraycopy(resultBuffer, 1, newResultBuffer, 0, resultBuffer.length-1);
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
    protected void addValueBootstrap(double value[], int result) {
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
    public void learn(int expectedClass, Callback<Boolean> callback) {
        extractFeatures(new Callback<double[]>() {
            @Override
            public void on(double[] result) {
                boolean outcome = addValue(result, expectedClass);
                callback.on(outcome);
            }
        });
    }

}
