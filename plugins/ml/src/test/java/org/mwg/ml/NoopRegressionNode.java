package org.mwg.ml;

import org.mwg.Callback;
import org.mwg.Graph;

public class NoopRegressionNode extends AbstractMLNode implements RegressionNode {

    public NoopRegressionNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    @Override
    public void learn(double output, Callback<Boolean> callback) {
        extractFeatures(new Callback<double[]>() {
            @Override
            public void on(double[] result) {
                set("extracted", result);
                if (callback != null) {
                    callback.on(true);
                }
            }
        });


    }

    @Override
    public void extrapolate(Callback<Double> callback) {

    }

}
