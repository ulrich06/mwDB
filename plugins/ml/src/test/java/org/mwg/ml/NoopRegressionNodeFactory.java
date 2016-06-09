package org.mwg.ml;

import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.plugin.NodeFactory;

public class NoopRegressionNodeFactory implements NodeFactory {
    @Override
    public String name() {
        return "NoopRegressionNode";
    }

    @Override
    public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
        return new NoopRegressionNode(world, time, id, graph, initialResolution);
    }

}
