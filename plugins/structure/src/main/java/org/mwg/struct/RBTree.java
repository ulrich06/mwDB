package org.mwg.struct;

import org.mwg.Graph;
import org.mwg.plugin.AbstractNode;

public class RBTree extends AbstractNode {

    public static String NAME = "RBTree";

    RBTree(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }



}
