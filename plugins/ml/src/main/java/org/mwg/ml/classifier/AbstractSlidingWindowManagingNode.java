package org.mwg.ml.classifier;

import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.Type;
import org.mwg.plugin.AbstractNode;

import java.util.Objects;

/**
 * Created by andre on 4/26/2016.
 *
 * Common superclass for all ML algorithms that use bootstrap mode
 * and
 */
public abstract class AbstractSlidingWindowManagingNode extends AbstractNode {
    /**
     * Attribute key - whether the node is in bootstrap (re-learning) mode
     */
    public static final String BOOTSTRAP_MODE_KEY = "bootstrapMode";
    public static final boolean BOOTSTRAP_MODE_DEF = true;

    /**
     *  Buffer size
     */
    public static final String BUFFER_SIZE_KEY = "BufferSize";

    /**
     *  Number of input dimensions
     */
    public static final String INPUT_DIM_KEY = "InputDimensions";
    /**
     *  Index of response value
     */
    public static final String RESPONSE_INDEX_KEY = "ResponseIndex";
    /**
     *  Higher error threshold
     */
    public static final String HIGH_ERROR_THRESH_KEY = "HighErrorThreshold";
    /**
     *  Lower error threshold
     */
    public static final String LOW_ERROR_THRESH_KEY = "LowErrorThreshold";
    /**
     *  Value
     */
    public static final String FEATURES_KEY = "FEATURES";

    /**
     * Attribute key - sliding window of values
     */
    private static final String INTERNAL_VALUE_BUFFER_KEY = "_valueBuffer";

    public AbstractSlidingWindowManagingNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    protected void addValueToBuffer(double[] value) {
        double valueBuffer[] = getValueBuffer();
        double newBuffer[] = new double[valueBuffer.length + value.length];
        for (int i = 0; i < valueBuffer.length; i++) {
            newBuffer[i] = valueBuffer[i];
        }
        for (int i = valueBuffer.length; i < newBuffer.length; i++) {
            newBuffer[i] = value[i - valueBuffer.length];
        }
        setValueBuffer(newBuffer);
    }

    protected final void setValueBuffer(double[] valueBuffer) {
        Objects.requireNonNull(valueBuffer,"value buffer must be not null");
        unphasedState().set(_resolver.stringToLongKey(INTERNAL_VALUE_BUFFER_KEY), Type.DOUBLE_ARRAY, valueBuffer);
    }

    protected void removeFirstValueFromBuffer() {
        final int dims = getInputDimensions();
        double valueBuffer[] = getValueBuffer();
        if (valueBuffer.length == 0) {
            return;
        }
        double newBuffer[] = new double[valueBuffer.length - dims];
        for (int i = 0; i < newBuffer.length; i++) {
            newBuffer[i] = valueBuffer[i + dims];
        }
        setValueBuffer(newBuffer);
    }

    protected int getNumValuesInBuffer() {
        final int valLength = getValueBuffer().length;
        return valLength / getInputDimensions();
    }

    public boolean isInBootstrapMode() {
        return unphasedState().getFromKeyWithDefault(BOOTSTRAP_MODE_KEY, BOOTSTRAP_MODE_DEF);
    }

    protected double[] getValueBuffer() {
        return unphasedState().getFromKeyWithDefault(INTERNAL_VALUE_BUFFER_KEY, new double[0]);
    }

    /**
     * @return Class index - index in a value array, where class label is supposed to be
     */
    protected int getMaxBufferLength() {
        Object objClassIndex = unphasedState().getFromKey(BUFFER_SIZE_KEY);
        Objects.requireNonNull(objClassIndex, "Buffer size must be not null");
        return ((Integer) objClassIndex).intValue();
    }

    /**
     * @return Class index - index in a value array, where class label is supposed to be
     */
    protected int getInputDimensions() {
        Object objClassIndex = unphasedState().getFromKey(INPUT_DIM_KEY);
        Objects.requireNonNull(objClassIndex, "Input dimensions must be not null");
        return ((Integer) objClassIndex).intValue();
    }

    /**
     * @return Class index - index in a value array, where class label is supposed to be
     */
    protected int getResponseIndex() {
        Object objClassIndex = unphasedState().getFromKey(RESPONSE_INDEX_KEY);
        Objects.requireNonNull(objClassIndex, "Response index must be not null");
        return ((Integer) objClassIndex).intValue();
    }

    @Override
    public void index(String indexName, Node nodeToIndex, String[] keyAttributes, Callback<Boolean> callback) {
        // Nothing for now
    }

    @Override
    public void unindex(String indexName, Node nodeToIndex, String[] keyAttributes, Callback<Boolean> callback) {
        // Nothing for now
    }

