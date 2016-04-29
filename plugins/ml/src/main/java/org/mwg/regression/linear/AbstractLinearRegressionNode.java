package org.mwg.regression.linear;

import org.mwg.Graph;
import org.mwg.Type;
import org.mwg.classifier.common.SlidingWindowManagingNode;

import java.util.Arrays;
import java.util.Objects;

/**
 * Created by andre on 4/29/2016.
 */
public abstract class AbstractLinearRegressionNode extends SlidingWindowManagingNode implements KLinearRegression {

    /**
     * Attribute key - sliding window of values
     */
    protected static final String INTERNAL_VALUE_COEFFICIENTS_KEY = "_regressionCoefficients";

    /**
     * Attribute key - L2 regularization coefficient
     */
    protected static final String INTERNAL_VALUE_L2_COEF_KEY = "_L2Coefficient";

    public AbstractLinearRegressionNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    @Override
    public double[] getCoefficients(){
        return unphasedState().getFromKeyWithDefault(INTERNAL_VALUE_COEFFICIENTS_KEY, new double[0]);
    }

    @Override
    public double getIntercept(){
        return getCoefficients()[getResponseIndex()];
    }

    protected void setCoefficients(double[] coefficients) {
        Objects.requireNonNull(coefficients,"Regression coefficients must be not null");
        unphasedState().setFromKey(INTERNAL_VALUE_COEFFICIENTS_KEY, Type.DOUBLE_ARRAY, coefficients);
    }

    @Override
    public double getL2Regularization(){
        return unphasedState().getFromKeyWithDefault(INTERNAL_VALUE_L2_COEF_KEY, 0.0);
    }

    @Override
    public void setL2Regularization(double l2) {
        illegalArgumentIfFalse(l2>=0,"L2 coefficients must be non-negative");
        unphasedState().setFromKey(INTERNAL_VALUE_L2_COEF_KEY, Type.DOUBLE, l2);
    }

    @Override
    protected void setBootstrapModeHook() {
        //What should we do when bootstrap mode is approaching?
        //TODO Nothing?
    }

    @Override
    public double getBufferError() {
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
        while (startIndex + dims <= valueBuffer.length) { //For each value
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
        return sqrResidualSum / numValues;
    }
}
