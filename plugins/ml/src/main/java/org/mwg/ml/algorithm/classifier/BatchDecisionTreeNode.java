package org.mwg.ml.algorithm.classifier;

import org.mwg.Graph;
import org.mwg.ml.common.AbstractClassifierSlidingWindowManagingNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by andre on 5/9/2016.
 */
public class BatchDecisionTreeNode extends AbstractClassifierSlidingWindowManagingNode {

    //TODO what to do after node split? Re-create decision tree from buffer?

    //TODO Make criterion configurable (entropy vs Giny index vs accuracy improvement vs ... think of other options)

    //TODO Configure for built-in pruning?

    DecisionTreeNode rootNode = null;

    protected class DecisionTreeNode{
        //TODO Add conditions and splits.
        double boundary; //For splitting the criterion
        int classNum; //For leaf
        DecisionTreeNode left; // <boundary
        DecisionTreeNode right; //>=boundary
        int featureNum;
        //TODO Unknown? NaN?
    }

    public BatchDecisionTreeNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    @Override
    protected int predictValue(double[] value) {
        //TODO
        return 0;
    }

    @Override
    protected double getLikelihoodForClass(double[] value, int classNum) {
        final int predictedClass = predictValue(value);
        //No real likelihood. Just yes or no.
        return (classNum==predictedClass)?1.0:0.0;
    }

    private static double[] extractColumn(double[][] values, int columnNumber){
        double result[] = new double[values.length];
        for (int i=0;i<result.length;i++){
            result[i] = values[i][columnNumber];
        }
        return result;
    }

    private static double getBoundary(double values[]){
        //TODO: for now it is only median. Add more sophisticated things.
        double sortedValues[] = Arrays.copyOf(values, values.length);
        Arrays.sort(sortedValues);
        final int midArray = sortedValues.length/2;
        if (values.length % 2 == 0)
            return (sortedValues[midArray] + sortedValues[midArray - 1])/2; //Mind that index start with 0
        return sortedValues[midArray]; //Again, mind that index start with 0
    }

    private static int[] toIntArray(List<Integer> intList){
        int result[] = new int[intList.size()];
        int index = 0;
        for (Integer i : intList){
            result[index] = i;
            index++;
        }
        return result;
    }

    private static double[][] toDouble2DArray(List<double[]> doubleList){
        double result[][] = new double[doubleList.size()][];
        int index = 0;
        for (double[] i : doubleList){
            result[index] = Arrays.copyOf(i, i.length);
            index++;
        }
        return result;
    }

    private static double getImprovement(double[] values, int classNumbers[]){
        //TODO for now using only accuracy improvement. Add more criteria.
        double boundary = getBoundary(values);
        int originalErrors = getMajorityErrors(classNumbers);
        List<Integer> aboveBoundaryClassNumbers = new ArrayList<Integer>();
        List<Integer> belowBoundaryClassNumbers = new ArrayList<Integer>();
        for (int i=0;i<values.length;i++){
            //= can go either way.
            //It is a median, so something is likely to be on the boundary.
            if (values[i]>=boundary){
                aboveBoundaryClassNumbers.add(classNumbers[i]);
            }else{
                belowBoundaryClassNumbers.add(classNumbers[i]);
            }
        }
        int errorsAbove = getMajorityErrors(toIntArray(aboveBoundaryClassNumbers));
        int errorsBelow = getMajorityErrors(toIntArray(belowBoundaryClassNumbers));
        double accuracyBefore = ((double)originalErrors)/classNumbers.length;
        double accuracyAfter = ((double)errorsAbove + errorsBelow)/classNumbers.length;
        return accuracyAfter-accuracyBefore;
    }

    private static int getMajorityErrors(int classNumbers[]){
        int mostPopularClass = getMostFrequentElement(classNumbers);
        int errors = 0;
        for (int i=0;i<classNumbers.length;i++){
            errors += (classNumbers[i]==mostPopularClass)?0:1;
        }
        return errors;
    }