    @Override
    public void setProperty(String propertyName, byte propertyType, Object propertyValue) {
        if(propertyName.equals(RESPONSE_INDEX_KEY)){
            illegalArgumentIfFalse(propertyValue instanceof Integer, "Class index should be integer");
            illegalArgumentIfFalse((Integer)propertyValue >= 0, "Class index should be non-negative");
            unphasedState().setFromKey(RESPONSE_INDEX_KEY, Type.INT, propertyValue);
        }else if(propertyName.equals(INPUT_DIM_KEY)){
            illegalArgumentIfFalse(propertyValue instanceof Integer, "Number of input dimensions should be integer");
            illegalArgumentIfFalse((Integer)propertyValue >= 0, "Input should have at least dimension");
            unphasedState().setFromKey(INPUT_DIM_KEY, Type.INT, propertyValue);
        }else if(propertyName.equals(BUFFER_SIZE_KEY)){
            illegalArgumentIfFalse(propertyValue instanceof Integer, "Buffer size should be integer");
            illegalArgumentIfFalse((Integer)propertyValue > 0, "Buffer size should be positive");
            unphasedState().setFromKey(BUFFER_SIZE_KEY, Type.INT, propertyValue);
        }else if (propertyName.equals(LOW_ERROR_THRESH_KEY)){
            illegalArgumentIfFalse( (propertyValue instanceof Double)||(propertyValue instanceof Integer),
                    "Low error threshold should be of type double or integer");
            if (propertyValue instanceof Double){
                illegalArgumentIfFalse((Double)propertyValue >= 0, "Low error threshold should be non-negative");
                unphasedState().setFromKey(LOW_ERROR_THRESH_KEY, Type.DOUBLE, propertyValue);
            }else{
                illegalArgumentIfFalse((Integer)propertyValue >= 0, "Low error threshold should be non-negative");
                unphasedState().setFromKey(LOW_ERROR_THRESH_KEY, Type.DOUBLE, ((Integer)propertyValue).doubleValue());
            }
        }else if (propertyName.equals(HIGH_ERROR_THRESH_KEY)){
            illegalArgumentIfFalse((propertyValue instanceof Double)||(propertyValue instanceof Integer),
                    "High error threshold should be of type double or integer");
            if (propertyValue instanceof Double){
                illegalArgumentIfFalse((Double)propertyValue >= 0, "High error threshold should be non-negative");
                unphasedState().setFromKey(HIGH_ERROR_THRESH_KEY, Type.DOUBLE, propertyValue);
            }else{
                illegalArgumentIfFalse((Integer)propertyValue >= 0, "High error threshold should be non-negative");
                unphasedState().setFromKey(HIGH_ERROR_THRESH_KEY, Type.DOUBLE, ((Integer)propertyValue).doubleValue());
            }
        }else if(propertyName.equals(FEATURES_KEY)){
            addValue((double[]) propertyValue);
        }else{
            super.setProperty(propertyName,propertyType,propertyValue);
        }
    }
    /**
     * Adds new value to the buffer. Connotations change depending on whether the node is in bootstrap mode or not.
     *
     * @param value New value to add; {@code null} disallowed
     */
    public void addValue(double value[]) {
        illegalArgumentIfFalse(value != null, "Value must be not null");
        illegalArgumentIfFalse(value.length == getInputDimensions(), "Class index is not included in the value");

        if (isInBootstrapMode()) {
            addValueBootstrap(value);
        } else {
            addValueNoBootstrap(value);
        }
    }

    /**
     * Asserts that condition is true. If not - throws {@code IllegalArgumentException} with a specified error message
     *
     * @param condition    Condition to test
     * @param errorMessage Error message thrown with {@code IllegalArgumentException} (if thrown)
     * @throws IllegalArgumentException if condition is false
     */
    protected void illegalArgumentIfFalse(boolean condition, String errorMessage) {
        assert errorMessage != null;
        if (!condition) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    /**
     * Defines implementation-specific actions to do before going to bootstrap mode.
     */
    protected abstract void setBootstrapModeHook();

    protected void setBootstrapMode(boolean newBootstrapMode) {
        if (newBootstrapMode) {
            //New state starts now
            phasedState();
            setBootstrapModeHook();
        }
        unphasedState().setFromKey(BOOTSTRAP_MODE_KEY, Type.BOOL, newBootstrapMode);
    }

    protected void addValueNoBootstrap(double value[]) {
        addValueToBuffer(value);
        while (getCurrentBufferLength() > getMaxBufferLength()) {
            removeFirstValueFromBuffer();
        }

        //Predict for each value in the buffer. Calculate percentage of errors.
        double errorInBuffer = getBufferError();
        if (errorInBuffer > getHigherErrorThreshold()) {
            setBootstrapMode(true); //If number of errors is above higher threshold, get into the bootstrap
        }
    }

    protected double getHigherErrorThreshold() {
        Object objHET = unphasedState().getFromKey(HIGH_ERROR_THRESH_KEY);
        Objects.requireNonNull(objHET, "Higher error threshold must be not null");
        return (double) objHET;
    }

    protected double getLowerErrorThreshold() {
        Object objLET = unphasedState().getFromKey(LOW_ERROR_THRESH_KEY);
        Objects.requireNonNull(objLET, "Lower error threshold must be not null");
        return (double) objLET;
    }

    protected abstract double getBufferError();

    /**
     * Adds new value to the buffer. Gaussian model is regenerated.
     *
     * @param value New value to add; {@code null} disallowed
     */
    private void addValueBootstrap(double value[]) {
        addValueToBuffer(value); //In bootstrap - no need to account for length

        if (getNumValuesInBuffer() >= getMaxBufferLength()) {
            //Predict for each value in the buffer. Calculate percentage of errors.
            double errorInBuffer = getBufferError();
            if (errorInBuffer <= getLowerErrorThreshold()) {
                setBootstrapMode(false); //If number of errors is below lower threshold, get out of bootstrap
            }
        }

        updateModelParameters(value);
    }

    /**
     * Adds value's contribution to total, sum and sum of squares of new model.
     * Does NOT build model yet.
     *
     * @param value New value
     */
    protected abstract void updateModelParameters(double value[]);

    public int getCurrentBufferLength() {
        double valueBuffer[] = getValueBuffer();
        final int dims = getInputDimensions();
        return valueBuffer.length / dims;
    }

}
