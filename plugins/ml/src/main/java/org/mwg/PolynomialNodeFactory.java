package org.mwg;

import org.mwg.plugin.NodeFactory;
import org.mwg.polynomial.PolynomialNode;

public class PolynomialNodeFactory implements NodeFactory {
    @Override
    public String name() {
        return "PolynomialNode";
    }

    @Override
    public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
        return new PolynomialNode(world,time,id,graph,initialResolution);
    }
}
