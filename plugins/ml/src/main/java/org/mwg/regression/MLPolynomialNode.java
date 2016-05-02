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

    public static final String PRECISION_NAME = "PRECISION"; //tolerated Error to specify by the signal
    public static final int PRECISION_DEF = 1;


    //Public specific getters and setters
    private static final String FEATURES_NAME = "FEATURES";

    //Internal state variables private and starts with _
    private static final String WEIGHT_NAME = "_weight";
    private static final String STEP_NAME = "_step";
    private static final String NB_PAST_NAME = "_nb";
    private static final String LAST_TIME_NAME = "_lastTime";

    //Other default parameters that should not be changed externally:
    private static final int _maxDegree = 20; // maximum polynomial degree


    public MLPolynomialNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }


    //Override default Abstract node default setters and getters
    @Override
    public void setProperty(String propertyName, byte propertyType, Object propertyValue) {
        if (propertyName.equals(FEATURES_NAME)) {
            learn((double) propertyValue);
        }
        if(propertyName.equals(PRECISION_NAME)){
            super.setPropertyWithType(propertyName,propertyType,propertyValue, Type.DOUBLE);
        }
        else {
            super.setProperty(propertyName, propertyType,propertyValue);
        }
    }

    @Override
    public Object get(String attributeName) {
        if (attributeName.equals(FEATURES_NAME)) {
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
        double precision = (double) previousState.getFromKey(PRECISION_NAME);
        double[] weight = (double[]) previousState.getFromKey(WEIGHT_NAME);

        //Initial feed for the very first time
        if (weight == null) {
            weight = new double[1];
            weight[0] = value;
            previousState.setFromKey(WEIGHT_NAME, Type.DOUBLE_ARRAY, weight);
            previousState.setFromKey(NB_PAST_NAME, Type.INT, 1);
            previousState.setFromKey(STEP_NAME, Type.LONG, 0l);
            previousState.setFromKey(LAST_TIME_NAME, Type.LONG, 0l);
            return;
        }


        // Test the step and set it

        Long stp = (Long) previousState.getFromKey(STEP_NAME);
        long lastTime = time - timeOrigin;
        if (stp == null || stp == 0) {

            if (lastTime == 0) {
                weight = new double[1];
                weight[0] = value;
                previousState.setFromKey(WEIGHT_NAME, Type.DOUBLE_ARRAY, weight);
                return;
            } else {
                stp = lastTime;
                previousState.setFromKey(STEP_NAME, Type.LONG, stp);
            }
        }


        //Check if current model already fit the new value:

        int deg = weight.length - 1;
        int num = (int) previousState.getFromKey(NB_PAST_NAME);

        double t = (time - timeOrigin);
        t = t / stp;

        double maxError = maxErr(precision, deg);

        //If the current createModel fits well the new value, return
        if (Math.abs(PolynomialFit.extrapolate(t, weight) - value) <= maxError) {
            previousState.setFromKey(NB_PAST_NAME, Type.INT, num + 1);
            previousState.setFromKey(LAST_TIME_NAME, Type.LONG, lastTime);
            return;
        }

        //If not, first check if we can increase the degree
        int newMaxDegree = Math.min(num, _maxDegree);
        if (deg < newMaxDegree) {
            deg++;
            int factor = 1;
            double[] times = new double[factor * num + 1];
            double[] values = new double[factor * num + 1];
            double inc = 0;
            if (num > 1) {
                inc = ((long) previousState.getFromKey(LAST_TIME_NAME));
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
                previousState.setFromKey(WEIGHT_NAME, Type.DOUBLE_ARRAY, weight);
                previousState.setFromKey(NB_PAST_NAME, Type.INT, num + 1);
                previousState.setFromKey(LAST_TIME_NAME, Type.LONG, lastTime);
                return;
            }
        }


        long previousTime = timeOrigin + (long) previousState.getFromKey(LAST_TIME_NAME);
        long newstep = time - previousTime;


        //It does not fit, create a new state
        NodeState phasedState = graph().resolver().newState(this, world(), previousTime); //force clone
        //put inside
        double[] times = new double[2];
        double[] values = new double[2];
        times[0] = 0;
        times[1] = 1;
        double pt = previousTime - timeOrigin;
        pt = pt / stp;
        values[0] = PolynomialFit.extrapolate(pt, weight);
        values[1] = value;

        maxError = maxErr(precision, 0);
        if (Math.abs(values[1] - values[0]) <= maxError) {
            // degree 0

            weight = new double[1];
            weight[0] = values[0];

            phasedState.setFromKey(PRECISION_NAME, Type.DOUBLE, precision);
            phasedState.setFromKey(WEIGHT_NAME, Type.DOUBLE_ARRAY, weight);
            phasedState.setFromKey(NB_PAST_NAME, Type.INT, 2);
            phasedState.setFromKey(STEP_NAME, Type.LONG, newstep);
            phasedState.setFromKey(LAST_TIME_NAME, Type.LONG, newstep);

            return;
        }


        //Save degree 1
        PolynomialFit pf = new PolynomialFit(1);
        pf.fit(times, values);
        weight = pf.getCoef();
        phasedState.setFromKey(PRECISION_NAME, Type.DOUBLE, precision);
        phasedState.setFromKey(WEIGHT_NAME, Type.DOUBLE_ARRAY, weight);
        phasedState.setFromKey(NB_PAST_NAME, Type.INT, 2);
        phasedState.setFromKey(STEP_NAME, Type.LONG, newstep);
        phasedState.setFromKey(LAST_TIME_NAME, Type.LONG, newstep);
    }


    //Main extrapolate function

    public double extrapolate() {
        long time = time();
        NodeState state = graph().resolver().resolveState(this, true);
        long timeOrigin = state.time();
        double[] weight = (double[]) state.getFromKey(WEIGHT_NAME);
        if (weight == null) {
            return 0;
        }
        Long inferSTEP = (Long) state.getFromKey(STEP_NAME);
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
        Object d = state.getFromKeyWithDefault(PRECISION_NAME, PRECISION_DEF);
        return (double) d;
    }

    public double[] getWeight() {
        NodeState state = graph().resolver().resolveState(this, false);
        return (double[]) state.getFromKey(WEIGHT_NAME);
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
            double[] weight = (double[]) state.getFromKey(WEIGHT_NAME);
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
