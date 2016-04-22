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
        GaussianNaiveBayesianNode newNode = new GaussianNaiveBayesianNode(world, time, id, graph, initialResolution);
        //newNode.attSet("_knownClassesList", KType.INT_ARRAY, new int[0]);
        //newNode.attSet("_bootstrapMode", KType.BOOL, true); //Start in bootstrap mode
        //newNode.attSet("_valueBuffer", KType.DOUBLE_ARRAY, new double[0]); //Value buffer, starts empty
        return newNode;
    }
}
