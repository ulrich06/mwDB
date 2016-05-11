package org.mwg.ml.algorithm.profiling;

import org.mwg.*;
import org.mwg.ml.ProfilingNode;
import org.mwg.ml.common.AbstractMLNode;
import org.mwg.plugin.NodeFactory;
import org.mwg.ml.common.matrix.Matrix;
import org.mwg.ml.common.matrix.operation.MultivariateNormalDistribution;
import org.mwg.task.*;

public class GaussianGmmNode2 extends AbstractMLNode implements ProfilingNode{

    public final static double ERR=1.0;
    //Name of the algorithm to be used in the meta model
    public final static String NAME = "GaussianGmm2";

    @Override
    public void learn(Callback<Boolean> callback) {
        extractFeatures(new Callback<double[]>() {
            @Override
            public void on(final double[] value) {

            }
        });
    }


    private void learnVector(final double[] value,Callback<Boolean> callback){

        Task creationTask = graph().newTask().then(new TaskAction() {
            @Override
            public void eval(TaskContext context) {
                GaussianGmmNode2 node = (GaussianGmmNode2) context.getVariable("starterNode");
                System.out.println("Creation at node: " + node.id());
                node.internallearn(value,true);
            }
        });


        Task traverse = graph().newTask();
        traverse.fromVar("starterNode").traverse(INTERNAL_SUBGAUSSIAN_KEY).then(new TaskAction() {
            @Override
            public void eval(TaskContext context) {
                GaussianGmmNode2[] result = (GaussianGmmNode2[]) context.getPreviousResult();
                GaussianGmmNode2 parent = (GaussianGmmNode2) context.getVariable("starterNode");
                GaussianGmmNode2 resultChild = filter(result, value);
                if (resultChild != null) {
                    parent.internallearn(value, false);
                    context.setVariable("continueLoop", true);
                    context.setVariable("starterNode", resultChild);
                } else {
                    context.setVariable("continueLoop", false);
                }

            }
        })
                .ifThen(new TaskFunctionConditional() {
                    @Override
                    public boolean eval(TaskContext context) {
                        return (boolean) context.getVariable("continueLoop");
                    }
                }, traverse);

        Task mainTask = graph().newTask().from(this).asVar("starterNode").wait(traverse).wait(creationTask);
        mainTask.execute();
    }

    private GaussianGmmNode2 filter(GaussianGmmNode2[] result, double[] features) {
        if(result==null||result.length==0){
            return null;
        }
        double[] distances=new double[result.length];
        double min=Double.MAX_VALUE;
        int index=0;
        for(int i=0;i<result.length;i++){
            distances[i]=distance(features,result[i].getAvg());
            if(distances[i]<min){
                min=distances[i];
                index=i;
            }
        }
        if(min<ERR){
            return result[index];
        }
        else {
            return null;
        }
    }

    @Override
    public void predict(Callback<double[]> callback) {

    }


    //Factory of the class integrated
    public static class Factory implements NodeFactory {

        @Override
        public String name() {
            return NAME;
        }