    private static int getMostFrequentElement(int[] classNumbers) {
        assert classNumbers.length > 0; //Should ot reach that spot if we have empty set
        int sortedClassNumbers[] = Arrays.copyOf(classNumbers, classNumbers.length);
        Arrays.sort(sortedClassNumbers);
        //Actually, we don't need sorting itself
        //We only need to group same class numbers together for detecting the majority
        //This way we can have majority detection in O(n*log(n)) + O(n) = O(n*log(n))
        //As opposed to O(n^2) for straightforward algorithm
        int maxClass = 0;
        int maxCount = Integer.MIN_VALUE;
        int currentClass = 0;
        int currentCount = 0;
        for (int i=0;i<sortedClassNumbers.length;i++){
            if ((sortedClassNumbers[i]!=currentClass) || (i==0)){ //Class has changed
                if (currentCount > maxCount) {
                    maxClass = currentClass;
                    maxCount = currentCount;
                }
                currentClass = sortedClassNumbers[i];
                currentCount = 0;
            }
            currentCount++;
        }
        return maxClass;
    }

    private DecisionTreeNode createLeaf(int classNum){
        DecisionTreeNode leaf = new DecisionTreeNode();
        leaf.featureNum = -1;
        leaf.boundary = Double.NaN;
        leaf.classNum = classNum;
        leaf.left = null;
        leaf.right = null;
        return leaf;
    }

    private double[][] unpackValues(double[] values){
        final int dims = getInputDimensions();
        final int numValues = getNumValuesInBuffer();
        double result[][] = new double[numValues][dims];
        int indexX = 0;
        int indexY = 0;
        for (int i=0;i<values.length;i++){
            result[indexX][indexY] = values[i];
            indexY = (indexY+1) % dims;
            if(indexY==0){
                indexX++;
            }
        }
        return result;
    }

    private DecisionTreeNode split(double[][] values, int classNumbers[]){
        assert classNumbers.length > 0;
        //Step 1. Select the feature to split upon
        // For each feature:
        //   1.1. Get the boundary
        //   1.2. Calculate the criterion
        // Select feature with maximum criterion.
        // If improvement is not worth it, just stop here and make a leaf.
        // Otherwise - make a split, recursively call this method.
        if (allClassNumbersAreTheSame(classNumbers)){
            return createLeaf(classNumbers[0]);
        }

        final int dims = getInputDimensions();
        int chosenFeature = -1;
        double maxImprovement = 0; //TODO initialize with threshold
        for (int i=0;i<dims;i++){
            double valuesOfFeature[] = extractColumn(values, i);
            double improvement = getImprovement(valuesOfFeature, classNumbers);
            if (improvement > maxImprovement){
                maxImprovement = improvement;
                chosenFeature = i;
            }
        }
        if (chosenFeature < 0){
            return createLeaf(getMostFrequentElement(classNumbers));
        }

        //So, we chose the feature.
        //TODO Do not duplicate the code. We did all of that when choosing the features
        double valuesByFeature[] = extractColumn(values, chosenFeature);
        double boundary = getBoundary(valuesByFeature);

        List<Integer> aboveBoundaryClassNumbers = new ArrayList<Integer>();
        List<double[]> aboveBoundaryValues = new ArrayList<double[]>();
        List<Integer> belowBoundaryClassNumbers = new ArrayList<Integer>();
        List<double[]> belowBoundaryValues = new ArrayList<double[]>();
        for (int i=0;i<values.length;i++){
            //= can go either way.
            //It is a median, so something is likely to be on the boundary.
            if (values[i][chosenFeature]>=boundary){
                aboveBoundaryClassNumbers.add(classNumbers[i]);
                aboveBoundaryValues.add(values[i]);
            }else{
                belowBoundaryClassNumbers.add(classNumbers[i]);
                belowBoundaryValues.add(values[i]);
            }
        }
        double bbValuesArray[][] = toDouble2DArray(aboveBoundaryValues);
        int bbClasses[] = toIntArray(belowBoundaryClassNumbers);
        double abValuesArray[][] = toDouble2DArray(aboveBoundaryValues);
        int abClasses[] = toIntArray(aboveBoundaryClassNumbers);

        DecisionTreeNode split = new DecisionTreeNode();
        split.featureNum = chosenFeature;
        split.boundary = Double.NaN;
        //split.classNum does not matter
        split.left = split(bbValuesArray, bbClasses);
        split.right = split(abValuesArray, abClasses);
        return split;
    }

    private boolean allClassNumbersAreTheSame(int[] classNumbers) {
        final int curClass = classNumbers[0];
        for (int i=1;i<classNumbers.length;i++){
            if (classNumbers[i]!=curClass){
                return false;
            }
        }
        return true;
    }

    @Override
    protected void updateModelParameters(double[] value, int classNumber) {
        if (getInputDimensions()==INPUT_DIM_UNKNOWN){
            setInputDimensions(value.length);
        }

        rootNode = split(unpackValues(getValueBuffer()), getRealBufferClasses());
    }
}
