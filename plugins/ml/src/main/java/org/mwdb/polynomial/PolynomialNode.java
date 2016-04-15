package org.mwdb.polynomial;

import org.mwdb.*;
import org.mwdb.math.matrix.operation.PolynomialFit;
import org.mwdb.plugin.KResolver;

public class PolynomialNode extends AbstractNode implements KPolynomialNode {

    private static final String VALUE_NAME = "value";

    private static final String PRECISION_NAME = "_precision";
    private final long PRECISION_KEY;

    private static final String WEIGHT_NAME = "_weight";
    private final long WEIGHT_KEY;

    private static final String STEP_NAME = "_step";
    private final long STEP_KEY;

    private static final String NB_PAST_NAME = "_nb";
    private final long NB_PAST_KEY;

    private static final String LAST_TIME_NAME = "_lastTime";
    private final long LAST_TIME_KEY;

    private static final int _maxDegree = 18;

    public PolynomialNode(long p_world, long p_time, long p_id, KGraph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
        PRECISION_KEY = _resolver.stringToLongKey(PRECISION_NAME);
        WEIGHT_KEY = _resolver.stringToLongKey(WEIGHT_NAME);
        STEP_KEY = _resolver.stringToLongKey(STEP_NAME);
        NB_PAST_KEY = _resolver.stringToLongKey(NB_PAST_NAME);
        LAST_TIME_KEY = _resolver.stringToLongKey(LAST_TIME_NAME);
    }

    @Override
    public void attSet(String attributeName, byte attributeType, Object attributeValue) {
        if (attributeName.equals(VALUE_NAME)) {
            set((double) attributeValue);
        }
        if (attributeName.equals(PRECISION_NAME)) {
            setPrecision((double) attributeValue);
        } else {
            super.attSet(attributeName, attributeType, attributeValue);
        }
    }

    @Override
    public byte attType(String attributeName) {
        if (attributeName.equals(VALUE_NAME)) {
            return KType.DOUBLE;
        }
        if (attributeName.equals(PRECISION_NAME)) {
            return KType.DOUBLE;
        } else {
            return super.attType(attributeName);
        }
    }

    @Override
    public Object att(String attributeName) {
        if (attributeName.equals(VALUE_NAME)) {
            return get();
        }
        if (attributeName.equals(PRECISION_NAME)) {
            return getPrecision();
        } else {
            return super.att(attributeName);
        }
    }

    @Override
    public void setPrecision(double precision) {
        KResolver.KNodeState state = graph().resolver().resolveState(this, false);
        state.set(PRECISION_KEY, KType.DOUBLE, precision);
    }

    @Override
    public double getPrecision() {
        KResolver.KNodeState state = graph().resolver().resolveState(this, false);
        Object d = state.get(PRECISION_KEY);
        if (d != null) {
            return (double) d;
        } else {
            throw new RuntimeException("Precision is not defined");
        }

    }

    @Override
    public double[] getWeight() {
        KResolver.KNodeState state = graph().resolver().resolveState(this, false);
        return (double[]) state.get(WEIGHT_KEY);
    }

