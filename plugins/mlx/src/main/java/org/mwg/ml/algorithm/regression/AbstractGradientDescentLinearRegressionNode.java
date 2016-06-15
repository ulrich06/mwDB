package org.mwg.ml.algorithm.regression;

import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.Type;
import org.mwg.ml.common.matrix.Matrix;
import org.mwg.ml.common.matrix.TransposeType;
import org.mwg.ml.common.matrix.operation.PInvSVD;
import org.mwg.plugin.NodeFactory;

/**
 * Created by andre on 4/29/2016.
 */
public abstract class AbstractGradientDescentLinearRegressionNode extends AbstractLinearRegressionNode{

    public static final String GD_ERROR_THRESH_KEY = "gdErrorThreshold";
    public static final String GD_ITERATION_THRESH_KEY = "gdIterationThreshold";

    public static final int DEFAULT_GD_ITERATIONS_COUNT = 10000;

    public static final double DEFAULT_LEARNING_RATE = 0.0001;

    /**
     * Attribute key - Learning rate
     */
    protected static final String INTERNAL_VALUE_LEARNING_RATE_KEY = "_LearningRate";

    public AbstractGradientDescentLinearRegressionNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    public double getLearningRate(){
        return unphasedState().getFromKeyWithDefault(INTERNAL_VALUE_LEARNING_RATE_KEY, DEFAULT_LEARNING_RATE);
    }

    public void setLearningRate(double newLearningRate){
        illegalArgumentIfFalse(newLearningRate > 0, "Learning rate should be positive");
        unphasedState().setFromKey(INTERNAL_VALUE_LEARNING_RATE_KEY, Type.DOUBLE, newLearningRate);
    }

    @Override
    public void setProperty(String propertyName, byte propertyType, Object propertyValue) {
        if (GD_ERROR_THRESH_KEY.equals(propertyName)){
            setIterationErrorThreshold((Double)propertyValue);
        }else if (GD_ITERATION_THRESH_KEY.equals(propertyName)){
            setIterationCountThreshold((Integer)propertyValue);
        }else if (INTERNAL_VALUE_LEARNING_RATE_KEY.equals(propertyName)){
            setLearningRate((Double)propertyValue);
        }else{
            super.setProperty(propertyName, propertyType, propertyValue);
        }
    }

    public double getIterationErrorThreshold() {
        return unphasedState().getFromKeyWithDefault(GD_ERROR_THRESH_KEY, Double.NaN);
    }

    public void setIterationErrorThreshold(double errorThreshold) {
        unphasedState().setFromKey(GD_ERROR_THRESH_KEY, Type.DOUBLE, errorThreshold);
    }

    public void removeIterationErrorThreshold() {
        unphasedState().setFromKey(GD_ERROR_THRESH_KEY, Type.DOUBLE, Double.NaN);
    }

    public int getIterationCountThreshold() {
        return unphasedState().getFromKeyWithDefault(GD_ITERATION_THRESH_KEY, DEFAULT_GD_ITERATIONS_COUNT);
    }

    public void setIterationCountThreshold(int iterationCountThreshold) {
        //Any value is acceptable.
        unphasedState().setFromKey(GD_ITERATION_THRESH_KEY, Type.INT, iterationCountThreshold);
    }

    public void removeIterationCountThreshold() {
        unphasedState().setFromKey(GD_ITERATION_THRESH_KEY, Type.INT, -1);
    }

    /**
     * Created by andre on 4/26/2016.
     */
    public static class LinearRegressionNode extends AbstractLinearRegressionNode {

        public static final String NAME = "LinearRegressionBatch";

        public static class Factory implements NodeFactory {
            @Override
            public String name() {
                return NAME;
            }

            @Override
            public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
                org.mwg.ml.algorithm.regression.LinearRegressionNode newNode = new org.mwg.ml.algorithm.regression.LinearRegressionNode(world, time, id, graph, initialResolution);
                return newNode;
            }
        }

        public LinearRegressionNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
            super(p_world, p_time, p_id, p_graph, currentResolution);
        }

        @Override
        protected void updateModelParameters(double[] value, double response) {
            int dims = getInputDimensions();
            if (dims==INPUT_DIM_UNKNOWN){
                dims = value.length;
                setInputDimensions(dims);
            }
            final int bufferLength = getCurrentBufferLength();
            //Value should be already added to buffer by that time
            final double currentBuffer[] = getValueBuffer();
            //Adding place for intercepts
            final double reshapedValue[] = new double[currentBuffer.length + bufferLength];

            final double y[] = getResultBuffer();

            final double l2 = getL2Regularization();

            //Step 1. Re-arrange to column-based format.
            for (int i=0;i<bufferLength;i++){
                reshapedValue[i] = 1; //First column is 1 (for intercepts)
            }
            for (int i=0;i<bufferLength;i++){
                for (int j=0;j<dims;j++){
                    reshapedValue[(j+1)*bufferLength+i] = currentBuffer[i*dims+j];
                }
            }

            Matrix xMatrix = new Matrix(reshapedValue, bufferLength, dims+1);
            Matrix yVector = new Matrix(y, bufferLength, 1);

            // inv(Xt * X - lambda*I) * Xt * ys
            // I - almost identity, but with 0 for intercept term
            Matrix xtMulX = Matrix.multiplyTranspose
                    (TransposeType.TRANSPOSE, xMatrix, TransposeType.NOTRANSPOSE, xMatrix);

            for (int i=1;i<=dims;i++){
                xtMulX.add(i,i,l2);
            }

            PInvSVD pinvsvd = new PInvSVD();
            pinvsvd.factor(xtMulX,false);
            Matrix pinv=pinvsvd.getPInv();

            Matrix invMulXt = Matrix.multiplyTranspose
                    (TransposeType.NOTRANSPOSE, pinv, TransposeType.TRANSPOSE, xMatrix);

            Matrix result = Matrix.multiplyTranspose
                    (TransposeType.NOTRANSPOSE, invMulXt, TransposeType.NOTRANSPOSE, yVector);

            final double newCoefficients[] = new double[dims];
            for (int i=0;i<dims;i++){
                newCoefficients[i] = result.get(i+1, 0);
            }
            setIntercept(result.get(0, 0));
            setCoefficients(newCoefficients);
        }
    }
}
