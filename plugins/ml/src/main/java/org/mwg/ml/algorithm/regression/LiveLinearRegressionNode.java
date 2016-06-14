package org.mwg.ml.algorithm.regression;

import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.ml.AbstractMLNode;
import org.mwg.ml.RegressionNode;
import org.mwg.plugin.NodeFactory;
import org.mwg.plugin.NodeState;

/**
 * Created by assaad on 14/06/16.
 */
public class LiveLinearRegressionNode extends AbstractMLNode implements RegressionNode  {

    public static final String ALPHA_KEY = "ALPHA"; //tolerated Error to specify by the signal
    public static final double ALPHA_DEF = 0.01;

    public static final String ITERATION_KEY = "ITERATION"; //tolerated Error to specify by the signal
    public static final int ITERATION_DEF = 5;

    private static final String INTERNAL_WEIGHT_KEY = "_weight";
    private static final String INTERNAL_WEIGHT_BACKUP_KEY = "_weightbckup";
    private static final String INTERNAL_NB_PAST_KEY = "_nb";


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
                internalLearn(input,output,callback);
            }
        });

    }

    public void internalLearn(double[] input, double output, Callback<Boolean> callback){
        NodeState state = this._resolver.resolveState(this, true);
        int iteration=state.getFromKeyWithDefault(ITERATION_KEY,ITERATION_DEF);
        double alpha =state.getFromKeyWithDefault(ALPHA_KEY,ALPHA_DEF);
        double [] weights=(double []) state.getFromKey(INTERNAL_WEIGHT_KEY);
        if(weights==null){
            weights=new double[input.length+1];
        }
        if(weights.length!=(input.length+1)){
            throw new RuntimeException("Different weight length is not supported");
        }
        int featuresize=input.length;

        for(int j=0; j<iteration;j++) {
        /*    double h = calculate(input);
            double err = -alpha * (h - output);
            for (int i = 0; i < featuresize; i++) {
                weights[i] = weights[i] + err * input[i];
            }
            weights[featuresize] = weights[featuresize] + err; */
        }
    }

    @Override
    public void extrapolate(Callback<Double> callback) {

    }
}
