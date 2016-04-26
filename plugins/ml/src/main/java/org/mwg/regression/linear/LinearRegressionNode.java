package org.mwg.regression.linear;

import org.mwg.Graph;
import org.mwg.plugin.AbstractNode;

/**
 * Created by andre on 4/26/2016.
 */
public class LinearRegressionNode extends AbstractNode implements KLinearRegression {

    public LinearRegressionNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }
}
