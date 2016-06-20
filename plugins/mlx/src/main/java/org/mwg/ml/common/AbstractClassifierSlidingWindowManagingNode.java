package org.mwg.ml.common;

import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.Type;
import org.mwg.ml.ClassificationNode;
import org.mwg.plugin.NodeState;

/**
 * Created by andre on 5/4/2016.
 */
public abstract class AbstractClassifierSlidingWindowManagingNode extends AbstractSlidingWindowManagingNode implements ClassificationNode {

    protected static final int[] INTERNAL_RESULTS_BUFFER_DEF = new int[0];

    public AbstractClassifierSlidingWindowManagingNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    protected abstract int predictValue(NodeState state, double value[]);

    /**
     * {@inheritDoc}
     */
    @Override
    public void classify(final Callback<Integer> callback) {
        extractFeatures(new Callback<double[]>() {
            @Override
            public void on(double[] result) {
                int predictedClass = predictValue(unphasedState(), result);
                callback.on(predictedClass);
            }
        });
    }

    protected int getBufferErrorCount(NodeState state, double valueBuffer[], int resultBuffer[]) {
        //For each value in value buffer
        int startIndex = 0;
        if (resultBuffer.length == 0) {
            return 0;
        }

        final int dims = valueBuffer.length/resultBuffer.length;

        int errorCount = 0;
        int index = 0;
        while (startIndex + dims <= valueBuffer.length) {
            double curValue[] = new double[dims];
            System.arraycopy(valueBuffer, startIndex, curValue, 0, dims);
            int realClass = resultBuffer[index];
            int predictedClass = predictValue(state, curValue);
            errorCount += (realClass != predictedClass) ? 1 : 0;

            //Continue the loop
            startIndex += dims;
            index++;
        }
        return errorCount;
    }

    /**
     * Attribute key - List of known classes
     */
    private static final String INTERNAL_KNOWN_CLASSES_LIST = "_knownClassesList";

    protected void addToKnownClassesList(NodeState state, int classLabel) {
        int[] knownClasses = getKnownClasses();
        int[] newKnownClasses = new int[knownClasses.length + 1];
        for (int i = 0; i < knownClasses.length; i++) {
            if (classLabel == knownClasses[i]) {
                return; //Already known. No need to add
            }
            newKnownClasses[i] = knownClasses[i];
        }
        newKnownClasses[knownClasses.length] = classLabel;
        state.setFromKey(INTERNAL_KNOWN_CLASSES_LIST, Type.INT_ARRAY, newKnownClasses);
    }

    /**
     * @param value
     * @param classNum
     * @return
     */
    protected abstract double getLikelihoodForClass(NodeState state, double value[], int classNum);

    protected int[] getKnownClasses() {
        return unphasedState().getFromKeyWithDefault(INTERNAL_KNOWN_CLASSES_LIST, new int[0]);
    }

    /**
     * Adds value's contribution to total, sum and sum of squares of new model.
     * Does NOT build model yet.
     *
     * @param value New value
     */
    protected abstract void updateModelParameters(NodeState state, double[] valueBuffer, int[] resultBuffer, double[] value, int classNumber);

    @Override
    protected void setBootstrapModeHook(NodeState state) {
        //It would have been easy if not for keeping the buffers
        removeAllClasses(state);

        //Now step-by-step build new models
        double valueBuffer[] = state.getFromKeyWithDefault(INTERNAL_VALUE_BUFFER_KEY, new double[0]);
        int resultBuffer[] = state.getFromKeyWithDefault(INTERNAL_RESULTS_BUFFER_KEY, new int[0]);
        int startIndex = 0;
        final int dims = state.getFromKeyWithDefault(INPUT_DIM_KEY, INPUT_DIM_UNKNOWN);
        int i = 0;
        while (startIndex + dims < valueBuffer.length) {
            double curValue[] = new double[dims];
            System.arraycopy(valueBuffer, startIndex, curValue, 0, dims);
            updateModelParameters(state, valueBuffer, resultBuffer, curValue, resultBuffer[i]);
            startIndex += dims;
            i++;
        }
    }

    protected abstract void removeAllClassesHook(NodeState state);

    private void removeAllClasses(NodeState state) {
        removeAllClassesHook(state);
        state.setFromKey(INTERNAL_KNOWN_CLASSES_LIST, Type.INT_ARRAY, new int[0]);
    }

