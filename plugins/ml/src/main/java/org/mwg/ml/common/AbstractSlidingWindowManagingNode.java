package org.mwg.ml.common;

import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.Type;
import org.mwg.plugin.AbstractNode;

import java.util.Objects;

/**
 * Created by andre on 4/26/2016.
 *
 * Common superclass for all ML algorithm that use bootstrap mode
 * and
 */
public abstract class AbstractSlidingWindowManagingNode extends AbstractMLNode {
    /**
     * Internal keys - those attributes are only for internal use within the node.
     * They are not supposed to be accessed from outside (although it is not banned).
     */
    protected static final String INTERNAL_RESULTS_BUFFER_KEY = "_results";

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
     *  Buffer size - default
     */
    public static final int BUFFER_SIZE_DEF = 50;

    /**
     *  Number of input dimensions
     */
    public static final String INPUT_DIM_KEY = "InputDimensions";
    /**
     * Unknown number of input dimensions
     */
    public static final int INPUT_DIM_UNKNOWN = -1;
    /**
     *  Number of input dimensions - default (unknown so far)
     */
    public static final int INPUT_DIM_DEF = INPUT_DIM_UNKNOWN;

    /**
     *  Higher error threshold
     */
    public static final String HIGH_ERROR_THRESH_KEY = "HighErrorThreshold";
    /**
     *  Higher error threshold - default
     */
    public static final double HIGH_ERROR_THRESH_DEF = 0.1;

    /**
     *  Lower error threshold
     */
    public static final String LOW_ERROR_THRESH_KEY = "LowErrorThreshold";
    /**
     *  Lower error threshold
     */
    public static final double LOW_ERROR_THRESH_DEF = 0.05;

    /**
     * Attribute key - sliding window of values
     */
    private static final String INTERNAL_VALUE_BUFFER_KEY = "_valueBuffer";

    public AbstractSlidingWindowManagingNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    protected final void setValueBuffer(double[] valueBuffer) {
        Objects.requireNonNull(valueBuffer,"value buffer must be not null");
        unphasedState().set(_resolver.stringToLongKey(INTERNAL_VALUE_BUFFER_KEY), Type.DOUBLE_ARRAY, valueBuffer);
    }

    //Results buffer is set by further class. .

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
        removeFirstValueFromResultBuffer();
    }

    protected abstract void removeFirstValueFromResultBuffer();

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
        return unphasedState().getFromKeyWithDefault(BUFFER_SIZE_KEY, BUFFER_SIZE_DEF);
    }

    /**
     * @return Class index - index in a value array, where class label is supposed to be
     */
    protected int getInputDimensions() {
        return unphasedState().getFromKeyWithDefault(INPUT_DIM_KEY, INPUT_DIM_DEF);
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
        if(BUFFER_SIZE_KEY.equals(propertyName)){
            illegalArgumentIfFalse(propertyValue instanceof Integer, "Buffer size should be integer");
            illegalArgumentIfFalse((Integer)propertyValue > 0, "Buffer size should be positive");
            unphasedState().setFromKey(BUFFER_SIZE_KEY, Type.INT, propertyValue);
        }else if (LOW_ERROR_THRESH_KEY.equals(propertyName)){
            illegalArgumentIfFalse( (propertyValue instanceof Double)||(propertyValue instanceof Integer),
                    "Low error threshold should be of type double or integer");
            if (propertyValue instanceof Double){
                illegalArgumentIfFalse((Double)propertyValue >= 0, "Low error threshold should be non-negative");
                unphasedState().setFromKey(LOW_ERROR_THRESH_KEY, Type.DOUBLE, propertyValue);
            }else{
                illegalArgumentIfFalse((Integer)propertyValue >= 0, "Low error threshold should be non-negative");
                unphasedState().setFromKey(LOW_ERROR_THRESH_KEY, Type.DOUBLE, ((Integer)propertyValue).doubleValue());
            }
        }else if (HIGH_ERROR_THRESH_KEY.equals(propertyName)){
            illegalArgumentIfFalse((propertyValue instanceof Double)||(propertyValue instanceof Integer),
                    "High error threshold should be of type double or integer");
            if (propertyValue instanceof Double){
                illegalArgumentIfFalse((Double)propertyValue >= 0, "High error threshold should be non-negative");
                unphasedState().setFromKey(HIGH_ERROR_THRESH_KEY, Type.DOUBLE, propertyValue);
            }else{
                illegalArgumentIfFalse((Integer)propertyValue >= 0, "High error threshold should be non-negative");
                unphasedState().setFromKey(HIGH_ERROR_THRESH_KEY, Type.DOUBLE, ((Integer)propertyValue).doubleValue());
            }
        }else if(INTERNAL_VALUE_BUFFER_KEY.equals(propertyName) || BOOTSTRAP_MODE_KEY.equals(propertyName) ||
                INPUT_DIM_KEY.equals(propertyName) || INTERNAL_RESULTS_BUFFER_KEY.equals(propertyName)){
            //Nothing. They are unsettable directly
        }else{
            super.setProperty(propertyName,propertyType,propertyValue);
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

    protected double getHigherErrorThreshold() {
        return unphasedState().getFromKeyWithDefault(HIGH_ERROR_THRESH_KEY, HIGH_ERROR_THRESH_DEF);
    }

    protected double getLowerErrorThreshold() {
        return unphasedState().getFromKeyWithDefault(LOW_ERROR_THRESH_KEY, LOW_ERROR_THRESH_DEF);
    }

    protected abstract double getBufferError();

    public int getCurrentBufferLength() {
        double valueBuffer[] = getValueBuffer();
        final int dims = getInputDimensions();
        return valueBuffer.length / dims;
    }

    @Override
    public Object get(String propertyName){
        if(INPUT_DIM_KEY.equals(propertyName)){
            return getInputDimensions();
        }else if(BUFFER_SIZE_KEY.equals(propertyName)){
            return getMaxBufferLength();
        }else if (LOW_ERROR_THRESH_KEY.equals(propertyName)){
            return getLowerErrorThreshold();
        }else if (HIGH_ERROR_THRESH_KEY.equals(propertyName)){
            return getHigherErrorThreshold();
        }else if(BOOTSTRAP_MODE_KEY.equals(propertyName)){
            return isInBootstrapMode();
        }
        return super.get(propertyName);
    }

}
