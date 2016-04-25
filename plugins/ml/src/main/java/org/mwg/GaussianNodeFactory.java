package org.mwg;

import org.mwg.gmm.GaussianNode;
import org.mwg.plugin.NodeFactory;

public class GaussianNodeFactory implements NodeFactory {
    @Override
    public String name() {
        return "GaussianNode";
    }

    @Override
    public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
        return new GaussianNode(world, time, id, graph, initialResolution);
    }
}
