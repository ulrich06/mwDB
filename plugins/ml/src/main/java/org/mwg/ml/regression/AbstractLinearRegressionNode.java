package org.mwg.ml.regression;

import org.mwg.Graph;
import org.mwg.Type;
import org.mwg.ml.classifier.AbstractSlidingWindowManagingNode;

import java.util.Arrays;
import java.util.Objects;

/**
 * Created by andre on 4/29/2016.
 */
public abstract class AbstractLinearRegressionNode extends AbstractSlidingWindowManagingNode {

    /**
     * Regression coefficients
     */
    public static final String COEFFICIENTS_KEY = "regressionCoefficients";
    /**
     * Regression coefficients - default
     */
    public static final double[] COEFFICIENTS_DEF = new double[0];
    /**
     * Regression intercept
     */
    public static final String INTERCEPT_KEY = "regressionCoefficients";
    /**
     * Regression intercept - default
     */
    public static final double INTERCEPT_DEF = 0.0;

    /**
     * L2 regularization coefficient
     */
    public static final String L2_COEF_KEY = "L2Coefficient";
    /**
     * L2 regularization coefficient - default
     */
    public static final double L2_COEF_DEF = 0.0;

    public AbstractLinearRegressionNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    public double[] getCoefficients(){
        return unphasedState().getFromKeyWithDefault(COEFFICIENTS_KEY, COEFFICIENTS_DEF);
    }

    public double getIntercept(){
        final double[] allCoefs =  getCoefficients();
        final int respIndex = getResponseIndex();
        if ((allCoefs!=null)&&(allCoefs.length > respIndex)){
            return allCoefs[respIndex];
        }
        return INTERCEPT_DEF;
    }

    @Override
    public void setProperty(String propertyName, byte propertyType, Object propertyValue) {
        if (L2_COEF_KEY.equals(propertyName)) {
            illegalArgumentIfFalse( (propertyValue instanceof Double)||(propertyValue instanceof Integer),
                    "L2 regularization coefficient should be of type double or integer");
            if (propertyValue instanceof Double){
                illegalArgumentIfFalse((Double)propertyValue >= 0, "L2 regularization coefficient should be non-negative");
                setL2Regularization((Double)propertyValue);
            }else{
                illegalArgumentIfFalse((Integer)propertyValue >= 0, "L2 regularization coefficient should be non-negative");
                setL2Regularization(((Integer)propertyValue).doubleValue());
            }
        }else if (COEFFICIENTS_KEY.equals(propertyName) || INTERCEPT_KEY.equals(propertyName)) {
            //Nothing. Those cannot be set.
        }else{
            super.setProperty(propertyName, propertyType, propertyValue);
        }
    }

    @Override
    public Object get(String propertyName){
        if(COEFFICIENTS_KEY.equals(propertyName)){
            return getCoefficients();
        }else if(INTERCEPT_KEY.equals(propertyName)){
            return getIntercept();
        }else if(L2_COEF_KEY.equals(propertyName)){
            return getL2Regularization();
        }
        return super.get(propertyName);
    }

    protected void setCoefficients(double[] coefficients) {
        Objects.requireNonNull(coefficients,"Regression coefficients must be not null");
        unphasedState().setFromKey(COEFFICIENTS_KEY, Type.DOUBLE_ARRAY, coefficients);
    }

    public double getL2Regularization(){
        return unphasedState().getFromKeyWithDefault(L2_COEF_KEY, L2_COEF_DEF);
    }

    public void setL2Regularization(double l2) {
        illegalArgumentIfFalse(l2>=0,"L2 coefficients must be non-negative");
        unphasedState().setFromKey(L2_COEF_KEY, Type.DOUBLE, l2);
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
