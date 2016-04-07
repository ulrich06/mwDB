package org.mwdb;

import org.mwdb.plugin.KFactory;
import org.mwdb.polynomial.PolynomialNode;

public class PolynomialNodeFactory implements KFactory {
    @Override
    public String name() {
        return "PolynomialNode";
    }

    @Override
    public KNode create(long world, long time, long id, KGraph graph, long[] initialResolution) {
        return new PolynomialNode(world,time,id,graph,initialResolution);
    }
}
