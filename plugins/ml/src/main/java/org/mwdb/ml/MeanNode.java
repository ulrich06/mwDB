package org.mwdb.ml;

import org.mwdb.KMeanNode;
import org.mwdb.KNode;
import org.mwdb.KType;

public class MeanNode extends AbstractMLNode implements KMeanNode {

    private static final String MIN_KEY = "min";
    private static final String MAX_KEY = "max";
    private static final String VALUE_KEY = "value";
    private static final String MEAN_KEY = "mean";
    
    private static final String INTERNAL_SUM_KEY = "_sum";
    private static final String INTERNAL_TOTAL_KEY = "_total";
    private static final String INTERNAL_MIN_KEY = "_min";
    private static final String INTERNAL_MAX_KEY = "_max";

    public MeanNode(KNode p_rootNode) {
        super(p_rootNode);
    }

    @Override
    public void attSet(String attributeName, byte attributeType, Object attributeValue) {
        if (attributeName.equals(VALUE_KEY)) {
            learn((double) attributeType);
        } else {
            rootNode().attSet(attributeName, attributeType, attributeValue);
        }
    }

    @Override
    public byte attType(String attributeName) {
        if (attributeName.equals(MEAN_KEY)) {
            return KType.DOUBLE;
        } else if (attributeName.equals(MIN_KEY)) {
            return KType.DOUBLE;
        } else if (attributeName.equals(MAX_KEY)) {
            return KType.DOUBLE;
        } else {
            return rootNode().attType(attributeName);
        }
    }

    @Override
    public Object att(String attributeName) {
        if (attributeName.equals(MEAN_KEY)) {
            return mean();
        } else if (attributeName.equals(MIN_KEY)) {
            return min();
        } else if (attributeName.equals(MAX_KEY)) {
            return max();
        } else {
            return rootNode().att(attributeName);
        }
    }

    @Override
    public void learn(double value) {
        //manage total
        Double currentTotal = (Double) rootNode().att(INTERNAL_TOTAL_KEY);
        if (currentTotal == null) {
            rootNode().attSet(INTERNAL_TOTAL_KEY, KType.DOUBLE, 1.0);
        } else {
            rootNode().attSet(INTERNAL_TOTAL_KEY, KType.DOUBLE, currentTotal + 1);
        }
        //manage sum
        Double currentSum = (Double) rootNode().att(INTERNAL_SUM_KEY);
        if (currentSum == null) {
            rootNode().attSet(INTERNAL_SUM_KEY, KType.DOUBLE, value);
        } else {
            rootNode().attSet(INTERNAL_SUM_KEY, KType.DOUBLE, value + currentSum);
        }
        //manage min
        Double currentMin = (Double) rootNode().att(INTERNAL_MIN_KEY);
        if (currentMin == null || value < currentMin) {
            rootNode().attSet(INTERNAL_MIN_KEY, KType.DOUBLE, value);
        }
        //manage max
        Double currentMax = (Double) rootNode().att(INTERNAL_MAX_KEY);
        if (currentMax == null || value > currentMax) {
            rootNode().attSet(INTERNAL_MAX_KEY, KType.DOUBLE, value);
        }
    }

    @Override
    public double mean() {
        Double currentTotal = (Double) att(INTERNAL_TOTAL_KEY);
        Double currentSum = (Double) att(INTERNAL_SUM_KEY);
        if (currentTotal == null || currentSum == null) {
            return 0;
        } else {
            return currentSum / currentTotal;
        }
    }

    @Override
    public double min() {
        Double currentMin = (Double) att(INTERNAL_MIN_KEY);
        if (currentMin == null) {
            return 0;
        } else {
            return currentMin;
        }
    }

    @Override
    public double max() {
        Double currentMax = (Double) att(INTERNAL_MAX_KEY);
        if (currentMax == null) {
            return 0;
        } else {
            return currentMax;
        }
    }

}
