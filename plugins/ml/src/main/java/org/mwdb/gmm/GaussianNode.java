package org.mwdb.gmm;

import org.mwdb.KCallback;
import org.mwdb.KNode;
import org.mwdb.KType;
import org.mwdb.math.matrix.KMatrix;
import org.mwdb.math.matrix.Matrix;
import org.mwdb.math.matrix.solver.MultivariateNormalDistribution;
import org.mwdb.AbstractMLNode;


/**
 * Created by assaad on 21/03/16.
 */
public class GaussianNode extends AbstractMLNode<KGaussianNode> implements KGaussianNode {


    public GaussianNode(KNode p_rootNode) {
        super(p_rootNode);
    }

    private static final String MIN_KEY = "getMin";
    private static final String MAX_KEY = "getMax";
    private static final String VALUE_KEY = "value";
    private static final String AVG_KEY = "getAvg";
    private static final String COV_KEY = "cov";


    //Mixture model keys
    private static final String INTERNAL_LEVEL_KEY="_level";
    private static final String INTERNAL_WIDTH_KEY="_width";
    private static final String INTERNAL_SUBGAUSSIAN_KEY="_subGaussian";

    //Gaussian keys
    private static final String INTERNAL_SUM_KEY = "_sum";
    private static final String INTERNAL_SUMSQUARE_KEY = "_sumSquare";
    private static final String INTERNAL_TOTAL_KEY = "_total";
    private static final String INTERNAL_WEIGHT_KEY = "_weight";
    private static final String INTERNAL_MIN_KEY = "_min";
    private static final String INTERNAL_MAX_KEY = "_max";


    @Override
    public void attSet(String attributeName, byte attributeType, Object attributeValue) {
        if (attributeName.equals(VALUE_KEY) && attributeType == KType.DOUBLE_ARRAY) {
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
        } else if (attributeName.equals(COV_KEY)) {
            return KType.DOUBLE_ARRAY;
        } else {
            return rootNode().attType(attributeName);
        }
    }

    @Override
    public Object att(String attributeName) {
        if (attributeName.equals(AVG_KEY)) {
            return getAvg();
        } else if (attributeName.equals(MIN_KEY)) {
            return getMin();
        } else if (attributeName.equals(MAX_KEY)) {
            return getMax();
        } else if (attributeName.equals(MAX_KEY)) {
            return getMax();
        } else if (attributeName.equals(COV_KEY)) {
            return getCovariance(getAvg());
        } else {
            return rootNode().att(attributeName);
        }
    }



    public double incWeight(double weight) {
        return weight + 1;
    }



    @Override
    public void configMixture(int levels,int maxPerLevel){
        rootNode().attSet(INTERNAL_LEVEL_KEY,KType.INT,levels);
        rootNode().attSet(INTERNAL_WIDTH_KEY,KType.INT,maxPerLevel);

    }
    @Override
    public int getSubLevels(){
        Integer g= (Integer) rootNode().att(INTERNAL_LEVEL_KEY);
        if(g!=null){
            return g;
        }
        else return 0;
    }

    @Override
    public int getMaxPerLevel(){
        Integer g= (Integer) rootNode().att(INTERNAL_WIDTH_KEY);
        if(g!=null){
            return g;
        }
        else return 0;
    }



    private void createLevel(double[] values, int level, int width){
        KNode node= graph().newNode(this.world(),this.time());
        GaussianNode g = new GaussianNode(node);
        g.configMixture(level,width);
        g.learn(values);

        rootNode().relAdd(INTERNAL_SUBGAUSSIAN_KEY,node);
    }

    private void checkAndCompress(){
        int width=getMaxPerLevel();
        long[] subgaussians= rootNode().relValues(INTERNAL_SUBGAUSSIAN_KEY);
        if(subgaussians==null|| subgaussians.length<width){
            return;
        }
        else{
            //Compress here
        }
    }


    @Override
    public void learnBatch(double[][] values) {
        if (values.length == 0) {
            return;
        }
        if (values.length == 1) {
            learn(values[0]);
            return;
        }

        //here values are more than 2


        int features = values[0].length;
        int level=getSubLevels();
        int width=getMaxPerLevel();


        //manage total
        Integer total = getTotal();
        Double weight = getWeight();

        double[] sum;
        double[] min;
        double[] max;
        double[] sumsquares;

        if (total == null) {
            total = 0;
            weight = 0.0;
            sum = new double[features];
            min = new double[features];
            max = new double[features];
            sumsquares = new double[features * (features + 1) / 2];
        } else {
            //get everything from saved
            sum = (double[]) rootNode().att(INTERNAL_SUM_KEY);
            min = (double[]) rootNode().att(INTERNAL_MIN_KEY);
            max = (double[]) rootNode().att(INTERNAL_MAX_KEY);
            sumsquares = (double[]) rootNode().att(INTERNAL_SUMSQUARE_KEY);
        }

        for (int k = 0; k < values.length; k++) {
            //Update the values
            for (int i = 0; i < features; i++) {
                if (values[k][i] < min[i]) {
                    min[i] = values[k][i];
                }

                if (values[k][i] > max[i]) {
                    max[i] = values[k][i];
                }
                sum[i] += values[k][i];
                if(level>0){
                    createLevel(values[k],level-1,width);
                }
            }

            int count = 0;
            for (int i = 0; i < features; i++) {
                for (int j = i; j < features; j++) {
                    sumsquares[count] += values[k][i] * values[k][j];
                    count++;
                }
            }
            total++;
            weight = incWeight(weight);
        }
        checkAndCompress();

        //Store everything
        attSet(INTERNAL_TOTAL_KEY, KType.INT, total);
        attSet(INTERNAL_WEIGHT_KEY, KType.DOUBLE, weight);
        attSet(INTERNAL_SUM_KEY, KType.DOUBLE_ARRAY, sum);
        attSet(INTERNAL_MIN_KEY, KType.DOUBLE_ARRAY, min);
        attSet(INTERNAL_MAX_KEY, KType.DOUBLE_ARRAY, max);
        attSet(INTERNAL_SUMSQUARE_KEY, KType.DOUBLE_ARRAY, sumsquares);
    }

