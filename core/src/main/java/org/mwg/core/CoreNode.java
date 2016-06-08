package org.mwg.core;

import org.mwg.Graph;
import org.mwg.plugin.AbstractNode;

class CoreNode extends AbstractNode {

    CoreNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

}
