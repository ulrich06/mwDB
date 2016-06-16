package org.mwg.ml.common;

import java.util.ArrayList;
import java.util.List;

public class DecisionTreeNode{
    //TODO Add conditions and splits.
    public double boundary; //For splitting the criterion
    public int classNum; //For leaf
    public DecisionTreeNode left; // <boundary
    public DecisionTreeNode right; //>=boundary
    public int featureNum;
    //TODO Unknown? NaN?

    private static int ELEMENTS_PER_NODE = 5;

    public static DecisionTreeNode deserializeFromDoubleArray(double decisionTreeArray[]){
        return deserializeFromDoubleArrayWithRoot(decisionTreeArray, 0);
    }

    protected static DecisionTreeNode deserializeFromDoubleArrayWithRoot(double decisionTreeArray[], int rootIndex){
        DecisionTreeNode dtr = new DecisionTreeNode();
        dtr.boundary = decisionTreeArray[rootIndex*ELEMENTS_PER_NODE];
        dtr.classNum = (int)decisionTreeArray[rootIndex*ELEMENTS_PER_NODE+1];
        int leftNum = (int)decisionTreeArray[rootIndex*ELEMENTS_PER_NODE+2];
        //Stringctly > is correct - 0 is root node, loops are not acceptable
        dtr.left = (leftNum>0)?deserializeFromDoubleArrayWithRoot(decisionTreeArray, leftNum):null;
        int rightNum = (int)decisionTreeArray[rootIndex*ELEMENTS_PER_NODE+3];
        dtr.right = (rightNum>0)?deserializeFromDoubleArrayWithRoot(decisionTreeArray, rightNum):null;
        dtr.featureNum = (int)decisionTreeArray[rootIndex*ELEMENTS_PER_NODE+4];
        return dtr;
    }

    /**
     * @return Decision tree (with current node as a root) serialized to double array
     */
    public double[] serializeToDoubleArray(){
        List<Double> resultObjects = serializeToDoubleObjectArray();
        double result[] = new double[resultObjects.size()];
        for (int i=0;i<result.length;i++){
            result[i] = resultObjects.get(i);
        }
        return result;
    }

    private List<Double> serializeToDoubleObjectArray(){
        List<Double> result = new ArrayList<Double>();
        List<DecisionTreeNode> frontier = new ArrayList<DecisionTreeNode>();
        frontier.add(this);
        int currentNumberToAdd = 0;
        while(frontier.size()>0){
            DecisionTreeNode currentNode = frontier.remove(0);
            result.add(currentNode.boundary);
            result.add((double)currentNode.classNum);
            if (currentNode.left!=null){
                currentNumberToAdd++;
                result.add((double)currentNumberToAdd);
                frontier.add(currentNode.left);
            }else{
                result.add(-1.0);
            }
            if (currentNode.right!=null){
                currentNumberToAdd++;
                result.add((double)currentNumberToAdd);
                frontier.add(currentNode.right);
            }else{
                result.add(-1.0);
            }
            result.add((double)currentNode.featureNum);
        }
        return result;
    }

}