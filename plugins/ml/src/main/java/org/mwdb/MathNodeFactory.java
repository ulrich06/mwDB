package org.mwdb;

import org.mwdb.math.MathNode;
import org.mwdb.plugin.KFactory;

public class MathNodeFactory implements KFactory {

    @Override
    public String name() {
        return "MathNode";
    }

    @Override
    public KNode create(long world, long time, long id, KGraph graph, long[] initialResolution) {
        return new MathNode(world, time, id, graph, initialResolution);
    }
}
