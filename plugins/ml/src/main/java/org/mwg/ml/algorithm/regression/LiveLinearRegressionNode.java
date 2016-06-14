package org.mwg.ml.algorithm.regression;

import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.Type;
import org.mwg.ml.AbstractMLNode;
import org.mwg.ml.RegressionNode;
import org.mwg.plugin.NodeFactory;
import org.mwg.plugin.NodeState;

/**
 * Created by assaad on 14/06/16.
 */
public class LiveLinearRegressionNode extends AbstractMLNode implements RegressionNode {

    public static final String ALPHA_KEY = "ALPHA"; //learning rate
    public static final double ALPHA_DEF = 0.01;

    public static final String LAMBDA_KEY = "LAMBDA"; //regularization rate
    public static final double LAMBDA_DEF = 0.001;

    public static final String ITERATION_KEY = "ITERATION"; //Number of iterations on each values
    public static final int ITERATION_DEF = 5;

    public static final String THRESHOLD_KEY = "THRESHOLD"; //Number of iterations on each values
    public static final double THRESHOLD_DEF = 0.01;


    private static final String INTERNAL_TOTAL_KEY="TOTAL_KEY";
    private static final String INTERNAL_WEIGHT_KEY = "_weight";
    private static final String INTERNAL_WEIGHT_BACKUP_KEY = "_weightbckup";

    private static final String MISMATCH_MSG = "Different Imput lengths are not supported";



    //Name of the algorithm to be used in the meta model
    public final static String NAME = "LiveLinearRegression";

    //Factory of the class integrated
    public static class Factory implements NodeFactory {

        @Override
        public String name() {
            return NAME;
        }

        @Override
        public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
            return new LiveLinearRegressionNode(world, time, id, graph, initialResolution);
        }
    }


    public LiveLinearRegressionNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    @Override
    public void learn(double output, Callback<Boolean> callback) {
        extractFeatures(new Callback<double[]>() {
            @Override
            public void on(double[] input) {
                internalLearn(input, output, callback);
            }
        });

    }

    public void internalLearn(double[] input, double output, Callback<Boolean> callback) {
        NodeState state = this._resolver.resolveState(this, true);
        int iterations = state.getFromKeyWithDefault(ITERATION_KEY, ITERATION_DEF);
        double alpha = state.getFromKeyWithDefault(ALPHA_KEY, ALPHA_DEF);
        double lambda = state.getFromKeyWithDefault(LAMBDA_KEY, LAMBDA_DEF);
        double[] weights = (double[]) state.getFromKey(INTERNAL_WEIGHT_KEY);

        if (weights == null) {
            weights = new double[input.length + 1];
        }
        if (weights.length != (input.length + 1)) {
            throw new RuntimeException(MISMATCH_MSG);
        }
        int featuresize = input.length;

        for (int j = 0; j < iterations; j++) {
            double h = calculate(weights,input);
            double err = alpha * (h - output);
            for (int i = 0; i < featuresize; i++) {
                weights[i] = weights[i] - alpha * ((h - output) * input[j] - lambda * weights[j]);
            }
            weights[featuresize] = weights[featuresize] - err;
        }

        double[] bckupWeight = (double[]) state.getFromKey(INTERNAL_WEIGHT_BACKUP_KEY);
        if(bckupWeight==null){
            state.setFromKey(INTERNAL_WEIGHT_KEY, Type.DOUBLE_ARRAY,weights);
            state.setFromKey(INTERNAL_WEIGHT_BACKUP_KEY, Type.DOUBLE_ARRAY,weights);
            state.setFromKey(INTERNAL_TOTAL_KEY,Type.INT,1);
        }
        else{
            double diff=0;
            for(int i=0;i<weights.length;i++){
                diff=Math.max(diff,Math.abs(weights[i]-bckupWeight[i]));
            }
            double deviation=state.getFromKeyWithDefault(THRESHOLD_KEY,THRESHOLD_DEF);
            if(diff>deviation){
                state= newState(time());
                state.setFromKey(INTERNAL_WEIGHT_KEY, Type.DOUBLE_ARRAY,weights);
                state.setFromKey(INTERNAL_WEIGHT_BACKUP_KEY, Type.DOUBLE_ARRAY,weights);
                state.setFromKey(INTERNAL_TOTAL_KEY,Type.INT,1);
            }
            else {
                state.setFromKey(INTERNAL_WEIGHT_KEY, Type.DOUBLE_ARRAY,weights);
                state.setFromKey(INTERNAL_TOTAL_KEY,Type.INT, (int)state.getFromKey(INTERNAL_TOTAL_KEY)+1);
            }
        }
    }

    private double calculate(double[] weights, double[] input) {
        double h = 0;
        for (int j=0;j<input.length;j++){
            h += weights[j]*input[j];
        }
        h+=weights[input.length];
        return h;
    }

    @Override
    public void extrapolate(Callback<Double> callback) {
        NodeState state = this._resolver.resolveState(this, true);
        final double[] weights = (double[]) state.getFromKey(INTERNAL_WEIGHT_KEY);
        if(weights==null){
            if(callback!=null){
                callback.on(0.0);
            }
        }
        else {
            extractFeatures(new Callback<double[]>() {
                @Override
                public void on(double[] input) {
                    if(input.length!=weights.length-1){
                        throw new RuntimeException(MISMATCH_MSG);
                    }
                    else if(callback!=null){
                        callback.on(calculate(weights,input));
                    }
                }
            });
        }
    }
}
