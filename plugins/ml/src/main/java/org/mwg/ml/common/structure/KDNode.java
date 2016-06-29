package org.mwg.ml.common.structure;

import org.mwg.Graph;
import org.mwg.plugin.AbstractNode;

/**
 * Created by assaad on 29/06/16.
 */
public class KDNode extends AbstractNode {

    private static final String INTERNAL_LEFT = "_left";            //to navigate left
    private static final String INTERNAL_RIGHT= "_right";           //to navigate right
    private static final String INTERNAL_REL ="_rel";               //Object attached to the kd-Node
    private static final String INTERNAL_VALUES="_values";          //Values of the node

    public KDNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }
}
