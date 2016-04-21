package org.mwdb;

import org.mwdb.gaussiannb.GaussianNaiveBayesianNode;
import org.mwdb.gmm.GaussianNode;
import org.mwdb.plugin.KFactory;

public class GaussianNaiveBayesianNodeFactory implements KFactory {
    @Override
    public String name() {
        return "GaussianNaiveBayesianNode";
    }

    @Override
    public KNode create(long world, long time, long id, KGraph graph, long[] initialResolution) {
        return new GaussianNaiveBayesianNode(world, time, id, graph, initialResolution);
    }
}
