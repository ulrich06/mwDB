package org.mwg.ml.common;

import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.Type;
import org.mwg.ml.RegressionNode;

import java.util.Arrays;
import java.util.Objects;

/**
 * Created by andre on 5/4/2016.
 */
public abstract class AbstractRegressionSlidingWindowManagingNode extends AbstractSlidingWindowManagingNode implements RegressionNode {

    protected static final double[] INTERNAL_RESULTS_BUFFER_DEF = new double[0];

    public AbstractRegressionSlidingWindowManagingNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    protected abstract double predictValue(double value[]);

    public double[] getResultBuffer() {
        return unphasedState().getFromKeyWithDefault(INTERNAL_RESULTS_BUFFER_KEY, INTERNAL_RESULTS_BUFFER_DEF);
    }

    /**
     * Adds new value to the buffer. Connotations change depending on whether the node is in bootstrap mode or not.
     *
     * @param value New value to add; {@code null} disallowed
     */
    public boolean addValue(double value[], double result) {
        illegalArgumentIfFalse(value != null, "Value must be not null");

        if (isInBootstrapMode()) {
            addValueBootstrap(value, result);
        } else {
            addValueNoBootstrap(value, result);
        }
        return isInBootstrapMode(); //Can change since last time
    }

    protected void addValueToBuffer(double[] value, double result) {
        double valueBuffer[] = getValueBuffer();
        double resultBuffer[] = getResultBuffer();
        double newBuffer[] = new double[valueBuffer.length + value.length];
        double newResultBuffer[] = new double[resultBuffer.length + 1];
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

    protected final void setResultBuffer(double[] resBuffer) {
        Objects.requireNonNull(resBuffer,"result buffer must be not null");
        unphasedState().setFromKey(INTERNAL_RESULTS_BUFFER_KEY, Type.DOUBLE_ARRAY, resBuffer);
    }

    protected void addValueNoBootstrap(double value[], double result) {
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

    @Override
    protected void removeFirstValueFromResultBuffer(){
        double resultBuffer[] = getResultBuffer();
        if (resultBuffer.length == 0) {
            return;
        }
        double newResultBuffer[] = Arrays.copyOfRange(resultBuffer, 1, resultBuffer.length-1);
        setResultBuffer(newResultBuffer);
    }

    /**
     * Adds new value to the buffer. Gaussian model is regenerated.
     *
     * @param value New value to add; {@code null} disallowed
     */
    private void addValueBootstrap(double value[], double result) {
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
    public void learn(double output, Callback<Boolean> callback){
        extractFeatures(new Callback<double[]>(){
            @Override
            public void on(double[] result) {
                boolean outcome = addValue(result, output);
                callback.on(outcome);
            }
        });
    }


    /**
     *
     * @param value
     * @param outcome
     */
    protected abstract void updateModelParameters(double value[], double outcome);

    public void extrapolate(Callback<Double> callback){
        extractFeatures(new Callback<double[]>() {
            @Override
            public void on(double[] result) {
                double outcome = predictValue(result);
                callback.on(outcome);
            }
        });
    }
}
