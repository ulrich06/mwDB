package org.mwg.ml.common;

import org.mwg.Graph;
import org.mwg.plugin.AbstractNode;

/**
 * Created by assaad on 04/05/16.
 */
public abstract class AbstractMLNode extends AbstractNode {


    public AbstractMLNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    public double[] extractFeatures(){
        //ToDo to implement feature extractions
        return new double[0];
    }


}
