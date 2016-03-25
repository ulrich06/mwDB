package org.mwdb.ml;

import org.mwdb.KGaussianNode;
import org.mwdb.KNode;
import org.mwdb.KType;
import org.mwdb.math.matrix.KMatrix;
import org.mwdb.math.matrix.Matrix;

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
            return avg();
        } else if (attributeName.equals(MIN_KEY)) {
            return min();
        } else if (attributeName.equals(MAX_KEY)) {
            return max();
        } else if (attributeName.equals(MAX_KEY)) {
            return max();
        } else if (attributeName.equals(COV_KEY)) {
            return getCovariance(avg());
        } else {
            return rootNode().att(attributeName);
        }
    }

    public double incWeight(double weight){
        return weight +1;
    }

    @Override
    public void learn(double[] values) {
        int features = values.length;

        //manage total
        Integer total = getTotal();
        Double weight = getWeight();

        //Create dirac
        if (total == null) {
            double[] sum = new double[features];
            System.arraycopy(values, 0, sum, 0, features);
            total=1;
            weight=incWeight(0);

            //set total, weight, sum, return
            attSet(INTERNAL_TOTAL_KEY, KType.INT,total);
            attSet(INTERNAL_WEIGHT_KEY, KType.DOUBLE,weight);
            attSet(INTERNAL_SUM_KEY, KType.DOUBLE_ARRAY,sum);

        }
        else {
            double[] sum;
            double[] min;
            double[] max;
            double[] sumsquares;

            //Upgrade dirac to gaussian
            if(total==1){
                //Create min, max, sumsquares
                sum = (double[]) rootNode().att(INTERNAL_SUM_KEY);
                min = new double[features];
                max = new double[features];
                System.arraycopy(values, 0, min, 0, features);
                System.arraycopy(values, 0, max, 0, features);

                sumsquares = new double[features*(features+1)/2];
                int count=0;
                for(int i=0;i<features;i++){
                    for(int j=i;j<features;j++){
                        sumsquares[count]=min[i]*min[j];
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
            for(int i=0;i<features;i++){
                if(values[i]<min[i]){
                    min[i]=values[i];
                }

                if(values[i]>max[i]){
                    max[i]=values[i];
                }
                sum[i]+=values[i];
            }

            int count=0;
            for(int i=0;i<features;i++){
                for(int j=i;j<features;j++){
                    sumsquares[count]+=values[i]*values[j];
                    count++;
                }
            }
            total++;
            weight=incWeight(weight);

            //Store everything
            attSet(INTERNAL_TOTAL_KEY, KType.INT,total);
            attSet(INTERNAL_WEIGHT_KEY, KType.DOUBLE,weight);
            attSet(INTERNAL_SUM_KEY, KType.DOUBLE_ARRAY,sum);
            attSet(INTERNAL_MIN_KEY, KType.DOUBLE_ARRAY,min);
            attSet(INTERNAL_MAX_KEY, KType.DOUBLE_ARRAY,max);
            attSet(INTERNAL_SUMSQUARE_KEY, KType.DOUBLE_ARRAY,sumsquares);
        }

    }

    public Integer getTotal(){
        return (Integer) rootNode().att(INTERNAL_TOTAL_KEY);
    }

    public Double getWeight(){
        return (Double) rootNode().att(INTERNAL_WEIGHT_KEY);
    }

    @Override
    public double[] avg() {
        Integer total = getTotal();
        if (total == null) {
            return null;
        }
        if(total==1){
            return (double[]) rootNode().att(INTERNAL_SUM_KEY);
        }
        else {
            double[] avg = (double[]) rootNode().att(INTERNAL_SUM_KEY);
            for (int i = 0; i < avg.length; i++) {
                avg[i] = avg[i]/total;
            }
            return avg;
        }

    }

    @Override
    public double[][] getCovariance(double[] avg) {
        int features=avg.length;

        Integer total = getTotal();
        if (total == null) {
            return null;
        }
        if(total>1){
            double[][] covariances = new double[features][features];
            double[] sumsquares = (double[]) rootNode().att(INTERNAL_SUMSQUARE_KEY);

            double correction=total;
            correction=correction/(total-1);

            int count=0;
            for (int i = 0; i < features; i++) {
                for (int j = i; j < features; j++) {
                    covariances[i][j] = (sumsquares[count] / total - avg[i] * avg[j]) * correction;
                    covariances[j][i]=covariances[i][j];
                    count++;
                }
            }
            return covariances;
        }
        else {
            return null;
        }
    }

    @Override
    public KMatrix getCovarianceMatrix(double[] avg) {
        int features=avg.length;

        Integer total = getTotal();
        if (total == null) {
            return null;
        }
        if(total>1){
            double[] covariances = new double[features*features];
            double[] sumsquares = (double[]) rootNode().att(INTERNAL_SUMSQUARE_KEY);

            double correction=total;
            correction=correction/(total-1);

            int count=0;
            for (int i = 0; i < features; i++) {
                for (int j = i; j < features; j++) {
                    covariances[i*features+j] = (sumsquares[count] / total - avg[i] * avg[j]) * correction;
                    covariances[j*features+i]=covariances[i*features+j];
                    count++;
                }
            }
            return new Matrix(covariances,features,features);
        }
        else {
            return null;
        }
    }

    @Override
    public double[] min() {
        Integer total = getTotal();
        if (total == null) {
            return null;
        }
        if(total==1){
            double[] min = (double[]) rootNode().att(INTERNAL_SUM_KEY);
            return min;
        }
        else {
            double[] min = (double[]) rootNode().att(INTERNAL_MIN_KEY);
            return min;
        }
    }

    @Override
    public double[] max() {
        Integer total = getTotal();
        if (total == null) {
            return null;
        }
        if(total==1){
            double[] max = (double[]) rootNode().att(INTERNAL_SUM_KEY);
            return max;
        }
        else {
            double[] max = (double[]) rootNode().att(INTERNAL_MAX_KEY);
            return max;
        }
    }
}
