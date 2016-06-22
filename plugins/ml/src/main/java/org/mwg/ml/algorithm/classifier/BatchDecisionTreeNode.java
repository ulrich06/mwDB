package org.mwg.ml.algorithm.classifier;

import org.mwg.Graph;
import org.mwg.Type;
import org.mwg.ml.algorithm.AbstractClassifierSlidingWindowManagingNode;
import org.mwg.ml.common.DecisionTreeNode;
import org.mwg.plugin.NodeState;

import java.util.*;

public class BatchDecisionTreeNode extends AbstractClassifierSlidingWindowManagingNode {
    public static final String NAME = "BatchDecisionTreeClassifier";

    //TODO what to do after node split? Re-create decision tree from buffer? Now - yes

    //TODO Make criterion configurable (entropy vs Giny index vs accuracy improvement vs ... think of other options)

    //TODO Configure for built-in pruning?

    DecisionTreeNode rootNode = null;

    public BatchDecisionTreeNode(long p_world, long p_time, long p_id, Graph p_graph, long[] currentResolution) {
        super(p_world, p_time, p_id, p_graph, currentResolution);
    }

    protected int predict(DecisionTreeNode node, double value[]) {
        if (node==rootNode){
            System.out.println(value[0]);
            if (value.length>1){
                System.out.println("\t"+value[1]);
            }
        }
        if ((node.left == null) && (node.right == null)) {
            System.out.println("Class: "+node.classNum);
            return node.classNum;
        }
        System.out.println("Going deeper...");
        if (value[node.featureNum] >= node.boundary) {
            return predict(node.right, value);
        }
        return predict(node.left, value);
    }

    protected static final String INTERNAL_DECISION_TREE_KEY = "_decisionTreeSerialized";

    @Override
    protected int predictValue(NodeState state, double[] value) {
        if (rootNode == null) {
            double serializedTree[] = (double[])state.getFromKey(INTERNAL_DECISION_TREE_KEY);
            String s = "DESERIALIZED\n";
            for (int i = 0;i<serializedTree.length;i++){
                s += serializedTree[i]+"; ";
                if (i%5 == 4){
                    s += "\n";
                }
            }
            System.out.println(s);
            rootNode = DecisionTreeNode.deserializeFromDoubleArray(serializedTree);
        }
        return predict(rootNode, value);
    }

    @Override
    protected double getLikelihoodForClass(NodeState state, double[] value, int classNum) {
        final int predictedClass = predictValue(state, value);
        //No real likelihood. Just yes or no.
        return (classNum == predictedClass) ? 1.0 : 0.0;
    }

    private static double[] extractColumn(double[][] values, int columnNumber) {
        double result[] = new double[values.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = values[i][columnNumber];
        }
        return result;
    }

    private static double[] getAllPossibleBoundaries(double values[]) {
        //When counting median, leave only unique elements.
        //Otherwise we can end up with everything above or below the boundary.
        Set<Double> uniqueValuesSet = new HashSet<Double>();
        for (int i = 0; i < values.length; i++) {
            uniqueValuesSet.add(values[i]);
        }
        if (uniqueValuesSet.size() == 1) {
            return new double[]{values[0]};
        }
        Double sortedValues[] = uniqueValuesSet.toArray(new Double[0]);
        Arrays.sort(sortedValues);
        double result[] = new double[sortedValues.length - 1];
        for (int i = 0; i < result.length; i++) {
            double a = sortedValues[i];
            double b = sortedValues[i+1];
            result[i] = a + b;
            System.out.println("Sum (a+b)"+(a+b));
            System.out.println("Sum (result)"+result[i]);
            result[i] = (a + b) / 2.0;
            System.out.println("Averaging: "+sortedValues[i]+"\t"+sortedValues[i+1]+"\t"+result[i]);
        }
        String s = "\tRESULT";
        for (int i=0;i<result.length;i++){
            s += result[i]+";";
        }
        System.out.println(s);
        return result;
    }

    private static int[] toIntArray(List<Integer> intList) {
        int result[] = new int[intList.size()];
        int index = 0;
        for (int i = 0; i < intList.size(); i++) {
            result[index] = intList.get(i);
            index++;
        }
        return result;
    }

    private static double[][] toDouble2DArray(List<double[]> doubleList) {
        double result[][] = new double[doubleList.size()][];
        int index = 0;
        for (int i = 0; i < doubleList.size(); i++) {
            double[] curArray = doubleList.get(i);
            result[index] = new double[curArray.length];
            System.arraycopy(curArray, 0, result[index], 0, curArray.length);
            index++;
        }
        return result;
    }

