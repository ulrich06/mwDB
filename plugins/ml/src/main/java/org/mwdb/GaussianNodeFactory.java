package org.mwdb;

import org.mwdb.gmm.GaussianNode;
import org.mwdb.plugin.KFactory;

public class GaussianNodeFactory implements KFactory {
    @Override
    public String name() {
        return "GaussianNode";
    }

    @Override
    public KNode create(long world, long time, long id, KGraph graph, long[] initialResolution) {
        return new GaussianNode(world, time, id, graph, initialResolution);
    }
}
