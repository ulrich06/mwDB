package org.mwg;

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
        //newNode.attSet("_knownClassesList", Type.INT_ARRAY, new int[0]);
        //newNode.attSet("_bootstrapMode", Type.BOOL, true); //Start in bootstrap mode
        //newNode.attSet("_valueBuffer", Type.DOUBLE_ARRAY, new double[0]); //Value buffer, starts empty
        return newNode;
    }
}
