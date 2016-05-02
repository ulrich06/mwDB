package org.mwg.regression.linear;

import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.plugin.NodeFactory;

public class LinearRegressionBatchGDNodeFactory implements NodeFactory {
    @Override
    public String name() {
        return "LinearRegressionBatchGDNode";
    }

    @Override
    public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
        LinearRegressionBatchGDNode newNode = new LinearRegressionBatchGDNode(world, time, id, graph, initialResolution);
        return newNode;
    }
}
