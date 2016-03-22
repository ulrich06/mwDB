package org.mwdb.ml;

import org.mwdb.KGaussianNode;
import org.mwdb.KNode;
import org.mwdb.KType;

/**
 * Created by assaad on 21/03/16.
 */
public class GaussianNode extends AbstractMLNode implements KGaussianNode {


    public GaussianNode(KNode p_rootNode) {
        super(p_rootNode);
    }

    private static final String MIN_KEY = "min";
    private static final String MAX_KEY = "max";
    private static final String VALUE_KEY = "value";
    private static final String AVG_KEY = "avg";
    private static final String COV_KEY = "cov";

    private static final String INTERNAL_SUM_KEY = "_sum";
    private static final String INTERNAL_SUMSQUARE_KEY = "_sumSquare";
    private static final String INTERNAL_TOTAL_KEY = "_total";
    private static final String INTERNAL_MIN_KEY = "_min";
    private static final String INTERNAL_MAX_KEY = "_max";


    @Override
    public void attSet(String attributeName, byte attributeType, Object attributeValue) {
        if (attributeName.equals(VALUE_KEY) && attributeType== KType.DOUBLE_ARRAY) {
            learn((double[]) attributeValue);
        } else {
            rootNode().attSet(attributeName, attributeType, attributeValue);
        }
    }

    @Override
    public byte attType(String attributeName) {
        if (attributeName.equals(AVG_KEY)) {
            return KType.DOUBLE_ARRAY;
        } else if (attributeName.equals(MIN_KEY)) {
            return KType.DOUBLE_ARRAY;
        } else if (attributeName.equals(MAX_KEY)) {
            return KType.DOUBLE_ARRAY;
        }
        else if (attributeName.equals(COV_KEY)) {
            return KType.DOUBLE_ARRAY;
        }else {
            return rootNode().attType(attributeName);
        }
    }

    @Override
    public Object att(String attributeName) {
        if (attributeName.equals(AVG_KEY)) {
            return avg();
        } else if (attributeName.equals(MIN_KEY)) {
            return min();
        } else if (attributeName.equals(MAX_KEY)) {
            return max();
        }
        else if (attributeName.equals(MAX_KEY)) {
            return max();
        }
        else if (attributeName.equals(COV_KEY)) {
            return cov(avg());
        }
        else {
            return rootNode().att(attributeName);
        }
    }

    @Override
    public void learn(double[] value) {

    }

    @Override
    public double[] avg() {
        return new double[0];
    }

    @Override
    public double[][] cov(double[] avg) {
        return new double[0][];
    }

    @Override
    public double[] min() {
        return new double[0];
    }

    @Override
    public double[] max() {
        return new double[0];
    }
}