    private static double[] getImprovementAndBoundary(double[] values, int classNumbers[]) {
        //TODO for now using only accuracy improvement. Add more criteria.
        double boundariesList[] = getAllPossibleBoundaries(values);
        String s ="\tBOUNDARIES LIST";
        for (int i=0;i<boundariesList.length;i++){
            s += boundariesList[i]+";";
        }
        System.out.println(s);
        double maxImprovement = -1; //Improvement cannot be <0 ,so we'll replace that immediately
        double bestBoundary = Double.NaN;
        for (int k = 0; k < boundariesList.length; k++) {
            double boundary = boundariesList[k];
            int originalErrors = getMajorityErrors(classNumbers);
            List<Integer> aboveBoundaryClassNumbers = new ArrayList<Integer>();
            List<Integer> belowBoundaryClassNumbers = new ArrayList<Integer>();
            for (int i = 0; i < values.length; i++) {
                //= can go either way.
                //It is a median, so something is likely to be on the boundary.
                if (values[i] >= boundary) {
                    aboveBoundaryClassNumbers.add(classNumbers[i]);
                } else {
                    belowBoundaryClassNumbers.add(classNumbers[i]);
                }
            }
            int errorsAbove = getMajorityErrors(toIntArray(aboveBoundaryClassNumbers));
            int errorsBelow = getMajorityErrors(toIntArray(belowBoundaryClassNumbers));
            double errorPercentageBefore = ((double) originalErrors) / classNumbers.length;
            double errorPercentageAfter = ((double) errorsAbove + errorsBelow) / classNumbers.length;
            double improvement = errorPercentageBefore - errorPercentageAfter;
            if (improvement > maxImprovement) {
                maxImprovement = improvement;
                bestBoundary = boundary;
            }
        }

        //double boundary = getBoundary(values);
        return new double[]{maxImprovement, bestBoundary};
    }

    private static int getMajorityErrors(int classNumbers[]) {
        int mostPopularClass = getMostFrequentElement(classNumbers);
        int errors = 0;
        for (int i = 0; i < classNumbers.length; i++) {
            errors += (classNumbers[i] == mostPopularClass) ? 0 : 1;
        }
        return errors;
    }

    private static int getMostFrequentElement(int[] classNumbers) {
        assert classNumbers.length > 0; //Should ot reach that spot if we have empty set
        int sortedClassNumbers[] = new int[classNumbers.length];
        System.arraycopy(classNumbers, 0, sortedClassNumbers, 0, classNumbers.length);
        Arrays.sort(sortedClassNumbers);
        //Actually, we don't need sorting itself
        //We only need to group same class numbers together for detecting the majority
        //This way we can have majority detection in O(n*log(n)) + O(n) = O(n*log(n))
        //As opposed to O(n^2) for straightforward algorithm
        int maxClass = 0;
        int maxCount = -1;
        int currentClass = 0;
        int currentCount = 0;
        for (int i = 0; i < sortedClassNumbers.length; i++) {
            if ((sortedClassNumbers[i] != currentClass) || (i == 0)) { //Class has changed
                if (currentCount > maxCount) {
                    maxClass = currentClass;
                    maxCount = currentCount;
                }
                currentClass = sortedClassNumbers[i];
                currentCount = 0;
            }
            currentCount++;
        }
        if (currentCount > maxCount) {
            maxClass = currentClass;
        }
        return maxClass;
    }

    private DecisionTreeNode createLeaf(int classNum) {
        DecisionTreeNode leaf = new DecisionTreeNode();
        leaf.featureNum = -1;
        leaf.boundary = Double.NaN;
        leaf.classNum = classNum;
        leaf.left = null;
        leaf.right = null;
        return leaf;
    }

    private double[][] unpackValues(double[] values, int dims) {
        final int numValues = values.length/dims;
        double result[][] = new double[numValues][dims];
        int indexX = 0;
        int indexY = 0;
        for (int i = 0; i < values.length; i++) {
            result[indexX][indexY] = values[i];
            indexY = (indexY + 1) % dims;
            if (indexY == 0) {
                indexX++;
            }
        }
        return result;
    }

