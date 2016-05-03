package org.mwg.ml.classifier;

import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.maths.matrix.KMatrix;
import org.mwg.maths.matrix.operation.MultivariateNormalDistribution;
import org.mwg.plugin.NodeFactory;

import java.util.Arrays;

public class MLGaussianClassifierNode extends AbstractGaussianClassifierNode {

    public static final String NAME = "GaussianClassifier";

    public static class Factory implements NodeFactory {
        @Override
        public String name() {
            return NAME;
        }

        @Override
        public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
            MLGaussianClassifierNode newNode = new MLGaussianClassifierNode(world, time, id, graph, initialResolution);
            return newNode;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param p_world
     * @param p_time
     * @param p_id
     * @param p_graph
     * @param currentResolution
     */
    public MLGaussianClassifierNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    //TODO Any synchronization?

    //TODO Try out changing parameters on the fly

    @Override
    protected void initializeClassIfNecessary(int classNum) {
        Object oldSumsObj = unphasedState().getFromKey(INTERNAL_SUM_KEY_PREFIX + classNum);
        if (oldSumsObj != null) {
            //Is there, but could be deleted
            double oldSums[] = (double[]) oldSumsObj;
            if (oldSums.length > 0) { //Is the class deleted?
                //Already initialized
                return;
            }
        }

        addToKnownClassesList(classNum);
        setTotal(classNum, 0);
        final int dimensions = getInputDimensions();
        setSums(classNum, new double[dimensions]);
        setSumsSquared(classNum, new double[dimensions * (dimensions + 1) / 2]);

        //Model can stay uninitialized until total is at least <TODO 2? 1?>
    }

    @Override
    protected void updateModelParameters(double value[]) {
        final int classIndex = getResponseIndex();
        final int classNum = (int) value[classIndex];
        //Rebuild Gaussian for mentioned class
        //Update sum, sum of squares and total
        initializeClassIfNecessary(classNum);
        setTotal(classNum, getClassTotal(classNum) + 1);

        double currentSum[] = getSums(classNum);
        for (int i = 0; i < value.length; i++) {
            currentSum[i] += value[i];
        }
        //Value at class index - force 0 (or it will be the ultimate predictor)
        currentSum[classIndex] = 0;
        setSums(classNum, currentSum);
        //TODO No need to put? Depends on whether att returns a copy. Just in case, re-put

        double currentSumSquares[] = getSumSquares(classNum);
        int k = 0;
        for (int i = 0; i < value.length; i++) {
            for (int j = i; j < value.length; j++) {
                //Value at class index - force 0 (or it will be the ultimate predictor)
                if ((i != classIndex) && (j != classIndex)) {
                    currentSumSquares[k] += value[i] * value[j];
                }
            }
            k++;
        }
        setSumsSquared(classNum, currentSumSquares);
        //TODO No need to put? Depends on whether att returns a copy. Just in case, re-put
    }

    @Override
    protected double getLikelihoodForClass(double value[], int classNum) {
        //It is assumed that real class is removed and replaces with 0
        initializeClassIfNecessary(classNum); //TODO should not be necessary. Double-check.
        int total = getClassTotal(classNum);
        if (total < 2) {
            //Not enough data to build model for that class.
            return 0;
        }
        double sums[] = getSums(classNum);
        double sumSquares[] = getSumSquares(classNum);
        MultivariateNormalDistribution distr = MultivariateNormalDistribution.getDistribution(sums, sumSquares, total);
        return distr.density(value, true);//TODO Normalize on average? Does not matter (comparing anyway)
        //But normalization leaves less chance for underflow
    }

    @Override
    protected int predictValue(double value[]) {
        double valueWithClassRemoved[] = Arrays.copyOf(value, value.length);
        valueWithClassRemoved[getResponseIndex()] = 0; //Do NOT use real class for prediction
        int classes[] = getKnownClasses();
        double curMaxLikelihood = Double.NEGATIVE_INFINITY; //Even likelihood 0 should surpass it
        int curMaxLikelihoodClass = -1;
        for (int curClass : classes) {
            double curLikelihood = getLikelihoodForClass(valueWithClassRemoved, curClass);
            if (curLikelihood > curMaxLikelihood) {
                curMaxLikelihood = curLikelihood;
                curMaxLikelihoodClass = curClass;
            }
        }
        return curMaxLikelihoodClass;
    }

    public String allDistributionsToString() {
        String result = "";
        int allClasses[] = getKnownClasses();
        if (allClasses.length == 0) {
            return "No classes";
        }
        for (int classNum : allClasses) {
            initializeClassIfNecessary(classNum); //TODO should not be necessary. Double-check.
            int total = getClassTotal(classNum);
            if (total < 2) {
                //Not enough data to build model for that class.
                result += classNum + ": Not enough data(" + total + ")\n";
            } else {
                double sums[] = getSums(classNum);
                double means[] = getSums(classNum);
                for (int i = 0; i < means.length; i++) {
                    means[i] = means[i] / total;
                }
                double sumSquares[] = getSumSquares(classNum);
                KMatrix cov =
                        MultivariateNormalDistribution.getCovariance(sums, sumSquares, total);
                result += classNum + ": mean = ["; //TODO For now - cannot report variance from distribution
                for (int i = 0; i < means.length; i++) {
                    result += means[i] + ", ";
                }
                result += "]\nCovariance:\n";
                for (int i = 0; i < cov.rows(); i++) {
                    result += "[";
                    for (int j = 0; j < cov.columns(); j++) {
                        result += cov.get(i, j) + ", ";
                    }
                    result += "]\n";
                }
            }
        }
        return result;//TODO Normalize on average?
    }
}