        @Override
        public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
            return new GaussianGmmNode2(world, time, id, graph, initialResolution);
        }
    }
    public GaussianGmmNode2(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }



    //Getters and setters
    public static final String MIN_KEY = "min";
    public static final String MAX_KEY = "max";
    public static final String AVG_KEY = "avg";
    public static final String COV_KEY = "cov";


    //Mixture model keys
    private static final String INTERNAL_LEVEL_KEY = "_level";
    private static final String INTERNAL_WIDTH_KEY = "_width";
    private static final String INTERNAL_SUBGAUSSIAN_KEY = "_subGaussian";

    //Gaussian keys
    private static final String INTERNAL_SUM_KEY = "_sum";
    private static final String INTERNAL_SUMSQUARE_KEY = "_sumSquare";
    private static final String INTERNAL_TOTAL_KEY = "_total";
    private static final String INTERNAL_WEIGHT_KEY = "_weight";
    private static final String INTERNAL_MIN_KEY = "_min";
    private static final String INTERNAL_MAX_KEY = "_max";

    //Private internal params
    private static final int _COMPRESSION_ITER = 10;
    private static final int _CAPACITY_FACTOR = 3;

    @Override
    public void setProperty(String propertyName, byte propertyType, Object propertyValue) {
        super.setProperty(propertyName,propertyType,propertyValue);
    }

    @Override
    public byte type(String attributeName) {
        if (attributeName.equals(AVG_KEY)) {
            return Type.DOUBLE_ARRAY;
        } else if (attributeName.equals(MIN_KEY)) {
            return Type.DOUBLE_ARRAY;
        } else if (attributeName.equals(MAX_KEY)) {
            return Type.DOUBLE_ARRAY;
        } else if (attributeName.equals(COV_KEY)) {
            return Type.DOUBLE_ARRAY;
        } else {
            return super.type(attributeName);
        }
    }

    @Override
    public Object get(String attributeName) {
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
            return super.get(attributeName);
        }
    }


    public double incWeight(double weight) {
        return weight + 1;
    }


    public void configMixture(int levels, int maxPerLevel) {
        super.set(INTERNAL_LEVEL_KEY, levels);
        super.set(INTERNAL_WIDTH_KEY, maxPerLevel);
    }

    public int getLevel() {
        Integer g = (Integer) super.get(INTERNAL_LEVEL_KEY);
        if (g != null) {
            return g;
        } else return 0;
    }

    public int getMaxPerLevel() {
        Integer g = (Integer) super.get(INTERNAL_WIDTH_KEY);
        if (g != null) {
            return g;
        } else return 0;
    }



    private void updateLevel(final int newLevel) {
        super.set(INTERNAL_LEVEL_KEY, newLevel);
        if (newLevel == 0) {
            super.set(INTERNAL_SUBGAUSSIAN_KEY, null);
        } else {
            super.rel(INTERNAL_SUBGAUSSIAN_KEY, new Callback<Node[]>() {
                @Override
                public void on(Node[] result) {
                    for (int i = 0; i < result.length; i++) {
                        GaussianGmmNode2 g = (GaussianGmmNode2) result[i];
                        g.updateLevel(newLevel - 1);
                    }
                }
            });
        }
    }

    private void createLevel(double[] values, int level, int width) {
        GaussianGmmNode2 g = (GaussianGmmNode2) graph().newNode(this.world(), this.time(), "GaussianGmm");
        g.configMixture(level, width);
        g.internallearn(values, false); //dirac

        super.add(INTERNAL_SUBGAUSSIAN_KEY, g);
    }

    private void checkAndCompress() {

        final Node selfPointer = this;

        final int width = getMaxPerLevel();
        long[] subgaussians = (long[]) super.get(INTERNAL_SUBGAUSSIAN_KEY);
        if (subgaussians == null || subgaussians.length < _CAPACITY_FACTOR * width) {
            return;
        } else {
            //Compress here
            super.rel(INTERNAL_SUBGAUSSIAN_KEY, new Callback<Node[]>() {
                @Override
                //result.length hold the original subgaussian number, and width is after compression
                public void on(Node[] result) {
                    int features = getNumberOfFeatures();

                    int[] totals = new int[width];

                    GaussianGmmNode2[] subgauss = new GaussianGmmNode2[result.length];
                    double[][] data = new double[result.length][];
                    for (int i = 0; i < result.length; i++) {
                        subgauss[i] = (GaussianGmmNode2) result[i];
                        data[i] = subgauss[i].getAvg();
                    }

                    //Cluster the different gaussians
                    KMeans clusteringEngine = new KMeans();
                    int[][] clusters = clusteringEngine.getClusterIds(data, width, _COMPRESSION_ITER, getMin(), getMax());

                    //Select the ones which will remain as head by the maximum weight
                    GaussianGmmNode2[] mainClusters = new GaussianGmmNode2[width];
                    for (int i = 0; i < width; i++) {
                        if (clusters[i] != null && clusters[i].length > 0) {
                            int max = 0;
                            int maxpos = 0;
                            for (int j = 0; j < clusters[i].length; j++) {
                                int x = subgauss[clusters[i][j]].getTotal();
                                if (x > max) {
                                    max = x;
                                    maxpos = clusters[i][j];
                                }
                            }
                            mainClusters[i] = subgauss[maxpos];
                        }
                    }


                    //move the nodes
                    for (int i = 0; i < width; i++) {
                        //if the main cluster node contains only 1 sample, it needs to clone itself in itself
                        if (clusters[i].length > 1 && mainClusters[i].getTotal() == 1 && mainClusters[i].getLevel() > 0) {
                            mainClusters[i].createLevel(mainClusters[i].getAvg(), mainClusters[i].getLevel() - 1, mainClusters[i].getMaxPerLevel());
                        }

                        if (clusters[i] != null && clusters[i].length > 0) {
                            for (int j = 0; j < clusters[i].length; j++) {
                                GaussianGmmNode2 g = subgauss[clusters[i][j]];
                                if (g != mainClusters[i]) {
                                    mainClusters[i].move(g);
                                    selfPointer.remove(INTERNAL_SUBGAUSSIAN_KEY, g);
                                }
                            }
                            mainClusters[i].checkAndCompress();
                        }
                    }
                }
            });
        }
    }


    private void move(GaussianGmmNode2 subgaus) {
        //manage total
        int total = getTotal();
        Double weight = getWeight();
        int level = getLevel();


        double[] sum = getSum();
        double[] min = getMin();
        double[] max = getMax();
        double[] sumsquares = getSumSquares();


        //Start the merging phase

        total = total + subgaus.getTotal();
        weight = weight + subgaus.getWeight();

        double[] sum2 = subgaus.getSum();
        double[] min2 = subgaus.getMin();
        double[] max2 = subgaus.getMax();
        double[] sumsquares2 = subgaus.getSumSquares();

        for (int i = 0; i < sum.length; i++) {
            sum[i] = sum[i] + sum2[i];
            if (min2[i] < min[i]) {
                min[i] = min2[i];
            }
            if (max2[i] > max[i]) {
                max[i] = max2[i];
            }
        }

        for (int i = 0; i < sumsquares.length; i++) {
            sumsquares[i] = sumsquares[i] + sumsquares2[i];
        }

        //Store everything
        set(INTERNAL_TOTAL_KEY, total);
        set(INTERNAL_WEIGHT_KEY, weight);
        set(INTERNAL_SUM_KEY, sum);
        set(INTERNAL_MIN_KEY, min);
        set(INTERNAL_MAX_KEY, max);
        set(INTERNAL_SUMSQUARE_KEY, sumsquares);

        //Add the subGaussian to the relationship
        // TODO: to debug here to validate
        if (level > 0) {
            long[] subrelations = (long[]) subgaus.get(INTERNAL_SUBGAUSSIAN_KEY);
            if (subrelations == null) {
                subgaus.updateLevel(level - 1);
                super.add(INTERNAL_SUBGAUSSIAN_KEY, subgaus);
            } else {
                long[] oldrel = (long[]) this.get(INTERNAL_SUBGAUSSIAN_KEY);
                if (oldrel == null) {
                    oldrel = new long[0];
                }
                long[] newrelations = new long[oldrel.length + subrelations.length];
                System.arraycopy(oldrel, 0, newrelations, 0, oldrel.length);
                System.arraycopy(subrelations, 0, newrelations, oldrel.length, subrelations.length);
                set(INTERNAL_SUBGAUSSIAN_KEY, newrelations);
            }


        }
    }


    public void internallearn(double[] values, boolean createNode) {
        int features = values.length;

        //manage total
        int total = getTotal();
        Double weight = getWeight();
        int level = getLevel();
        int width = getMaxPerLevel();

        //Create dirac
        if (total == 0) {
            double[] sum = new double[features];
            System.arraycopy(values, 0, sum, 0, features);
            total = 1;
            weight = incWeight(0);

            //set total, weight, sum, return
            set(INTERNAL_TOTAL_KEY, total);
            set(INTERNAL_WEIGHT_KEY, weight);
            set(INTERNAL_SUM_KEY, sum);
            if (createNode && level > 0) {
                createLevel(values, level - 1, width);
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
                sum = (double[]) super.get(INTERNAL_SUM_KEY);
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
                sum = (double[]) super.get(INTERNAL_SUM_KEY);
                min = (double[]) super.get(INTERNAL_MIN_KEY);
                max = (double[]) super.get(INTERNAL_MAX_KEY);
                sumsquares = (double[]) super.get(INTERNAL_SUMSQUARE_KEY);
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
            if (createNode && level > 0) {
                createLevel(values, level - 1, width);
                checkAndCompress();
            }

            //Store everything
            set(INTERNAL_TOTAL_KEY, total);
            set(INTERNAL_WEIGHT_KEY, weight);
            set(INTERNAL_SUM_KEY, sum);
            set(INTERNAL_MIN_KEY, min);
            set(INTERNAL_MAX_KEY, max);
            set(INTERNAL_SUMSQUARE_KEY, sumsquares);
        }

    }

    public int getNumberOfFeatures() {
        int total = getTotal();
        if (total == 0) {
            return 0;
        } else {
            double[] sum = (double[]) super.get(INTERNAL_SUM_KEY);
            return sum.length;
        }
    }

    public double[] getSum() {
        int total = getTotal();
        if (total == 0) {
            return null;
        } else {
            return (double[]) super.get(INTERNAL_SUM_KEY);
        }
    }

    public double[] getSumSquares() {
        int total = getTotal();
        if (total == 0) {
            return null;
        }
        if (total == 1) {
            double[] sum = (double[]) super.get(INTERNAL_SUM_KEY);

            int features = sum.length;
            double[] sumsquares = new double[features * (features + 1) / 2];
            int count = 0;
            for (int i = 0; i < features; i++) {
                for (int j = i; j < features; j++) {
                    sumsquares[count] = sum[i] * sum[j];
                    count++;
                }
            }
            return sumsquares;
        } else {
            return (double[]) super.get(INTERNAL_SUMSQUARE_KEY);
        }
    }

    public double getProbability(double[] featArray, double[] err, boolean normalizeOnAvg) {
        double[] sum = (double[]) super.get(INTERNAL_SUM_KEY);
        double[] sumsquares = (double[]) super.get(INTERNAL_SUMSQUARE_KEY);
        MultivariateNormalDistribution mnd = MultivariateNormalDistribution.getDistribution(sum, sumsquares, getTotal());
        if (mnd == null) {
            //todo handle dirac to be replaced later
            return 0;
        } else {
            return mnd.density(featArray, normalizeOnAvg);
        }
    }

    public double[] getProbabilityArray(double[][] featArray, double[] err, boolean normalizeOnAvg) {
        double[] res = new double[featArray.length];

        double[] sum = (double[]) super.get(INTERNAL_SUM_KEY);
        double[] sumsquares = (double[]) super.get(INTERNAL_SUMSQUARE_KEY);
        MultivariateNormalDistribution mnd = MultivariateNormalDistribution.getDistribution(sum, sumsquares, getTotal());

        if (mnd == null) {
            //todo handle dirac to be replaced later
            return res;
        } else {
            for (int i = 0; i < res.length; i++) {
                res[i] = mnd.density(featArray[i], normalizeOnAvg);
            }
            return res;
        }

    }

    public int getTotal() {
        Integer x = (Integer) super.get(INTERNAL_TOTAL_KEY);
        if (x == null) {
            return 0;
        } else {
            return x;
        }
    }

    public Double getWeight() {
        return (Double) super.get(INTERNAL_WEIGHT_KEY);
    }


    public double[] getAvg() {
        int total = getTotal();
        if (total == 0) {
            return null;
        }
        if (total == 1) {
            return (double[]) super.get(INTERNAL_SUM_KEY);
        } else {
            double[] avg = (double[]) super.get(INTERNAL_SUM_KEY);
            for (int i = 0; i < avg.length; i++) {
                avg[i] = avg[i] / total;
            }
            return avg;
        }

    }

    public double[][] getCovariance(double[] avg) {
        if (avg == null) {
            return null;
        }
        int features = avg.length;

        int total = getTotal();
        if (total == 0) {
            return null;
        }
        if (total > 1) {
            double[][] covariances = new double[features][features];
            double[] sumsquares = (double[]) super.get(INTERNAL_SUMSQUARE_KEY);

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

    public Matrix getCovarianceMatrix(double[] avg) {
        int features = avg.length;

        int total = getTotal();
        if (total == 0) {
            return null;
        }
        if (total > 1) {
            double[] covariances = new double[features * features];
            double[] sumsquares = (double[]) super.get(INTERNAL_SUMSQUARE_KEY);

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

    public double[] getMin() {
        int total = getTotal();
        if (total == 0) {
            return null;
        }
        if (total == 1) {
            double[] min = (double[]) super.get(INTERNAL_SUM_KEY);
            return min;
        } else {
            double[] min = (double[]) super.get(INTERNAL_MIN_KEY);
            return min;
        }
    }

    public double[] getMax() {
        int total = getTotal();
        if (total == 0) {
            return null;
        }
        if (total == 1) {
            double[] max = (double[]) super.get(INTERNAL_SUM_KEY);
            return max;
        } else {
            double[] max = (double[]) super.get(INTERNAL_MAX_KEY);
            return max;
        }
    }

    public long[] getSubGraph() {
        long[] res = (long[]) super.get(INTERNAL_SUBGAUSSIAN_KEY);
        if (res == null) {
            res = new long[0];
        }
        return res;
    }


    private double distance(double[] features, double[] avg){
        double res=0;
        for(int i=0;i<features.length;i++){
            res+= (features[i]-avg[i])*(features[i]-avg[i]);
        }
        return Math.sqrt(res);
    }

    public boolean checkInside(double[] feature) {
        int total=getTotal();
        if(total<2){
            return true;
        }
        return true;
    }

}
