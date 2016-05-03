package org.mwg.ml.regression;

import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.plugin.NodeFactory;

/**
 * Linear regression node based on stochastic gradient descent.
 * Likelz to be much faster than non-SGD, but might take longer time to converge.
 *
 * Created by andre on 4/29/2016.
 */
public class MLLinearRegressionSGDNode extends AbstractGradientDescentLinearRegressionNode {

    public static final String NAME = "LinearRegressionStochasticGradientDescent";

    public static class Factory implements NodeFactory {
        @Override
        public String name() {
            return NAME;
        }

        @Override
        public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
            MLLinearRegressionSGDNode newNode = new MLLinearRegressionSGDNode(world, time, id, graph, initialResolution);
            return newNode;
        }
    }

    public MLLinearRegressionSGDNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    @Override
    protected void updateModelParameters(double[] value) {
        //Value should be already added to buffer by that time
        final int dims = getInputDimensions();
        final int respIndex = getResponseIndex();
        final double alpha = getLearningRate();
        double lambda = getL2Regularization();

        //Get coefficients. If they are of length 0, initialize with random.
        double coefs[] = getCoefficients();
        if (coefs.length==0){
            coefs = new double[dims];
        }

        //For batch gradient descent:
        //Theta_j = theta_j - alpha * (1/m * sum( h(X_i) - y_i )*X_j_i - lambda/m * theta_j)

        double h = 0;
        for (int j=0;j<dims;j++){
            h += coefs[j]*( (j!=respIndex)?value[j]:1 );
        }

        //For stochastic gradient descent:
        //Theta_j = theta_j - alpha * ( (h(X_i) - y_i )*X_j - lambda * theta_j)
        for (int j=0;j<dims;j++){
            if (j!=respIndex){
                coefs[j] -= alpha * ( ( h - value[respIndex] )*value[j] - lambda * coefs[j]);
            }else{
                //Intercept: value is 1, L2 reg-n not used.
                coefs[j] -= alpha * ( h - value[respIndex] );
            }
        }
        setCoefficients(coefs);
    }

}

