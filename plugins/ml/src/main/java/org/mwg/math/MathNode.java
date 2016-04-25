package org.mwg.math;

import org.mwg.*;
import org.mwg.util.expression.KMathExpressionEngine;
import org.mwg.util.expression.impl.MathExpressionEngine;
import org.mwg.plugin.AbstractNode;

public class MathNode extends AbstractNode {

    private KMathExpressionEngine mathEngine;

    public MathNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
        //mathEngine = new MathExpressionEngine();
    }


    @Override
    public Object get(String propertyName) {
        Object expressionObj = super.get(propertyName);
        if (propertyName.startsWith("$") && expressionObj != null && type(propertyName) == Type.STRING) {
            KMathExpressionEngine localEngine = MathExpressionEngine.parse(expressionObj.toString());
            return localEngine.eval(this);
        }
        return expressionObj;
    }

    @Override
    public void index(String indexName, Node nodeToIndex, String[] keyAttributes, Callback<Boolean> callback) {

    }

    @Override
    public void unindex(String indexName, Node nodeToIndex, String[] keyAttributes, Callback<Boolean> callback) {

    }


}