    @Override
    public void learn(double[] values) {
        int features = values.length;

        //manage total
        Integer total = getTotal();
        Double weight = getWeight();
        int level=getSubLevels();
        int width=getMaxPerLevel();

        //Create dirac
        if (total == null) {
            double[] sum = new double[features];
            System.arraycopy(values, 0, sum, 0, features);
            total = 1;
            weight = incWeight(0);

            //set total, weight, sum, return
            attSet(INTERNAL_TOTAL_KEY, KType.INT, total);
            attSet(INTERNAL_WEIGHT_KEY, KType.DOUBLE, weight);
            attSet(INTERNAL_SUM_KEY, KType.DOUBLE_ARRAY, sum);
            if(level>0){
                createLevel(values,level-1,width);
                checkAndCompress();
            }

        } else {
            double[] sum;
            double[] min;
            double[] max;
            double[] sumsquares;

            //Upgrade dirac to gaussian
            if (total == 1) {
                //Create getMin, getMax, sumsquares
                sum = (double[]) rootNode().att(INTERNAL_SUM_KEY);
                min = new double[features];
                max = new double[features];
                System.arraycopy(sum, 0, min, 0, features);
                System.arraycopy(sum, 0, max, 0, features);

                sumsquares = new double[features * (features + 1) / 2];
                int count = 0;
                for (int i = 0; i < features; i++) {
                    for (int j = i; j < features; j++) {
                        sumsquares[count] = sum[i] * sum[j];
                        count++;
                    }
                }
            }
            //Otherwise, get previously stored values
            else {
                sum = (double[]) rootNode().att(INTERNAL_SUM_KEY);
                min = (double[]) rootNode().att(INTERNAL_MIN_KEY);
                max = (double[]) rootNode().att(INTERNAL_MAX_KEY);
                sumsquares = (double[]) rootNode().att(INTERNAL_SUMSQUARE_KEY);
            }

            //Update the values
            for (int i = 0; i < features; i++) {
                if (values[i] < min[i]) {
                    min[i] = values[i];
                }

                if (values[i] > max[i]) {
                    max[i] = values[i];
                }
                sum[i] += values[i];
            }

            int count = 0;
            for (int i = 0; i < features; i++) {
                for (int j = i; j < features; j++) {
                    sumsquares[count] += values[i] * values[j];
                    count++;
                }
            }
            total++;
            weight = incWeight(weight);
            if(level>0){
                createLevel(values,level-1,width);
                checkAndCompress();
            }

            //Store everything
            attSet(INTERNAL_TOTAL_KEY, KType.INT, total);
            attSet(INTERNAL_WEIGHT_KEY, KType.DOUBLE, weight);
            attSet(INTERNAL_SUM_KEY, KType.DOUBLE_ARRAY, sum);
            attSet(INTERNAL_MIN_KEY, KType.DOUBLE_ARRAY, min);
            attSet(INTERNAL_MAX_KEY, KType.DOUBLE_ARRAY, max);
            attSet(INTERNAL_SUMSQUARE_KEY, KType.DOUBLE_ARRAY, sumsquares);
        }

    }

    @Override
    public int getNumberOfFeatures() {
        Integer total = getTotal();
        if (total == null) {
            return 0;
        } else {
            double[] sum = (double[]) rootNode().att(INTERNAL_SUM_KEY);
            return sum.length;
        }
    }

    @Override
    public double[] getSum(){
        Integer total = getTotal();
        if (total == null) {
            return null;
        }
        else  {
            return (double[]) rootNode().att(INTERNAL_SUM_KEY);
        }
    }

