package org.mwg.ml.common;

import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.ml.common.mathexp.impl.MathExpressionEngine;
import org.mwg.plugin.AbstractNode;

/**
 * Created by assaad on 04/05/16.
 */
public abstract class AbstractMLNode extends AbstractNode {
    public static String FROM_SEPARATOR = ";";
    public static String FROM = "FROM";

    public AbstractMLNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    @Override
    public Object get(String propertyName) {
        if (propertyName != null && propertyName.startsWith("$")) {
            Object expressionObj = super.get(propertyName.substring(1));
            //ToDo this is dangerous for infinite loops or circular dependency, to fix
            org.mwg.ml.common.mathexp.MathExpressionEngine localEngine = MathExpressionEngine.parse(expressionObj.toString());
            return localEngine.eval(this);
        } else {
            return super.get(propertyName);
        }
    }


    //ToDo ouch this is hurting eyes for performance
    public void setTrainingVector(double[] vec) {
        String setFrom = "";
        for (int i = 0; i < vec.length; i++) {
            set("f" + i, vec[i]);
            setFrom = setFrom + "f" + i + FROM_SEPARATOR;
        }
        set(FROM, setFrom);
    }

    public void extractFeatures(Callback<double[]> callback) {
        String query = (String) super.get(FROM);
        double[] result;
        if (query != null) {
            String[] split = query.split(FROM_SEPARATOR);
            result = new double[split.length];
            for (int i = 0; i < result.length; i++) {
                result[i] = Double.parseDouble(get(split[i]).toString());
            }
            callback.on(result);
        } else {
            callback.on(null);
        }
    }


}
