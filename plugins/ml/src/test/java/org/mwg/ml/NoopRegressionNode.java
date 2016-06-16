package org.mwg.ml;

import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.plugin.NodeFactory;

public class NoopRegressionNode extends AbstractMLNode implements RegressionNode {

    public static class NoopRegressionNodeFactory implements NodeFactory {
        @Override
        public String name() {
            return "NoopRegressionNode";
        }

        @Override
        public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
            return new NoopRegressionNode(world, time, id, graph, initialResolution);
        }

    }

    public NoopRegressionNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    @Override
    public void learn(double output, final Callback<Boolean> callback) {
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
