package org.mwg.ml.algorithm.regression;

import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.Type;
import org.mwg.plugin.NodeFactory;

import java.util.Arrays;

/**
 * Linear regression node based on stochastic gradient descent.
 * Likelz to be much faster than non-SGD, but might take longer time to converge.
 *
 * Created by andre on 4/29/2016.
 */
public class LinearRegressionBatchGDNode extends AbstractGradientDescentLinearRegressionNode {

    public static final String NAME = "LinearRegressionBatchGradientDescent";

    public static class Factory implements NodeFactory {
        @Override
        public String name() {
            return NAME;
        }

        @Override
        public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
            LinearRegressionBatchGDNode newNode = new LinearRegressionBatchGDNode(world, time, id, graph, initialResolution);
            return newNode;
        }
    }

    public LinearRegressionBatchGDNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    @Override
    protected void updateModelParameters(double value[], double result) {
        //Value should be already added to buffer by that time
        int dims = getInputDimensions();
        if (dims==INPUT_DIM_UNKNOWN){
            dims = value.length;
            setInputDimensions(dims);
        }
        final double alpha = getLearningRate();
        final double lambda = getL2Regularization();
        final int bufferLength = getCurrentBufferLength();

        final double valueBuffer[] = getValueBuffer();
        final double resultBuffer[] = getResultBuffer();

        final double gdErrorThresh = getIterationErrorThreshold();
        final int gdIterThresh = getIterationCountThreshold();

        //Get coefficients. If they are of length 0, initialize with random.
        double coefs[] = getCoefficients();
        double intercept = getIntercept();
        if (coefs.length==0){
            coefs = new double[dims];
            setCoefficients(coefs);
        }

        //For batch gradient descent:
        //Theta_j = theta_j - alpha * (1/m * sum( h(X_i) - y_i )*X_j_i - lambda/m * theta_j)

        int iterCount = 0;
        boolean exitCase = false;
        double prevError = getBufferError();
        while (!exitCase){
            iterCount++;

            int startIndex = 0;
            int index = 0;
            double oldCoefs[] = Arrays.copyOf(coefs, coefs.length);
            while (startIndex + dims <= valueBuffer.length){
                double curValue[] = Arrays.copyOfRange(valueBuffer, startIndex, startIndex + dims);

                double h = 0;
                for (int j=0;j<dims;j++){
                    h += oldCoefs[j]*curValue[j];
                }
                h += intercept;

                double outcome = resultBuffer[index];
                for (int j=0;j<dims;j++){
                    coefs[j] -= alpha * ( ( h - outcome)*curValue[j])/bufferLength;
                }
                intercept -= alpha * ( h - outcome) / bufferLength;

                startIndex += dims;
                index++;
            }
            for (int j=0;j<dims;j++){
                coefs[j] += alpha * lambda * oldCoefs[j];
            }

            setCoefficients(coefs);
            setIntercept(intercept);
            if (gdErrorThresh>0){
                double newError = getBufferError();
                exitCase |= (prevError-newError)<gdErrorThresh;
                prevError = newError;
            }

            if (gdIterThresh>0){
                exitCase |= iterCount>=gdIterThresh;
            }

            if ((!(gdErrorThresh>0))&&(!(gdIterThresh>0))) {
                //Protection against infinite loops. If neither error threshold, nor iteration thresholds are used,
                //run loops once, do not go infinite.
                exitCase = true;
            }
        }
    }


}

