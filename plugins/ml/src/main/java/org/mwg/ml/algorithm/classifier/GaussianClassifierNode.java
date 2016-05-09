package org.mwg.ml.algorithm.classifier;

import org.mwg.Graph;
import org.mwg.Node;
import org.mwg.ml.ClassificationNode;
import org.mwg.ml.common.AbstractClassifierSlidingWindowManagingNode;
import org.mwg.ml.common.matrix.Matrix;
import org.mwg.ml.common.matrix.operation.MultivariateNormalDistribution;
import org.mwg.plugin.NodeFactory;

import java.util.Arrays;

public class GaussianClassifierNode extends AbstractClassifierSlidingWindowManagingNode implements ClassificationNode {

    public static final String NAME = "GaussianClassifier";

    public static class Factory implements NodeFactory {
        @Override
        public String name() {
            return NAME;
        }

        @Override
        public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
            GaussianClassifierNode newNode = new GaussianClassifierNode(world, time, id, graph, initialResolution);
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
    public GaussianClassifierNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    //TODO Any synchronization?

    //TODO Try out changing parameters on the fly

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
        final int dims = getInputDimensions();
        setSums(classNum, new double[dims]);
        setSumsSquared(classNum, new double[dims * (dims + 1) / 2]);

        //Model can stay uninitialized until total is at least <TODO 2? 1?>
    }

    @Override
    protected void updateModelParameters(double value[], int classNum) {
        //Rebuild Gaussian for mentioned class
        //Update sum, sum of squares and total
        if (getInputDimensions()==INPUT_DIM_UNKNOWN){
            setInputDimensions(value.length);
        }
        initializeClassIfNecessary(classNum);
        setTotal(classNum, getClassTotal(classNum) + 1);

        double currentSum[] = getSums(classNum);
        for (int i = 0; i < value.length; i++) {
            currentSum[i] += value[i];
        }
        setSums(classNum, currentSum);
        //TODO No need to put? Depends on whether att returns a copy. Just in case, re-put

        double currentSumSquares[] = getSumSquares(classNum);
        int k = 0;
        for (int i = 0; i < value.length; i++) {
            for (int j = i; j < value.length; j++) {
                //Value at class index - force 0 (or it will be the ultimate predictor)
                currentSumSquares[k] += value[i] * value[j];
            }
            k++;
        }
        setSumsSquared(classNum, currentSumSquares);
        //TODO No need to put? Depends on whether att returns a copy. Just in case, re-put
    }

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
        int classes[] = getKnownClasses();
        double curMaxLikelihood = Double.NEGATIVE_INFINITY; //Even likelihood 0 should surpass it
        int curMaxLikelihoodClass = -1;
        for (int curClass : classes) {
            double curLikelihood = getLikelihoodForClass(value, curClass);
            if (curLikelihood > curMaxLikelihood) {
                curMaxLikelihood = curLikelihood;
                curMaxLikelihoodClass = curClass;
            }
        }
        return curMaxLikelihoodClass;
    }

    @Override
    public String toString() {
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
                Matrix cov =
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
