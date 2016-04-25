package org.mwg;

import org.mwg.math.MathNode;
import org.mwg.plugin.NodeFactory;

public class MathNodeFactory implements NodeFactory {

    @Override
    public String name() {
        return "MathNode";
    }

    @Override
    public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
        return new MathNode(world, time, id, graph, initialResolution);
    }
}