    @Override
    public double[] getSumSquares(){
        Integer total = getTotal();
        if (total == null) {
            return null;
        }
        if(total ==1)  {
            double[] sum = (double[]) rootNode().att(INTERNAL_SUM_KEY);

            int features=sum.length;
            double[] sumsquares = new double[features * (features + 1) / 2];
            int count = 0;
            for (int i = 0; i < features; i++) {
                for (int j = i; j < features; j++) {
                    sumsquares[count] = sum[i] * sum[j];
                    count++;
                }
            }
            return sumsquares;
        }
        else {
            return  (double[]) rootNode().att(INTERNAL_SUMSQUARE_KEY);
        }
    }

    @Override
    public double getProbability(double[] featArray, double[] err, boolean normalizeOnAvg) {

        double[] avg = getAvg();
        if (avg == null) {
            return 0;
        }
        KMatrix cov = getCovarianceMatrix(avg);
        if (cov == null) {
            cov = new Matrix(null, featArray.length, featArray.length);
            for (int i = 0; i < featArray.length; i++) {
                cov.set(i, i, err[i] * err[i]);
            }
        }

        MultivariateNormalDistribution mnd = new MultivariateNormalDistribution(avg, cov);
        return mnd.density(featArray, normalizeOnAvg);
    }

    @Override
    public double[] getProbabilityArray(double[][] featArray, double[] err, boolean normalizeOnAvg) {
        double[] res = new double[featArray.length];
        double[] avg = getAvg();
        if (avg == null) {
            return null;
        }
        KMatrix cov = getCovarianceMatrix(avg);
        if (cov == null) {
            cov = new Matrix(null, featArray.length, featArray.length);
            for (int i = 0; i < featArray.length; i++) {
                cov.set(i, i, err[i] * err[i]);
            }
        }
        MultivariateNormalDistribution mnd = new MultivariateNormalDistribution(avg, cov);

        for (int i = 0; i < res.length; i++) {
            res[i] = mnd.density(featArray[i],normalizeOnAvg);
        }
        return res;
    }

    @Override
    public Integer getTotal() {
        return (Integer) rootNode().att(INTERNAL_TOTAL_KEY);
    }

    @Override
    public Double getWeight() {
        return (Double) rootNode().att(INTERNAL_WEIGHT_KEY);
    }

    @Override
    public double[] getAvg() {
        Integer total = getTotal();
        if (total == null) {
            return null;
        }
        if (total == 1) {
            return (double[]) rootNode().att(INTERNAL_SUM_KEY);
        } else {
            double[] avg = (double[]) rootNode().att(INTERNAL_SUM_KEY);
            for (int i = 0; i < avg.length; i++) {
                avg[i] = avg[i] / total;
            }
            return avg;
        }

    }

    @Override
    public double[][] getCovariance(double[] avg) {
        if(avg==null){
            return null;
        }
        int features = avg.length;

        Integer total = getTotal();
        if (total == null) {
            return null;
        }
        if (total > 1) {
            double[][] covariances = new double[features][features];
            double[] sumsquares = (double[]) rootNode().att(INTERNAL_SUMSQUARE_KEY);

            double correction = total;
            correction = correction / (total - 1);

            int count = 0;
            for (int i = 0; i < features; i++) {
                for (int j = i; j < features; j++) {
                    covariances[i][j] = (sumsquares[count] / total - avg[i] * avg[j]) * correction;
                    covariances[j][i] = covariances[i][j];
                    count++;
                }
            }
            return covariances;
        } else {
            return null;
        }
    }

    @Override
    public KMatrix getCovarianceMatrix(double[] avg) {
        int features = avg.length;

        Integer total = getTotal();
        if (total == null) {
            return null;
        }
        if (total > 1) {
            double[] covariances = new double[features * features];
            double[] sumsquares = (double[]) rootNode().att(INTERNAL_SUMSQUARE_KEY);

            double correction = total;
            correction = correction / (total - 1);

            int count = 0;
            for (int i = 0; i < features; i++) {
                for (int j = i; j < features; j++) {
                    covariances[i * features + j] = (sumsquares[count] / total - avg[i] * avg[j]) * correction;
                    covariances[j * features + i] = covariances[i * features + j];
                    count++;
                }
            }
            return new Matrix(covariances, features, features);
        } else {
            return null;
        }
    }

    @Override
    public double[] getMin() {
        Integer total = getTotal();
        if (total == null) {
            return null;
        }
        if (total == 1) {
            double[] min = (double[]) rootNode().att(INTERNAL_SUM_KEY);
            return min;
        } else {
            double[] min = (double[]) rootNode().att(INTERNAL_MIN_KEY);
            return min;
        }
    }

    @Override
    public double[] getMax() {
        Integer total = getTotal();
        if (total == null) {
            return null;
        }
        if (total == 1) {
            double[] max = (double[]) rootNode().att(INTERNAL_SUM_KEY);
            return max;
        } else {
            double[] max = (double[]) rootNode().att(INTERNAL_MAX_KEY);
            return max;
        }
    }


    @Override
    public void jump(long world, long time, KCallback<KGaussianNode> callback) {
        rootNode().graph().lookup(world, time, rootNode().id(), new KCallback<KNode>() {
            @Override
            public void on(KNode result) {
                callback.on(new GaussianNode(result));
            }
        });
    }

}
