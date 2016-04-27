package org.mwg.gmm;

import org.mwg.*;
import org.mwg.clustering.KMeans;
import org.mwg.util.matrix.KMatrix;
import org.mwg.util.matrix.operation.MultivariateNormalDistribution;
import org.mwg.plugin.AbstractNode;

public class GaussianNode extends AbstractNode implements KGaussianNode {

    public GaussianNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    public static final int _CAPACITYFACTOR = 3;
    public static final int _COMPRESSIONITER = 10;
    public static double[] err;

    private static final String MIN_KEY = "getMin";
    private static final String MAX_KEY = "getMax";
    private static final String VALUE_KEY = "value";
    private static final String AVG_KEY = "getAvg";
    private static final String COV_KEY = "cov";


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


    @Override
    public void set(String attributeName, Object attributeValue) {
        if (attributeName.equals(VALUE_KEY)) {
            learn((double[]) attributeValue);
        } else {
            super.set(attributeName, attributeValue);
        }
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


    @Override
    public void configMixture(int levels, int maxPerLevel) {
        super.set(INTERNAL_LEVEL_KEY, levels);
        super.set(INTERNAL_WIDTH_KEY, maxPerLevel);
    }

    @Override
    public int getLevel() {
        Integer g = (Integer) super.get(INTERNAL_LEVEL_KEY);
        if (g != null) {
            return g;
        } else return 0;
    }

    @Override
    public int getMaxPerLevel() {
        Integer g = (Integer) super.get(INTERNAL_WIDTH_KEY);
        if (g != null) {
            return g;
        } else return 0;
    }

    @Override
    public void learnBatch(double[][] values) {
        //todo can be optimized later, but for now:
        for (int i = 0; i < values.length; i++) {
            learn(values[i]);
        }


    }

    @Override
    public void learn(final double[] value) {
        long[] subgaussians = (long[]) super.get(INTERNAL_SUBGAUSSIAN_KEY);
        if (subgaussians == null || subgaussians.length == 0) {
            internallearn(value, true);
        } else {
            final int level = getLevel();
            super.rel(INTERNAL_SUBGAUSSIAN_KEY, new Callback<Node[]>() {
                @Override
                public void on(Node[] result) {
                    boolean inside = false;
                    for (int i = 0; i < result.length; i++) {
                        GaussianNode subgaussian = (GaussianNode) result[i];
                        if (subgaussian.checkInside(value, level - 1)) {
                            subgaussian.learn(value);
                            inside = true;
                            break;
                        }
                    }
                    //if inside a sub, not add it to root
                    //if not insider a sub, add it to root
                    internallearn(value, !inside);
                }
            });
        }

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
                        KGaussianNode g = (KGaussianNode) result[i];
                        ((GaussianNode) g).updateLevel(newLevel - 1);
                    }
                }
            });
        }
    }

    private void createLevel(double[] values, int level, int width) {
        KGaussianNode g = (KGaussianNode) graph().newNode(this.world(), this.time(), "GaussianNode");
        g.configMixture(level, width);
        ((GaussianNode) g).internallearn(values, false); //dirac

        super.add(INTERNAL_SUBGAUSSIAN_KEY, g);
    }

    private void checkAndCompress() {

        final Node selfPointer = this;

        final int width = getMaxPerLevel();
        long[] subgaussians = (long[]) super.get(INTERNAL_SUBGAUSSIAN_KEY);
        if (subgaussians == null || subgaussians.length < _CAPACITYFACTOR * width) {
            return;
        } else {
            //Compress here
            super.rel(INTERNAL_SUBGAUSSIAN_KEY, new Callback<Node[]>() {
                @Override
                //result.length hold the original subgaussian number, and width is after compression
                public void on(Node[] result) {
                    int features = getNumberOfFeatures();

                    int[] totals = new int[width];

                    GaussianNode[] subgauss = new GaussianNode[result.length];
                    double[][] data = new double[result.length][];
                    for (int i = 0; i < result.length; i++) {
                        subgauss[i] = (GaussianNode) result[i];
                        data[i] = subgauss[i].getAvg();
                    }

                    //Cluster the different gaussians
                    KMeans clusteringEngine = new KMeans();
                    int[][] clusters = clusteringEngine.getClusterIds(data, width, _COMPRESSIONITER, getMin(), getMax(), err);

                    //Select the ones which will remain as head by the maximum weight
                    GaussianNode[] mainClusters = new GaussianNode[width];
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
                                GaussianNode g = subgauss[clusters[i][j]];
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


    private void move(GaussianNode subgaus) {
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


    public void internallearnBatch(double[][] values) {
        if (values.length == 0) {
            return;
        }
        if (values.length == 1) {
            learn(values[0]);
            return;
        }

        //here values are more than 2


        int features = values[0].length;
        int level = getLevel();
        int width = getMaxPerLevel();


        //manage total
        int total = getTotal();
        Double weight = getWeight();

        double[] sum;
        double[] min;
        double[] max;
        double[] sumsquares;

        if (total == 0) {
            weight = 0.0;
            sum = new double[features];
            min = new double[features];
            max = new double[features];
            sumsquares = new double[features * (features + 1) / 2];
        } else {
            //get everything from saved
            sum = (double[]) super.get(INTERNAL_SUM_KEY);
            min = (double[]) super.get(INTERNAL_MIN_KEY);
            max = (double[]) super.get(INTERNAL_MAX_KEY);
            sumsquares = (double[]) super.get(INTERNAL_SUMSQUARE_KEY);
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
                if (level > 0) {
                    createLevel(values[k], level - 1, width);
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
        set(INTERNAL_TOTAL_KEY, total);
        set(INTERNAL_WEIGHT_KEY, weight);
        set(INTERNAL_SUM_KEY, sum);
        set(INTERNAL_MIN_KEY, min);
        set(INTERNAL_MAX_KEY, max);
        set(INTERNAL_SUMSQUARE_KEY, sumsquares);
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

    @Override
    public int getNumberOfFeatures() {
        int total = getTotal();
        if (total == 0) {
            return 0;
        } else {
            double[] sum = (double[]) super.get(INTERNAL_SUM_KEY);
            return sum.length;
        }
    }

    @Override
    public double[] getSum() {
        int total = getTotal();
        if (total == 0) {
            return null;
        } else {
            return (double[]) super.get(INTERNAL_SUM_KEY);
        }
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
    public int getTotal() {
        Integer x = (Integer) super.get(INTERNAL_TOTAL_KEY);
        if (x == null) {
            return 0;
        } else {
            return x;
        }
    }

    @Override
    public Double getWeight() {
        return (Double) super.get(INTERNAL_WEIGHT_KEY);
    }

    @Override
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

    @Override
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

    @Override
    public KMatrix getCovarianceMatrix(double[] avg) {
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
            return new KMatrix(covariances, features, features);
        } else {
            return null;
        }
    }

    @Override
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

    @Override
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

    @Override
    public long[] getSubGraph() {
        long[] res = (long[]) super.get(INTERNAL_SUBGAUSSIAN_KEY);
        if (res == null) {
            res = new long[0];
        }
        return res;
    }

    @Override
    public boolean checkInside(double[] feature, int level) {
        return false; //to reimplement
    }

}
