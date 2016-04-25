package org.mwg.classifier;

import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.gaussiannb.GaussianNaiveBayesianNode;
import org.mwg.plugin.NodeFactory;

public class GaussianNaiveBayesianNodeFactory implements NodeFactory {
    @Override
    public String name() {
        return "GaussianNaiveBayesianNode";
    }

    @Override
    public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
        GaussianNaiveBayesianNode newNode = new GaussianNaiveBayesianNode(world, time, id, graph, initialResolution);
        return newNode;
    }
}
