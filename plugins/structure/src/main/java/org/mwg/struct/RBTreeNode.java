package org.mwg.struct;

import org.mwg.Graph;
import org.mwg.plugin.AbstractNode;

public class RBTreeNode extends AbstractNode {

    public static String NAME = "RBTree";
    
    private static final boolean RED = true;
    private static final boolean BLACK = false;

    public RBTreeNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

}
