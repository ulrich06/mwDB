package org.mwg.ml.common;

import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.ml.common.mathexp.KMathExpressionEngine;
import org.mwg.ml.common.mathexp.impl.MathExpressionEngine;
import org.mwg.plugin.AbstractNode;

/**
 * Created by assaad on 04/05/16.
 */
public abstract class AbstractMLNode extends AbstractNode {
    public static String FEATURES_SEPARATOR=";";
    public static String FROM="FROM";


    public AbstractMLNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    @Override
    public Object get(String propertyName) {
        if (propertyName != null && propertyName.startsWith("$")) {
            Object expressionObj = super.get(propertyName.substring(1));
            //ToDo this is dangerous for infinite loops or circular dependency, to fix
            KMathExpressionEngine localEngine = MathExpressionEngine.parse(expressionObj.toString());
            return localEngine.eval(this);
        } else {
            return super.get(propertyName);
        }
    }

    public void extractFeatures(Callback<double[]> callback){
        String query= (String)super.get(FROM);
        double[] result;
        if(query!=null) {
            String[] split = query.split(FEATURES_SEPARATOR);
            result = new double[split.length];
            for (int i = 0; i < result.length; i++) {
                KMathExpressionEngine localEngine = MathExpressionEngine.parse(split[i]);
                result[i] = localEngine.eval(this);
            }
            callback.on(result);
        }
        else {
            callback.on(null);
        }
    }


}
