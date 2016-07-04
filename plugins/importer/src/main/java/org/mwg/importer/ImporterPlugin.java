package org.mwg.importer;

import org.mwg.Graph;
import org.mwg.plugin.AbstractNode;

public class ImporterPlugin extends AbstractNode {

    public ImporterPlugin(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

}