    /**
     * Adds new value to the buffer. Connotations change depending on whether the node is in bootstrap mode or not.
     *
     * @param value New value to add; {@code null} disallowed
     */
    protected boolean addValue(double value[], int result) {
        illegalArgumentIfFalse(value != null, "Value must be not null");
        NodeState state = unphasedState();
        boolean bootstrapMode = state.getFromKeyWithDefault(BOOTSTRAP_MODE_KEY, BOOTSTRAP_MODE_DEF);

        if (bootstrapMode) {
            return addValueBootstrap(state, value, result);
        }
        return addValueNoBootstrap(state, value, result);
    }

    protected static int[] adjustResultBuffer(NodeState state, int result, boolean bootstrapMode){
        int resultBuffer[] = state.getFromKeyWithDefault(INTERNAL_RESULTS_BUFFER_KEY, INTERNAL_RESULTS_BUFFER_DEF);;

        //So adding 1 value to the end and removing (currentBufferLength + 1) - maxBufferLength from the beginning.
        final int maxResultBufferLength = state.getFromKeyWithDefault(BUFFER_SIZE_KEY, BUFFER_SIZE_DEF);
        final int numValuesToRemoveFromBeginning = bootstrapMode? 0 : Math.max(0, resultBuffer.length + 1 - maxResultBufferLength);

        int newBuffer[] = new int[resultBuffer.length + 1 - numValuesToRemoveFromBeginning];
        //Setting first values
        System.arraycopy(resultBuffer, numValuesToRemoveFromBeginning, newBuffer, 0, newBuffer.length - 1);
        newBuffer[newBuffer.length-1] = result;
        state.setFromKey(INTERNAL_RESULTS_BUFFER_KEY, Type.INT_ARRAY, newBuffer);
        return newBuffer;
    }

    /**
     *
     * @param value
     * @param result
     * @return New bootstrap mode value
     */
    protected boolean addValueNoBootstrap(NodeState state, double value[], int result) {
        double newBuffer[] = AbstractClassifierSlidingWindowManagingNode.adjustValueBuffer(state, value, false);
        int newResultBuffer[] = AbstractClassifierSlidingWindowManagingNode.adjustResultBuffer(state, result, false);

        //Predict for each value in the buffer. Calculate percentage of errors.
        double errorInBuffer = ((double) getBufferErrorCount(state, newBuffer, newResultBuffer)) / newResultBuffer.length;
        double higherErrorThreshold = state.getFromKeyWithDefault(HIGH_ERROR_THRESH_KEY, HIGH_ERROR_THRESH_DEF);
        if (errorInBuffer > higherErrorThreshold) {
            NodeState newState = setBootstrapMode(state, true); //If number of errors is above higher threshold, get into the bootstrap
            updateModelParameters(newState, newBuffer, newResultBuffer, value, result);
            return true;
        }
        return false;
    }

    /**
     * Adds new value to the buffer. Gaussian model is regenerated.
     *
     * @param value New value to add; {@code null} disallowed
     */
    protected boolean addValueBootstrap(NodeState state, double value[], int result) {
        double newBuffer[] = AbstractClassifierSlidingWindowManagingNode.adjustValueBuffer(state, value, true);
        int newResultBuffer[] = AbstractClassifierSlidingWindowManagingNode.adjustResultBuffer(state, result, true);
        boolean newBootstrap = true;

        if (newResultBuffer.length >= getMaxBufferLength()) {
            //Predict for each value in the buffer. Calculate percentage of errors.
            double errorInBuffer = ((double) getBufferErrorCount(state, newBuffer, newResultBuffer)) / newResultBuffer.length;
            double lowerErrorThreshold = state.getFromKeyWithDefault(LOW_ERROR_THRESH_KEY, LOW_ERROR_THRESH_DEF);
            if (errorInBuffer <= lowerErrorThreshold) {
                setBootstrapMode(state, false); //If number of errors is below lower threshold, get out of bootstrap
                newBootstrap = false;
            }
        }

        updateModelParameters(state, newBuffer, newResultBuffer, value, result);
        return newBootstrap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void learn(final int expectedClass, final Callback<Boolean> callback) {
        extractFeatures(new Callback<double[]>() {
            @Override
            public void on(double[] result) {
                boolean outcome = addValue(result, expectedClass);
                callback.on(outcome);
            }
        });
    }

}
