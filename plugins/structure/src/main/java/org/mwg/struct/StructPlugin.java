package org.mwg.struct;

import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.plugin.AbstractPlugin;
import org.mwg.plugin.NodeFactory;

public class StructPlugin extends AbstractPlugin {

    public StructPlugin() {
        declareNodeType(RBTree.NAME, new NodeFactory() {
            @Override
            public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
                return new RBTree(world,time,id,graph,initialResolution);
            }
        });
        declareNodeType(RBTreeNode.NAME, new NodeFactory() {
            @Override
            public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
                return new RBTreeNode(world,time,id,graph,initialResolution);
            }
        });
    }
}
