package org.mwg.ml.algorithm.classifier;

import org.mwg.*;
import org.mwg.ml.ClassificationNode;
import org.mwg.ml.common.matrix.operation.Gaussian1D;
import org.mwg.plugin.NodeFactory;

/**
 * Created by Andrey Boytsov on 4/14/2016.
 */
public class GaussianNaiveBayesianNode extends AbstractGaussianClassifierNode implements ClassificationNode {

    //TODO Any synchronization?

    //TODO Try out changing parameters on the fly

    public static final String NAME = "GaussianNaiveBayesian";

    public static class Factory implements NodeFactory {
        @Override
        public String name() {
            return NAME;
        }

        @Override
        public Node create(long world, long time, long id, Graph graph, long[] initialResolution) {
            GaussianNaiveBayesianNode newNode = new GaussianNaiveBayesianNode(world, time, id, graph, initialResolution);
            return newNode;
        }
    }


    /**
     * {@inheritDoc}
     */
    public GaussianNaiveBayesianNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution){
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }


    protected void initializeClassIfNecessary(int classNum){
        Object oldSumsObj = unphasedState().getFromKey(INTERNAL_SUM_KEY_PREFIX+classNum);
        if (oldSumsObj!=null){
            //Is there, but could be deleted
            double oldSums[] = (double[])oldSumsObj;
            if (oldSums.length>0){ //Is the class deleted?
                //Already initialized
                return;
            }
        }

        addToKnownClassesList(classNum);
        setTotal(classNum, 0);
        final int dimensions = getInputDimensions();
        setSums(classNum, new double[dimensions]);
        setSumsSquared(classNum, new double[dimensions]);

        //Model can stay uninitialized until total is at least <TODO 2? 1?>
    }


    @Override
    protected void updateModelParameters(double value[], int classNum){
        //Rebuild Gaussian for mentioned class
        //Update sum, sum of squares and total
        if (getInputDimensions()==INPUT_DIM_UNKNOWN){
            setInputDimensions(value.length);
        }
        initializeClassIfNecessary(classNum);
        setTotal(classNum, getClassTotal(classNum)+1);

        double currentSum[] = getSums(classNum);
        double currentSumSquares[] = getSumSquares(classNum);
        for (int i=0;i<value.length;i++) {
            currentSum[i] += value[i];
            currentSumSquares[i] += value[i]*value[i];
        }
        setSums(classNum, currentSum);
        setSumsSquared(classNum, currentSumSquares);
        //Need to re-put
    }

    protected double getLikelihoodForClass(double value[], int classNum){
        //It is assumed that real class is removed and replaces with 0
        initializeClassIfNecessary(classNum); //TODO should not be necessary. Double-check.
        int total = getClassTotal(classNum);
        if (total < 2){
            //Not enough data to build model for that class.
            return 0;
        }

        //For each dimension
        //Step 1. Get sum
        //Step 2. Get sum of squares.
        //Step 3. Multiply for each dimension
        double likelihood = 1;
        double sums[] = getSums(classNum);
        double sumSquares[] = getSumSquares(classNum);
        for (int i=0;i<getInputDimensions();i++){
            likelihood *= Gaussian1D.getDensity(sums[i], sumSquares[i], total, value[i]);
        }
        //TODO Use log likelihood? Can be better for underflows.
        return likelihood;
    }

    @Override
    protected int predictValue(double value[]){
        int kk[] = getKnownClasses();
        if (kk.length==1){
            return kk[0];
        }
        int classes[] = getKnownClasses();
        double curMaxLikelihood = Constants.BEGINNING_OF_TIME; //Even likelihood 0 should surpass it
        int curMaxLikelihoodClass = -1;
        for (int curClass : classes){
            double curLikelihood = getLikelihoodForClass(value, curClass);
            if (curLikelihood > curMaxLikelihood){
                curMaxLikelihood = curLikelihood;
                curMaxLikelihoodClass = curClass;
            }
        }
        return curMaxLikelihoodClass;
    }

    @Override
    public String toString(){
        String result = "";
        int allClasses[] = getKnownClasses();
        if (allClasses.length==0){
            return "No classes";
        }
        for (int classNum : allClasses) {
            initializeClassIfNecessary(classNum); //TODO should not be necessary. Double-check.
            int total = getClassTotal(classNum);
            if (total < 2){
                //Not enough data to build model for that class.
                result += classNum+": Not enough data("+total+")\n";
            }else{
                double sums[] = getSums(classNum);
                double means[] = getSums(classNum);
                for (int i=0;i<means.length;i++){
                    means[i] = means[i]/total;
                }
                double sumSquares[] = getSumSquares(classNum);
                result += classNum+": mean = ["; //TODO For now - cannot report variance from distribution
                for (int i=0;i<means.length;i++){
                    result += means[i]+", ";
                }
                result += "]\nCovariance:\n";
                result += "[";
                for (int j=0;j<means.length;j++){
                    result += Gaussian1D.getCovariance(sums[j], sumSquares[j], total)+", ";
                }
                result += "]\n";
            }
        }
        return result;//TODO Normalize on average?
    }
}
