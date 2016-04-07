package org.mwdb.math;

import org.mwdb.AbstractNode;
import org.mwdb.KCallback;
import org.mwdb.KGraph;
import org.mwdb.KNode;
import org.mwdb.math.expression.KMathExpressionEngine;
import org.mwdb.math.expression.impl.MathExpressionEngine;

public class MathNode extends AbstractNode {

    private KMathExpressionEngine mathEngine;

    public MathNode(long p_world, long p_time, long p_id, KGraph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
        mathEngine = new MathExpressionEngine();
    }


    @Override
    public Object att(String attributeName) {
        Object expressionObj = super.att(attributeName);
        if (expressionObj != null && expressionObj.toString().startsWith("$")) {
            return mathEngine.eval(null);
        } else {
            return expressionObj;
        }
    }

    @Override
    public void index(String indexName, KNode nodeToIndex, String[] keyAttributes, KCallback<Boolean> callback) {

    }

    @Override
    public void unindex(String indexName, KNode nodeToIndex, String[] keyAttributes, KCallback<Boolean> callback) {

    }



}
