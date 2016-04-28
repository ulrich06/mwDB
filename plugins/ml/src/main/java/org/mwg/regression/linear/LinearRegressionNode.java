package org.mwg.regression.linear;

import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.Type;
import org.mwg.classifier.common.SlidingWindowManagingNode;
import org.mwg.plugin.AbstractNode;
import org.mwg.plugin.NodeState;

import java.util.Arrays;
import java.util.Objects;

/**
 * Created by andre on 4/26/2016.
 */
public class LinearRegressionNode extends SlidingWindowManagingNode implements KLinearRegression {

    /**
     * Attribute key - sliding window of values
     */
    private static final String INTERNAL_VALUE_COEFFICIENTS_KEY = "_regressionCoefficients";

    public LinearRegressionNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    private double[] getCoefficients(){
        Object objCoefBuffer = currentState.get(_resolver.stringToLongKey(INTERNAL_VALUE_COEFFICIENTS_KEY));
        if (objCoefBuffer == null) {
            double emptyCoefBuffer[] = new double[0];
            currentState.set(_resolver.stringToLongKey(INTERNAL_VALUE_COEFFICIENTS_KEY), Type.DOUBLE_ARRAY, emptyCoefBuffer); //Value buffer, starts empty
            return emptyCoefBuffer;
        }
        return (double[]) objCoefBuffer;
    }

    private void setCoefficients(double[] coefficients) {
        Objects.requireNonNull(coefficients,"Regression coefficients must be not null");
        currentState.set(_resolver.stringToLongKey(INTERNAL_VALUE_COEFFICIENTS_KEY), Type.DOUBLE_ARRAY, coefficients);
    }

    @Override
    protected void setBootstrapModeHook() {
        //TODO What should we do when bootstrap mode is approaching?
    }

    @Override
    protected void updateModelParameters(double[] value) {
        //TODO Step 1. Compose matrix X and vector Y (KMatrix).
        //TODO Don't forget intercept - it is at response index, so corresponding column should be 1

        // inv(Xt * X) * Xt * y
        //TODO use pseudoinverse right away? What if we have too few points?
    }

    @Override
    public double getBufferError() {
        //TODO What about intercept?

        //For each value in value buffer
        int startIndex = 0;
        final int dims = getInputDimensions();

        double valueBuffer[] = getValueBuffer();
        final int numValues = valueBuffer.length / dims;//TODO What if there are not enough values?
        if (numValues == 0) {
            return 0;
        }

        double coefficients[] = getCoefficients();

        final int responseIndex = getResponseIndex();
        double sqrResidualSum = 0;
        while (startIndex + dims < valueBuffer.length) { //For each value
            double curValue[] = Arrays.copyOfRange(valueBuffer, startIndex, startIndex + dims);
            double response = 0;
            for (int i=0;i<curValue.length;i++){
                if (i!=responseIndex){
                    response += coefficients[i]*curValue[i];
                }else{
                    //Acts as intercept
                    response += coefficients[i];
                }
            }
            sqrResidualSum += (response - curValue[responseIndex])*(response - curValue[responseIndex]);

            //Continue the loop
            startIndex += dims;
        }
        return sqrResidualSum / valueBuffer.length;
    }
}
