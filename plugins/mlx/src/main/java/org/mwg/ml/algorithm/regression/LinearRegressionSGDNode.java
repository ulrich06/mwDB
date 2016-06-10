package org.mwg.ml.algorithm.regression;

import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.plugin.NodeFactory;

/**
 * Linear regression node based on stochastic gradient descent.
 * Likelz to be much faster than non-SGD, but might take longer time to converge.
 *
 * Created by andre on 4/29/2016.
 */
public class LinearRegressionSGDNode extends AbstractGradientDescentLinearRegressionNode {

    public static final String NAME = "LinearRegressionStochasticGradientDescent";

    public static class Factory implements NodeFactory {
        @Override
        public String name() {
            return NAME;
        }

        @Override
        public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
            LinearRegressionSGDNode newNode = new LinearRegressionSGDNode(world, time, id, graph, initialResolution);
            return newNode;
        }
    }

    public LinearRegressionSGDNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    //We don't need large buffer. Gradient algorithm uses sliding window only for evaluations.
    //Therefore, we have to keep the window size even in bootstrap mode
    @Override
    protected void addValueBootstrap(double value[], double result){
        //-1 because we will add 1 value to the buffer later.
        while (getCurrentBufferLength() > (getMaxBufferLength()-1)) {
            removeFirstValueFromBuffer();
        }
        super.addValueBootstrap(value, result);
    }

    @Override
    protected void updateModelParameters(double[] value, double response) {
        //Value should be already added to buffer by that time
        int dims = getInputDimensions();
        if (dims==INPUT_DIM_UNKNOWN){
            dims = value.length;
            setInputDimensions(dims);
        }
        final double alpha = getLearningRate();
        double lambda = getL2Regularization();

        //Get coefficients. If they are of length 0, initialize with random.
        double coefs[] = getCoefficients();
        double intercept = getIntercept();
        if (coefs.length==0){
            coefs = new double[dims];
        }

        //For batch gradient descent:
        //Theta_j = theta_j - alpha * (1/m * sum( h(X_i) - y_i )*X_j_i - lambda/m * theta_j)

        double h = 0;
        for (int j=0;j<dims;j++){
            h += coefs[j]*value[j];
        }
        h += intercept;

        //For stochastic gradient descent:
        //Theta_j = theta_j - alpha * ( (h(X_i) - y_i )*X_j - lambda * theta_j)
        for (int j=0;j<dims;j++){
            coefs[j] -= alpha * ( ( h - response)*value[j] - lambda * coefs[j]);
        }
        //Intercept: value is 1, L2 reg-n not used.
        intercept -= alpha * ( h - response);
        setCoefficients(coefs);
        setIntercept(intercept);
    }

}

