package org.mwg.ml.algorithm.anomalydetector;

import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.Type;
import org.mwg.ml.AbstractMLNode;
import org.mwg.ml.AnomalyDetectionNode;
import org.mwg.plugin.NodeFactory;

import java.util.Arrays;

/**
 * Anomaly is detected if at least for one of the dimensions the value is <...> interquartile ranges away from the mean.
 * <p>
 * Created by andrey.boytsov on 14/06/16.
 */
public class InterquartileRangeOutlierDetectorNode extends AbstractMLNode implements AnomalyDetectionNode {

    public static final String NAME = "InterquartileRangeAnomalyDetection";

    /**
     * Attribute key - sliding window of values
     */
    private static final String INTERNAL_VALUE_BUFFER_KEY = "_valueBuffer";
    /**
     * Buffer size
     */
    public static final String BUFFER_SIZE_KEY = "BufferSize";
    /**
     * Buffer size - default
     */
    public static final int BUFFER_SIZE_DEF = 50;
    /**
     * Number of input dimensions
     */
    public static final String INPUT_DIM_KEY = "InputDimensions";

    /**
     * Number of input dimensions - default (unknown so far)
     */
    public static final int INPUT_DIM_DEF = -1;


    public static final double UPPER_PERCENTILE = 0.75;
    public static final double LOWER_PERCENTILE = 0.25;
    public static final double RANGE_COEF = 1.5;

    /**
     * Upper bound for some dimension (dimension added to the key)
     */
    public static final String UPPER_BOUND_KEY_PREFIX = "UpperBoundDimension";

    /**
     * Lower bound for some dimension (dimension added to the key)
     */
    public static final String LOWER_BOUND_KEY_PREFIX = "LowerBoundDimension";

    public InterquartileRangeOutlierDetectorNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    /**
     * Adds new value to the buffer. Connotations change depending on whether the node is in bootstrap mode or not.
     *
     * @param value New value to add; {@code null} disallowed
     */
    protected boolean addValue(double value[]) {
        illegalArgumentIfFalse(value != null, "Value must be not null");

        addValueToBuffer(value);
        while (getCurrentBufferLength() > getMaxBufferLength()) {
            removeFirstValueFromBuffer();
        }
        recalculateBounds();

        return checkValue(value);
    }

    private void recalculateBounds() {
        final int dims = getInputDimensions();
        double buf[] = getValueBuffer();
        int len = getCurrentBufferLength();
        for (int i = 0; i < dims; i++) {
            //Get column
            double column[] = new double[len];
            int index = i;
            for (int j = 0; j < len; j++) {
                column[j] = buf[index];
                index += dims;
            }
            //Sort.
            Arrays.sort(column);
            //Get 25 and 75 percentile
            double upperPercentile = column[Math.min((int) Math.round(len * UPPER_PERCENTILE), len - 1)];
            double lowerPercentile = column[Math.max((int) Math.round(len * LOWER_PERCENTILE), 0)];
            double interquartileRange = upperPercentile - lowerPercentile;
            double upperBound = upperPercentile + RANGE_COEF * interquartileRange;
            double lowerBound = lowerPercentile - RANGE_COEF * interquartileRange;
            unphasedState().setFromKey(UPPER_BOUND_KEY_PREFIX + i, Type.DOUBLE, upperBound);
            unphasedState().setFromKey(LOWER_BOUND_KEY_PREFIX + i, Type.DOUBLE, lowerBound);
        }
    }

    public int getCurrentBufferLength() {
        double valueBuffer[] = getValueBuffer();
        final int dims = getInputDimensions();
        return valueBuffer.length / dims;
    }

    private double getUpperBound(int dimension) {
        Object boundObj = unphasedState().getFromKey(UPPER_BOUND_KEY_PREFIX + dimension);
        return (Double) boundObj;
    }

    private double getLowerBound(int dimension) {
        Object boundObj = unphasedState().getFromKey(LOWER_BOUND_KEY_PREFIX + dimension);
        return (Double) boundObj;
    }

    protected boolean checkValue(double value[]) {
        for (int i = 0; i < value.length; i++) {
            //A bit strange condition to make sure that NaNs are counted as anomalies
            if (!((value[i] <= getUpperBound(i)) && (value[i] >= getLowerBound(i)))) {
                return true; //Not within bounds? Anomaly
            }
        }
        return false;
    }

    protected void setInputDimensions(int dims) {
        unphasedState().setFromKey(INPUT_DIM_KEY, Type.INT, dims);
    }

    protected void addValueToBuffer(double[] value) {
        if (getInputDimensions() < 0) {
            setInputDimensions(value.length);
        }
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

    @Override
    public void learn(final Callback<Boolean> callback) {
        extractFeatures(new Callback<double[]>() {
            @Override
            public void on(double[] result) {
                boolean outcome = addValue(result);
                callback.on(outcome);
            }
        });
    }

    @Override
    public void classify(final Callback<Boolean> callback) {
        extractFeatures(new Callback<double[]>() {
            @Override
            public void on(double[] result) {
                boolean isAnomaly = checkValue(result);
                callback.on(isAnomaly);
            }
        });
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

    protected double[] getValueBuffer() {
        return unphasedState().getFromKeyWithDefault(INTERNAL_VALUE_BUFFER_KEY, new double[0]);
    }

    protected int getMaxBufferLength() {
        return unphasedState().getFromKeyWithDefault(BUFFER_SIZE_KEY, BUFFER_SIZE_DEF);
    }

    protected final void setValueBuffer(double[] valueBuffer) {
        InterquartileRangeOutlierDetectorNode.requireNotNull(valueBuffer, "value buffer must be not null");
        unphasedState().setFromKey(INTERNAL_VALUE_BUFFER_KEY, Type.DOUBLE_ARRAY, valueBuffer);
    }

    /**
     * @return Class index - index in a value array, where class label is supposed to be
     */
    protected int getInputDimensions() {
        return unphasedState().getFromKeyWithDefault(INPUT_DIM_KEY, INPUT_DIM_DEF);
    }

    @Override
    public void setProperty(String propertyName, byte propertyType, Object propertyValue) {
        if (BUFFER_SIZE_KEY.equals(propertyName)) {
            illegalArgumentIfFalse(propertyValue instanceof Integer, "Buffer size should be integer");
            illegalArgumentIfFalse((Integer) propertyValue > 0, "Buffer size should be positive");
            unphasedState().setFromKey(BUFFER_SIZE_KEY, Type.INT, propertyValue);
        } else if (INTERNAL_VALUE_BUFFER_KEY.equals(propertyName) || INPUT_DIM_KEY.equals(propertyName)) {
            //Nothing. They are unsettable directly
        } else {
            super.setProperty(propertyName, propertyType, propertyValue);
        }
    }

    @Override
    public Object get(String propertyName) {
        if (INPUT_DIM_KEY.equals(propertyName)) {
            return getInputDimensions();
        } else if (BUFFER_SIZE_KEY.equals(propertyName)) {
            return getMaxBufferLength();
        }
        return super.get(propertyName);
    }
}
