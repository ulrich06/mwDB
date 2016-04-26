package org.mwg.classifier.gaussian;

import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.plugin.NodeFactory;

public class GaussianClassifierNodeFactory implements NodeFactory {
    @Override
    public String name() {
        return "GaussianClassifierNode";
    }

    @Override
    public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
        GaussianClassifierNode newNode = new GaussianClassifierNode(world, time, id, graph, initialResolution);
        return newNode;
    }
}
