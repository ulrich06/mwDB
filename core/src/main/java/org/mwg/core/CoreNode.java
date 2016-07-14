package org.mwg.core;

import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.plugin.AbstractNode;
import org.mwg.plugin.NodeFactory;

class CoreNode extends AbstractNode {

    CoreNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    @Override
    public Node clone() {
        long[] initPreviouslyResolved = _previousResolveds.get();
        if (initPreviouslyResolved == null) {
            throw new RuntimeException("This Node has been tagged destroyed, please don't use it anymore! node id: " + id());
        }

        long typeCode = _resolver.markNodeAndGetType(this);
        NodeFactory resolvedFactory = graph().factoryByCode(typeCode);
        org.mwg.Node newNode;
        if (resolvedFactory == null) {
            newNode = new CoreNode(world(), time(), id(), graph(), initPreviouslyResolved);
        } else {
            newNode = resolvedFactory.create(world(), time(), id(), graph(), initPreviouslyResolved);
        }
        return newNode;
    }
}
