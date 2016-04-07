package org.mwdb.polynomial;

import org.mwdb.*;

public class PolynomialNode extends AbstractNode implements KPolynomialNode {

    private static final String VALUE_KEY = "val";

    public PolynomialNode(long p_world, long p_time, long p_id, KGraph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    @Override
    public void attSet(String attributeName, byte attributeType, Object attributeValue) {
        if (attributeName.equals(VALUE_KEY)) {
            learn((double) attributeValue);
        } else {
            super.attSet(attributeName, attributeType, attributeValue);
        }
    }

    @Override
    public void index(String indexName, KNode nodeToIndex, String[] keyAttributes, KCallback<Boolean> callback) {

    }

    @Override
    public void unindex(String indexName, KNode nodeToIndex, String[] keyAttributes, KCallback<Boolean> callback) {

    }

    @Override
    public void find(String indexName, String query, KCallback<KNode[]> callback) {

    }

    @Override
    public void all(String indexName, KCallback<KNode[]> callback) {

    }

    @Override
    public byte attType(String attributeName) {
        if (attributeName.equals(VALUE_KEY)) {
            return KType.DOUBLE;
        } else {
            return super.attType(attributeName);
        }
    }

    @Override
    public Object att(String attributeName) {
        if (attributeName.equals(VALUE_KEY)) {
            return infer();
        } else {
            return super.att(attributeName);
        }
    }

    @Override
    public void learn(double value) {
        //TODO
    }

    @Override
    public double infer() {
        //TODO
        return 0;
    }
}