    @Override
    public void set(double value) {
        KResolver.KNodeState previousState = graph().resolver().resolveState(this, true); //past state, not cloned
        long timeOrigin = previousState.time();
        long time = time();
        double precision = (double) previousState.get(PRECISION_KEY);
        double[] weight = (double[]) previousState.get(WEIGHT_KEY);

        //Initial feed for the very first time
        if (weight == null) {
            weight = new double[1];
            weight[0] = value;
            previousState.set(WEIGHT_KEY, KType.DOUBLE_ARRAY, weight);
            previousState.set(NB_PAST_KEY, KType.INT, 1);
            previousState.set(STEP_KEY, KType.LONG, 0l);
            previousState.set(LAST_TIME_KEY, KType.LONG, 0l);
            return;
        }


        // Test the step and set it

        Long stp = (Long) previousState.get(STEP_KEY);
        long lastTime = time - timeOrigin;
        if (stp == null || stp == 0) {

            if (lastTime == 0) {
                weight = new double[1];
                weight[0] = value;
                previousState.set(WEIGHT_KEY, KType.DOUBLE_ARRAY, weight);
                return;
            } else {
                stp = lastTime;
                previousState.set(STEP_KEY, KType.LONG, stp);
            }
        }


        //Check if current model already fit the new value:

        int deg = weight.length - 1;
        int num = (int) previousState.get(NB_PAST_KEY);

        double t = (time - timeOrigin);
        t = t / stp;

        double maxError = maxErr(precision, deg);

        //If the current createModel fits well the new value, return
        if (Math.abs(PolynomialFit.extrapolate(t, weight) - value) <= maxError) {
            previousState.set(NB_PAST_KEY, KType.INT, num + 1);
            previousState.set(LAST_TIME_KEY, KType.LONG, lastTime);
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
                inc = ((long) previousState.get(LAST_TIME_KEY));
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
                previousState.set(WEIGHT_KEY, KType.DOUBLE_ARRAY, weight);
                previousState.set(NB_PAST_KEY, KType.INT, num + 1);
                previousState.set(LAST_TIME_KEY, KType.LONG, lastTime);
                return;
            }
        }


        long previousTime = timeOrigin + (long) previousState.get(LAST_TIME_KEY);
        long newstep = time - previousTime;
        //It does not fit, create a new state
        KResolver.KNodeState phasedState = graph().resolver().newState(this, world(), previousTime); //force clone
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

            phasedState.set(PRECISION_KEY, KType.DOUBLE, precision);
            phasedState.set(WEIGHT_KEY, KType.DOUBLE_ARRAY, weight);
            phasedState.set(NB_PAST_KEY, KType.INT, 2);
            phasedState.set(STEP_KEY, KType.LONG, newstep);
            phasedState.set(LAST_TIME_KEY, KType.LONG, newstep);

            return;
        }


        //Save degree 1
        PolynomialFit pf = new PolynomialFit(1);
        pf.fit(times, values);
        weight = pf.getCoef();
        phasedState.set(PRECISION_KEY, KType.DOUBLE, precision);
        phasedState.set(WEIGHT_KEY, KType.DOUBLE_ARRAY, weight);
        phasedState.set(NB_PAST_KEY, KType.INT, 2);
        phasedState.set(STEP_KEY, KType.LONG, newstep);
        phasedState.set(LAST_TIME_KEY, KType.LONG, newstep);
    }

    @Override
    public double get() {
        long time = time();
        KResolver.KNodeState state = graph().resolver().resolveState(this, true);
        long timeOrigin = state.time();
        double[] weight = (double[]) state.get(WEIGHT_KEY);
        if (weight == null) {
            return 0;
        }
        Long inferSTEP = (Long) state.get(STEP_KEY);
        if (inferSTEP == null || inferSTEP == 0) {
            return weight[0];
        }

        double t = (time - timeOrigin);
        t = t / inferSTEP;
        return PolynomialFit.extrapolate(t, weight);
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


    @Override
    public int getDegree() {
        double[] weights = getWeight();
        if (weights == null) {
            return -1;
        } else {
            return weights.length - 1;
        }
    }

    @Override
    public void index(String indexName, KNode nodeToIndex, String[] keyAttributes, KCallback<Boolean> callback) {

    }

    @Override
    public void unindex(String indexName, KNode nodeToIndex, String[] keyAttributes, KCallback<Boolean> callback) {

    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"world\":");
        builder.append(world());
        builder.append(",\"time\":");
        builder.append(time());
        builder.append(",\"id\":");
        builder.append(id());
        KResolver.KNodeState state = this._resolver.resolveState(this, true);
        if (state != null) {
            builder.append(",\"data\": {");
            double[] weight = (double[]) state.get(WEIGHT_KEY);
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
