package org.mwg.regression;

import org.mwg.*;
import org.mwg.plugin.NodeFactory;
import org.mwg.util.matrix.operation.PolynomialFit;
import org.mwg.plugin.AbstractNode;
import org.mwg.plugin.NodeState;

public class MLPolynomialNode extends AbstractNode {

    //Name of the algorithm to be used in the meta model
    public final static String NAME = "Polynomial";

    //Factory of the class integrated
    public static class Factory implements NodeFactory {

        @Override
        public String name() {
            return NAME;
        }

        @Override
        public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
            return new MLPolynomialNode(world, time, id, graph, initialResolution);
        }
    }

    //Machine Learning Properties and their default values with _DEF

    public static final String PRECISION_KEY = "PRECISION"; //tolerated Error to specify by the signal
    public static final int PRECISION_DEF = 1;


    //Public specific getters and setters
    private static final String FEATURES_KEY = "FEATURES";

    //Internal state variables private and starts with _
    private static final String INTERNAL_WEIGHT_KEY = "_weight";
    private static final String INTERNAL_STEP_KEY = "_step";
    private static final String INTERNAL_NB_PAST_KEY = "_nb";
    private static final String INTERNAL_LAST_TIME_KEY = "_lastTime";

    //Other default parameters that should not be changed externally:
    private static final int _maxDegree = 20; // maximum polynomial degree


    public MLPolynomialNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }


    //Override default Abstract node default setters and getters
    @Override
    public void setProperty(String propertyName, byte propertyType, Object propertyValue) {
        if (propertyName.equals(FEATURES_KEY)) {
            learn((double) propertyValue);
        }
        else if(propertyName.equals(PRECISION_KEY)){
            super.setPropertyWithType(propertyName,propertyType,propertyValue, Type.DOUBLE);
        }
        else {
            super.setProperty(propertyName, propertyType,propertyValue);
        }
    }

    @Override
    public Object get(String attributeName) {
        if (attributeName.equals(FEATURES_KEY)) {
            return extrapolate();
        }
        else {
            return super.get(attributeName);
        }
    }


    //Main learning function

    public void learn(double value) {
        NodeState previousState = graph().resolver().resolveState(this, true); //past state, not cloned

        long timeOrigin = previousState.time();
        long time = time();
        double precision = (double) previousState.getFromKey(PRECISION_KEY);
        double[] weight = (double[]) previousState.getFromKey(INTERNAL_WEIGHT_KEY);

        //Initial feed for the very first time, the weight is set directly with the first value that arrives
        if (weight == null) {
            weight = new double[1];
            weight[0] = value;
            previousState.setFromKey(INTERNAL_WEIGHT_KEY, Type.DOUBLE_ARRAY, weight);
            previousState.setFromKey(INTERNAL_NB_PAST_KEY, Type.INT, 1);
            previousState.setFromKey(INTERNAL_STEP_KEY, Type.LONG, 0l);
            previousState.setFromKey(INTERNAL_LAST_TIME_KEY, Type.LONG, 0l);
            return;
        }


        // For the second time point, test and check for the step in time

        Long stp = (Long) previousState.getFromKey(INTERNAL_STEP_KEY);
        long lastTime = time - timeOrigin;
        if (stp == null || stp == 0) {

            if (lastTime == 0) {
                weight = new double[1];
                weight[0] = value;
                previousState.setFromKey(INTERNAL_WEIGHT_KEY, Type.DOUBLE_ARRAY, weight);
                return;
            } else {
                stp = lastTime;
                previousState.setFromKey(INTERNAL_STEP_KEY, Type.LONG, stp);
            }
        }


        //Then, first step, check if the current model already fits the new value:
        int deg = weight.length - 1;
        int num = (int) previousState.getFromKey(INTERNAL_NB_PAST_KEY);
        double t = (time - timeOrigin);
        t = t / stp;
        double maxError = maxErr(precision, deg);

        //If yes, update some states parameters and return
        if (Math.abs(PolynomialFit.extrapolate(t, weight) - value) <= maxError) {
            previousState.setFromKey(INTERNAL_NB_PAST_KEY, Type.INT, num + 1);
            previousState.setFromKey(INTERNAL_LAST_TIME_KEY, Type.LONG, lastTime);
            return;
        }

        //Check if we are inserting in the past:
        long previousTime = timeOrigin + (long) previousState.getFromKey(INTERNAL_LAST_TIME_KEY);
        if(time>previousTime) {
            //first check if we can increase the degree
            int newMaxDegree = Math.min(num, _maxDegree);
            if (deg < newMaxDegree) {
                deg++;
                int factor = 1;
                double[] times = new double[factor * num + 1];
                double[] values = new double[factor * num + 1];
                double inc = 0;
                if (num > 1) {
                    inc = ((long) previousState.getFromKey(INTERNAL_LAST_TIME_KEY));
                    inc = inc / (stp * (factor * num - 1));
                }
                for (int i = 0; i < factor * num; i++) {
                    times[i] = i * inc;
                    values[i] = PolynomialFit.extrapolate(times[i], weight);
                }
                times[factor * num] = (time - timeOrigin) / stp;
                values[factor * num] = value;
                PolynomialFit pf = new PolynomialFit(deg);
                pf.fit(times, values);
                if (tempError(pf.getCoef(), times, values) <= maxError) {
                    weight = pf.getCoef();
                    previousState.setFromKey(INTERNAL_WEIGHT_KEY, Type.DOUBLE_ARRAY, weight);
                    previousState.setFromKey(INTERNAL_NB_PAST_KEY, Type.INT, num + 1);
                    previousState.setFromKey(INTERNAL_LAST_TIME_KEY, Type.LONG, lastTime);
                    return;
                }
            }
        }
        else{
            //we are inserting in the past

        }

        //It does not fit, create a new state and split the polynomial
        long newstep = time - previousTime;
        NodeState phasedState = graph().resolver().newState(this, world(), previousTime); //force clone
        double[] values = new double[2];
        double pt = previousTime - timeOrigin;
        pt = pt / stp;
        values[0] = PolynomialFit.extrapolate(pt, weight);
        values[1] = value;

        //Test if the newly created polynomial is of degree 0 or 1.
        maxError = maxErr(precision, 0);
        if (Math.abs(values[1] - values[0]) <= maxError) {
            // Here it's a degree 0
            weight = new double[1];
            weight[0] = values[0];

            phasedState.setFromKey(PRECISION_KEY, Type.DOUBLE, precision);
            phasedState.setFromKey(INTERNAL_WEIGHT_KEY, Type.DOUBLE_ARRAY, weight);
            phasedState.setFromKey(INTERNAL_NB_PAST_KEY, Type.INT, 2);
            phasedState.setFromKey(INTERNAL_STEP_KEY, Type.LONG, newstep);
            phasedState.setFromKey(INTERNAL_LAST_TIME_KEY, Type.LONG, newstep);

            return;
        }
        else{
            //Here it's a degree 1
            values[1]=values[1]-values[0];

            phasedState.setFromKey(PRECISION_KEY, Type.DOUBLE, precision);
            phasedState.setFromKey(INTERNAL_WEIGHT_KEY, Type.DOUBLE_ARRAY, values);
            phasedState.setFromKey(INTERNAL_NB_PAST_KEY, Type.INT, 2);
            phasedState.setFromKey(INTERNAL_STEP_KEY, Type.LONG, newstep);
            phasedState.setFromKey(INTERNAL_LAST_TIME_KEY, Type.LONG, newstep);
        }

    }


    //Main extrapolate function

    public double extrapolate() {
        long time = time();
        NodeState state = graph().resolver().resolveState(this, true);
        long timeOrigin = state.time();
        double[] weight = (double[]) state.getFromKey(INTERNAL_WEIGHT_KEY);
        if (weight == null) {
            return 0;
        }
        Long inferSTEP = (Long) state.getFromKey(INTERNAL_STEP_KEY);
        if (inferSTEP == null || inferSTEP == 0) {
            return weight[0];
        }

        double t = (time - timeOrigin);
        t = t / inferSTEP;
        return PolynomialFit.extrapolate(t, weight);
    }


    //Other services and funcitons
    public double getPrecision() {
        NodeState state = graph().resolver().resolveState(this, false);
        Object d = state.getFromKeyWithDefault(PRECISION_KEY, PRECISION_DEF);
        return (double) d;
    }

    public double[] getWeight() {
        NodeState state = graph().resolver().resolveState(this, false);
        return (double[]) state.getFromKey(INTERNAL_WEIGHT_KEY);
    }


    private double maxErr(double precision, int degree) {
        //double tol = precision;
    /*    if (_prioritization == Prioritization.HIGHDEGREES) {
            tol = precision / Math.pow(2, _maxDegree - degree);
        } else if (_prioritization == Prioritization.LOWDEGREES) {*/
        //double tol = precision / Math.pow(2, degree + 0.5);
       /* } else if (_prioritization == Prioritization.SAMEPRIORITY) {
            tol = precision * degree * 2 / (2 * _maxDegree);
        }*/
        return precision / Math.pow(2, degree + 2);
    }


    private double tempError(double[] computedWeights, double[] times, double[] values) {
        double maxErr = 0;
        double temp;
        for (int i = 0; i < times.length; i++) {
            temp = Math.abs(values[i] - PolynomialFit.extrapolate(times[i], computedWeights));
            if (temp > maxErr) {
                maxErr = temp;
            }
        }
        return maxErr;
    }


    public int getDegree() {
        double[] weights = getWeight();
        if (weights == null) {
            return -1;
        } else {
            return weights.length - 1;
        }
    }


    //Default to string to print the learned state of ML, useful for debug
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"world\":");
        builder.append(world());
        builder.append(",\"time\":");
        builder.append(time());
        builder.append(",\"id\":");
        builder.append(id());
        NodeState state = this._resolver.resolveState(this, true);
        if (state != null) {
            builder.append(",\"data\": {");
            double[] weight = (double[]) state.getFromKey(INTERNAL_WEIGHT_KEY);
            if (weight != null) {
                builder.append("\"polynomial\": \"");
                for (int i = 0; i < weight.length; i++) {
                    if (i != 0) {
                        builder.append("+(");
                    }
                    builder.append(weight[i]);
                    if (i == 1) {
                        builder.append("*t");
                    } else if (i > 1) {
                        builder.append("*t^" + i);
                    }
                    if (i != 0) {
                        builder.append(")");
                    }
                }
                builder.append("\"");
            }
            builder.append("}}");
        }
        return builder.toString();
    }
}
