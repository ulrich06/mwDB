package org.mwg.regression.linear;

import org.mwg.Graph;
import org.mwg.Type;
import org.mwg.classifier.common.SlidingWindowManagingNode;
import org.mwg.util.matrix.KMatrix;
import org.mwg.util.matrix.KTransposeType;
import org.mwg.util.matrix.operation.PInvSVD;

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

    @Override
    public double[] getCoefficients(){
        return unphasedState().getFromKeyWithDefault(INTERNAL_VALUE_COEFFICIENTS_KEY, new double[0]);
    }

    private void setCoefficients(double[] coefficients) {
        Objects.requireNonNull(coefficients,"Regression coefficients must be not null");
        unphasedState().setFromKey(INTERNAL_VALUE_COEFFICIENTS_KEY, Type.DOUBLE_ARRAY, coefficients);
    }

    @Override
    protected void setBootstrapModeHook() {
        //What should we do when bootstrap mode is approaching?
        //TODO Nothing?
    }

    @Override
    protected void updateModelParameters(double[] value) {
        //Value should be already added to buffer by that time
        final double currentBuffer[] = getValueBuffer();
        final double reshapedValue[] = new double[currentBuffer.length];
        final int dims = getInputDimensions();
        final int bufferLength = getCurrentBufferLength();
        final int respIndex = getResponseIndex();

        final double y[] = new double[bufferLength];

        //Step 1. Re-arrange to column-based format.
        for (int i=0;i<bufferLength;i++){
            for (int j=0;j<dims;j++){
                //Intercept goes instead of response value of the matrix
                if (j==respIndex){
                    reshapedValue[j*bufferLength+i] = 1;
                    y[i] = currentBuffer[i*dims+j];
                }else{
                    reshapedValue[j*bufferLength+i] = currentBuffer[i*dims+j];
                }
            }
        }

        KMatrix xMatrix = new KMatrix(reshapedValue, bufferLength, dims);
        KMatrix yVector = new KMatrix(y, bufferLength, 1);

        // inv(Xt * X) * Xt * ys
        KMatrix xtMulX = KMatrix.multiplyTransposeAlphaBeta
                (KTransposeType.TRANSPOSE, 1, xMatrix, KTransposeType.NOTRANSPOSE, 0, xMatrix);

        PInvSVD pinvsvd = new PInvSVD();
        pinvsvd.factor(xtMulX,false);
        KMatrix pinv=pinvsvd.getPInv();

        KMatrix invMulXt = KMatrix.multiplyTransposeAlphaBeta
                (KTransposeType.NOTRANSPOSE, 1, pinv, KTransposeType.TRANSPOSE, 0, xMatrix);

        KMatrix result = KMatrix.multiplyTransposeAlphaBeta
                (KTransposeType.NOTRANSPOSE, 1, invMulXt, KTransposeType.NOTRANSPOSE, 0, yVector);

        final double newCoefficients[] = new double[dims];
        for (int i=0;i<dims;i++){
            newCoefficients[i] = result.get(i, 0);
        }
        setCoefficients(newCoefficients);
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
        return sqrResidualSum / valueBuffer.length;
    }
}
