package org.mwdb.polynomial;

import org.mwdb.*;
import org.mwdb.plugin.KResolver;

public class PolynomialNode extends AbstractNode implements KPolynomialNode {

    private static final String VALUE_KEY = "value";

    private static final String PRECISION_NAME = "_precision";
    private final long PRECISION_KEY;

    private static final String WEIGHT_NAME = "_weight";
    private final long WEIGHT_KEY;

    private static final String STEP_NAME = "_step";
    private final long STEP_KEY;

    private static final String NB_PAST_NAME = "_nb";
    private final long NB_PAST_KEY;

    public PolynomialNode(long p_world, long p_time, long p_id, KGraph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
        PRECISION_KEY = _resolver.stringToLongKey(PRECISION_NAME);
        WEIGHT_KEY = _resolver.stringToLongKey(WEIGHT_NAME);
        STEP_KEY = _resolver.stringToLongKey(STEP_NAME);
        NB_PAST_KEY = _resolver.stringToLongKey(NB_PAST_NAME);
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
        KResolver.KNodeState previousState = graph().resolver().resolveState(this, true); //past state, not cloned
        long previousTime = previousState.time();

        previousState.get(PRECISION_KEY);


        //TEST IF GOOD

        //OR

        KResolver.KNodeState phasedState = graph().resolver().resolveState(this, false); //force clone
        //put inside


        //TODO
    }

    @Override
    public double infer() {
        long currentTime = time();

        KResolver.KNodeState previousState = graph().resolver().resolveState(this, true);
        long previousTime = previousState.time();

        return 0;
    }

    @Override
    public void index(String indexName, KNode nodeToIndex, String[] keyAttributes, KCallback<Boolean> callback) {

    }

    @Override
    public void unindex(String indexName, KNode nodeToIndex, String[] keyAttributes, KCallback<Boolean> callback) {

    }

    @Override
    public String toString() {
        return "PolynomialNode{}";
    }
}
