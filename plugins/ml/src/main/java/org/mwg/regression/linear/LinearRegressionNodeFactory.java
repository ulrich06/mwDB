package org.mwg.regression.linear;

import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.classifier.gaussiannb.GaussianNaiveBayesianNode;
import org.mwg.plugin.NodeFactory;

public class LinearRegressionNodeFactory implements NodeFactory {
    @Override
    public String name() {
        return "LinearRegressionNode";
    }

    @Override
    public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
        LinearRegressionNode newNode = new LinearRegressionNode(world, time, id, graph, initialResolution);
        return newNode;
    }
}