    private DecisionTreeNode split(NodeState state, double[][] values, int classNumbers[]) {
        assert classNumbers.length > 0;
        System.out.println("Starting split - inside");
        //Step 1. Select the feature to split upon
        // For each feature:
        //   1.1. Get the boundary
        //   1.2. Calculate the criterion
        // Select feature with maximum criterion.
        // If improvement is not worth it, just stop here and make a leaf.
        // Otherwise - make a split, recursively call this method.
        if (allClassNumbersAreTheSame(classNumbers)) {
            System.out.println("Same classes: "+classNumbers[0]);
            return createLeaf(classNumbers[0]);
        }

        if (allValuesAreTheSame(values)) {
            System.out.println("Same values: "+getMostFrequentElement(classNumbers));
            return createLeaf(getMostFrequentElement(classNumbers));
        }

        System.out.println("Split - checkpoint 1");

        final int dims = state.getFromKeyWithDefault(INPUT_DIM_KEY, INPUT_DIM_DEF);
        int chosenFeature = -1;
        double maxImprovement = 0; //TODO initialize with threshold
        double bestBoundary = Double.NaN;
        for (int i = 0; i < dims; i++) {
            double valuesOfFeature[] = extractColumn(values, i);
            double improvementAndBoundary[] = getImprovementAndBoundary(valuesOfFeature, classNumbers);
            double improvement = improvementAndBoundary[0];
            double boundary = improvementAndBoundary[1];
            System.out.println(i+"\tImprovement: "+improvement+"\tBoundary: "+boundary);
            if (improvement > maxImprovement) {
                maxImprovement = improvement;
                bestBoundary = boundary;
                chosenFeature = i;
            }
        }
        if (chosenFeature < 0) {
            System.out.println("No improvement: "+getMostFrequentElement(classNumbers)+"\t"+bestBoundary);
            return createLeaf(getMostFrequentElement(classNumbers));
        }

        //So, we chose the feature.
        //TODO Do not duplicate the code. We did all of that when choosing the features
        double valuesByFeature[] = extractColumn(values, chosenFeature);
        double boundary = bestBoundary;

        List<Integer> aboveBoundaryClassNumbers = new ArrayList<Integer>();
        List<double[]> aboveBoundaryValues = new ArrayList<double[]>();
        List<Integer> belowBoundaryClassNumbers = new ArrayList<Integer>();
        List<double[]> belowBoundaryValues = new ArrayList<double[]>();
        for (int i = 0; i < values.length; i++) {
            //= can go either way.
            //It is a median, so something is likely to be on the boundary.
            if (values[i][chosenFeature] >= boundary) {
                aboveBoundaryClassNumbers.add(classNumbers[i]);
                aboveBoundaryValues.add(values[i]);
            } else {
                belowBoundaryClassNumbers.add(classNumbers[i]);
                belowBoundaryValues.add(values[i]);
            }
        }
        double bbValuesArray[][] = toDouble2DArray(belowBoundaryValues);
        int bbClasses[] = toIntArray(belowBoundaryClassNumbers);
        double abValuesArray[][] = toDouble2DArray(aboveBoundaryValues);
        int abClasses[] = toIntArray(aboveBoundaryClassNumbers);

        DecisionTreeNode split = new DecisionTreeNode();
        split.featureNum = chosenFeature;
        split.boundary = boundary;
        //split.classNum does not matter
        split.left = split(state, bbValuesArray, bbClasses);
        split.right = split(state, abValuesArray, abClasses);
        System.out.println("Further split by feature: "+split.featureNum+"\tBoundary: "+split.boundary);
        return split;
    }

    protected static boolean allClassNumbersAreTheSame(int[] classNumbers) {
        final int curClass = classNumbers[0];
        for (int i = 1; i < classNumbers.length; i++) {
            if (classNumbers[i] != curClass) {
                return false;
            }
        }
        return true;
    }

    //TODO Limit precision? Introduce epsilon?
    protected static boolean allValuesAreTheSame(double[][] values) {
        final double curValue[] = values[0];
        for (int i = 1; i < values.length; i++) {
            for (int j = 0; j < curValue.length; j++) {
                if (values[i][j] != curValue[j]) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void updateModelParameters(NodeState state, double valueBuffer[], int resultBuffer[], double[] value, int classNumber) {
        System.out.println("Updating for class number: "+classNumber);
        if (state.getFromKeyWithDefault(INPUT_DIM_KEY, INPUT_DIM_DEF) == INPUT_DIM_UNKNOWN) {
            state.setFromKey(INPUT_DIM_KEY, Type.INT, value.length);
        }

        System.out.println("Starting split.");
        rootNode = split(state, unpackValues(valueBuffer, valueBuffer.length/resultBuffer.length), resultBuffer);
        double serializedTree[] = rootNode.serializeToDoubleArray();

        String s = "SERIALIZED\n";
        for (int i = 0;i<serializedTree.length;i++){
            s += serializedTree[i]+"; ";
            if (i%5 == 4){
                s += "\n";
            }
        }
        s += "Boundary: "+rootNode.boundary+"\n";
        s += "ClassNum: "+rootNode.classNum+"\n";
        s += "Left: "+rootNode.left+"\n";
        s += "Right: "+rootNode.right+"\n";
        s += "FeatureNum: "+rootNode.featureNum+"\n";
        int dims = valueBuffer.length/resultBuffer.length;
        int resIndex = 0;
        for (int i = 0;i<valueBuffer.length;i++){
            s += valueBuffer[i]+"; ";
            if (i%dims == (dims-1)){
                s += "\t"+resultBuffer[resIndex]+"\n";
                resIndex++;
            }
        }
        System.out.println(s);

        state.setFromKey(INTERNAL_DECISION_TREE_KEY, Type.DOUBLE_ARRAY, serializedTree);
    }

    @Override
    protected void removeAllClassesHook(NodeState state) {
        //Nothing to do here
    }
}
