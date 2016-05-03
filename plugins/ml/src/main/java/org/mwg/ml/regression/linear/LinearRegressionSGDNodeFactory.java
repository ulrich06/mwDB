package org.mwg.ml.regression.linear;

import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.plugin.NodeFactory;

public class LinearRegressionSGDNodeFactory implements NodeFactory {
    @Override
    public String name() {
        return "LinearRegressionSGDNode";
    }

    @Override
    public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
        LinearRegressionSGDNode newNode = new LinearRegressionSGDNode(world, time, id, graph, initialResolution);
        return newNode;
    }
}
